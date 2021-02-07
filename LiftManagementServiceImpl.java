package com.lift.service;

import com.lift.consts.DoorState;
import com.lift.consts.FloorType;
import com.lift.consts.LiftDirection;
import com.lift.exceptions.BuildingInitException;
import com.lift.vo.Building;
import com.lift.vo.Lift;
import com.lift.vo.LiftStateOutput;
import com.lift.vo.LiftTravelRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.CollectionUtils;

public class LiftManagementServiceImpl implements LiftManagementService {

  //Key: buildingId, Building
  private final Map<Integer, Building> buildingsPerId = new HashMap<>();

  private final NearestLiftDispatcherServiceImpl liftDispatcherService;

  public LiftManagementServiceImpl() {
    this.liftDispatcherService = new NearestLiftDispatcherServiceImpl(buildingsPerId);
  }

  @Override
  public void initBuildingLifts(int buildingId, int numOfFloors, int numOfLifts) {
    if (buildingsPerId.containsKey(buildingId)) {
      throw new BuildingInitException("Building Lifts are already initialized for buildingId:" + buildingId);
    }
    buildingsPerId.put(buildingId, new Building(buildingId, numOfFloors, numOfLifts));
  }

  @Override
  public List<LiftStateOutput> travel(int buildingId, List<LiftTravelRequest> travelRequests) {

    final Building building = buildingsPerId.get(buildingId);

    Map<Integer, Tuple> reqTimePerFloorForUpDir = new HashMap<>();
    Map<Integer, Tuple> reqTimePerFloorForDownDir = new HashMap<>();

    //Find min source and max dest floor
    int minSourceFloorForUpDir = Integer.MAX_VALUE;
    int maxDestFloorForUpDir = Integer.MIN_VALUE;

    int minDestFloorForDownDir = Integer.MAX_VALUE;
    int maxSourceFloorForDownDir = Integer.MIN_VALUE;

    for (LiftTravelRequest req : travelRequests) {

      if (isGoingUp(req)) {
        reqTimePerFloorForUpDir.put(req.getSource(), new Tuple(req.getTime(), FloorType.SOURCE));
        reqTimePerFloorForUpDir.put(req.getDestination(), new Tuple(req.getTime(), FloorType.DESTINATION));
        if (req.getSource() < minSourceFloorForUpDir) {
          minSourceFloorForUpDir = req.getSource();
        }
        if (req.getDestination() > maxDestFloorForUpDir) {
          maxDestFloorForUpDir = req.getDestination();
        }
      } else {
        reqTimePerFloorForDownDir.put(req.getSource(), new Tuple(req.getTime(), FloorType.SOURCE));
        reqTimePerFloorForDownDir.put(req.getDestination(), new Tuple(req.getTime(), FloorType.DESTINATION));
        if (req.getDestination() < minDestFloorForDownDir) {
          minDestFloorForDownDir = req.getDestination();
        }
        if (req.getSource() > maxSourceFloorForDownDir) {
          maxSourceFloorForDownDir = req.getSource();
        }
      }
    }

    int timeUnit = 0;
    int liftId = 1;

    List<LiftStateOutput> outputList = new ArrayList<>();

    //Use getClosestLiFt method to get Lift id
    final Lift currLift = building.getLifts().get(liftId - 1);

    int maxFloor = Math.max(maxDestFloorForUpDir, maxSourceFloorForDownDir);

    if (!CollectionUtils.isEmpty(reqTimePerFloorForUpDir)) {
      //Moving UP
      boolean liftMovementFlag = false;
      for (int floor = currLift.getCurrFloor(); floor <= maxFloor; floor++) {

        final Tuple tuple = reqTimePerFloorForUpDir.get(floor);
        if (tuple != null && tuple.getTime() <= timeUnit) {
          outputList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_OPEN));
          outputList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
        } else {
          outputList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
        }

        if (liftMovementFlag) {
          currLift.moveUp();
        }
        liftMovementFlag = true;
      }
    }

    if (!CollectionUtils.isEmpty(reqTimePerFloorForDownDir)) {
      //Moving down
      boolean liftMovementFlag = false;
      for (int floor = currLift.getCurrFloor(); floor >= minDestFloorForDownDir; floor--) {

        final Tuple tuple = reqTimePerFloorForDownDir.get(floor);
        if (tuple != null && tuple.getTime() <= timeUnit) {
          outputList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_OPEN));
          outputList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
        } else {
          outputList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
        }

        if (liftMovementFlag) {
          currLift.moveDown();
        }
        liftMovementFlag = true;
      }
    }

    return outputList;
  }

  private Map<LiftDirection, List<LiftTravelRequest>> groupRequestsAccordingDirection(
      List<LiftTravelRequest> travelRequest) {

    List<LiftTravelRequest> upList = new ArrayList<>();
    List<LiftTravelRequest> downList = new ArrayList<>();

    Set<Integer> allFloorsForUpDir = new HashSet<>();
    Set<Integer> allFloorsForDownDir = new HashSet<>();

    int minTimeForUpDir = Integer.MAX_VALUE;
    int minTimeForDownDir = Integer.MAX_VALUE;
    for (LiftTravelRequest req : travelRequest) {

      if (isGoingUp(req)) {
        upList.add(req);
        allFloorsForUpDir.add(req.getSource());
        allFloorsForUpDir.add(req.getDestination());
        if (minTimeForUpDir > req.getTime()) {
          minTimeForUpDir = req.getTime();
        }
      } else {
        downList.add(req);

        allFloorsForDownDir.add(req.getSource());
        allFloorsForDownDir.add(req.getDestination());

        if (minTimeForDownDir > req.getTime()) {
          minTimeForDownDir = req.getTime();
        }
      }


    }

    return null;
  }

  private boolean isGoingUp(LiftTravelRequest req) {
    return req.getSource() < req.getDestination();
  }


  private Map<Integer, Lift> getLiftsWithEnqueuedReq(int buildingId, List<LiftTravelRequest> reqList) {

    Map<Integer, Lift> lifPerLiftId = new HashMap<>();

    for (LiftTravelRequest req : reqList) {
      final Lift nearestLift = this.liftDispatcherService.getNearestLift(buildingId, req);
      lifPerLiftId.put(nearestLift.getLiftId(), nearestLift);
      nearestLift.enqueLiftCall(req);
    }
    return lifPerLiftId;
  }


  private boolean isGoingUp(int source, int destination) {
    return source < destination ? true : false;
  }

  private static class Tuple {

    private int time;
    private FloorType floorType;

    public Tuple(int time, FloorType state) {
      this.time = time;
      this.floorType = state;
    }

    public int getTime() {
      return time;
    }

    public FloorType getFloorType() {
      return floorType;
    }
  }


  private Comparator<LiftTravelRequest> getComparator() {

    return new Comparator<LiftTravelRequest>() {
      @Override
      public int compare(LiftTravelRequest o1, LiftTravelRequest o2) {

        if (o1.getSource() < o2.getSource()) {
          if (o1.getDestination() < o2.getDestination()) {
            return -1;
          } else {
            return 1;
          }
        } else if (o1.getSource() > o2.getSource()) {
          return 1;
        } else {
          return 0;
        }
      }
    };
  }
}
