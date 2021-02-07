package com.lift.service;

import com.lift.consts.DoorState;
import com.lift.consts.FloorType;
import com.lift.consts.LiftDirection;
import com.lift.consts.LiftState;
import com.lift.exceptions.BuildingInitException;
import com.lift.vo.Building;
import com.lift.vo.Lift;
import com.lift.vo.LiftCallRequest;
import com.lift.vo.LiftStateOutput;
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
  public Map<Integer, List<LiftStateOutput>> createLiftTravelTimeline(int buildingId,
      List<LiftCallRequest> travelRequests) {

    final Building building = buildingsPerId.get(buildingId);
    validateRequest(travelRequests, building);

    final Map<Integer, Lift> liftPerLiftId = getLiftsWithEnqueuedReq(buildingId, travelRequests);

    //Key liftId, Value: list of lift state (lift timeline)
    Map<Integer, List<LiftStateOutput>> liftTimeLinePerLiftId = new HashMap<>();

    int maxTimeLineSize = 0;
    for (Lift currLift : liftPerLiftId.values()) {

      int liftId = currLift.getLiftId();

      List<LiftStateOutput> liftTimeLineList = new ArrayList<>();
      liftTimeLinePerLiftId.put(liftId, liftTimeLineList);

      final List<LiftCallRequest> liftCallRequests = emptyLiftQueue(currLift);
      final LiftTimelinePrereq prereqInfo = prepInfoForLiftTimeLine(liftCallRequests);

      int timeUnit = 0;

      timeUnit = createLiftTravelTimelineForUpDir(prereqInfo, currLift, liftTimeLineList, timeUnit);
      timeUnit = createLiftTravelTimelineForDownDir(prereqInfo, currLift, liftTimeLineList, timeUnit);

      //Set lift state to IDLE after processing all lift calls
      currLift.setLiftState(LiftState.IDLE);

      if (liftTimeLineList.size() > maxTimeLineSize) {
        maxTimeLineSize = liftTimeLineList.size();
      }
    }

    updateTimeLineForUnselectedLifts(buildingId, liftTimeLinePerLiftId, maxTimeLineSize);

    return liftTimeLinePerLiftId;


  }

  private void validateRequest(List<LiftCallRequest> travelRequests, Building building) {
    for (LiftCallRequest req : travelRequests) {
      if ((req.getSource() < 0 && req.getSource() > building.getNumOfFloors()) && (req.getDestination() < 0
          && req.getDestination() > building.getNumOfFloors())){
        throw new IllegalArgumentException("Invalid floor numbers");
      }
    }
  }

  private void updateTimeLineForUnselectedLifts(int buildingId,
      Map<Integer, List<LiftStateOutput>> liftTimeLinePerLiftId, int maxTimeLineSize) {
    final List<Lift> lifts = getLifts(buildingId);
    for (Lift lift : lifts) {
      if (!liftTimeLinePerLiftId.containsKey(lift.getLiftId())) {
        List<LiftStateOutput> timeline = new ArrayList<>();
        for (int ctr = 0; ctr < maxTimeLineSize; ctr++) {
          timeline.add(new LiftStateOutput(lift.getLiftId(), ctr, lift.getCurrFloor(), DoorState.DOOR_CLOSE));
        }
        liftTimeLinePerLiftId.put(lift.getLiftId(), timeline);
      } else {
        final List<LiftStateOutput> timeline = liftTimeLinePerLiftId.get(lift.getLiftId());
        if (timeline.size() < maxTimeLineSize) {
          final LiftStateOutput lastState = timeline.get(timeline.size() - 1);
          for (int ctr = timeline.size(); ctr < maxTimeLineSize; ctr++) {
            timeline.add(new LiftStateOutput(lift.getLiftId(), ctr, lift.getCurrFloor(), DoorState.DOOR_CLOSE));
          }
        }
      }
    }
  }

  private List<Lift> getLifts(int buildingId) {
    return buildingsPerId.get(buildingId).getLifts();
  }

  private int createLiftTravelTimelineForUpDir(LiftTimelinePrereq prereqInfo, Lift currLift,
      List<LiftStateOutput> liftTimeLineList, int timeUnit) {

    int maxFloor = Math.max(prereqInfo.maxDestFloorForUpDir, prereqInfo.maxSourceFloorForDownDir);
    if (!CollectionUtils.isEmpty(prereqInfo.callTimePerFloorForUpDir)) {

      //if lift-call floor is below currLift floor, then bring lift to call floor
      if (prereqInfo.minSourceFloorForUpDir < currLift.getCurrFloor()) {

        for (int floor = currLift.getCurrFloor(); floor > currLift.getCurrFloor(); floor--) {
          liftTimeLineList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
          currLift.moveDown();
        }
      }

      //Moving UP
      boolean liftMovementFlag = false;
      for (int floor = currLift.getCurrFloor(); floor <= maxFloor; floor++) {

        final Tuple tuple = prereqInfo.callTimePerFloorForUpDir.get(floor);
        if (tuple != null && tuple.getTime() <= timeUnit) {
          liftTimeLineList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_OPEN));
          liftTimeLineList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
        } else {
          liftTimeLineList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
        }

        if (liftMovementFlag) {
          currLift.moveUp();
        }
        liftMovementFlag = true;
      }
    }
    return timeUnit;
  }

  private int createLiftTravelTimelineForDownDir(LiftTimelinePrereq prereqInfo, Lift currLift,
      List<LiftStateOutput> liftTimeLineList, int timeUnit) {

    //lift Moving down
    if (!CollectionUtils.isEmpty(prereqInfo.callTimePerFloorForDownDir)) {

      //if lift-call floor is above currLift floor, then bring lift to call floor
      if (prereqInfo.maxSourceFloorForDownDir > currLift.getCurrFloor()) {
        for (int floor = currLift.getCurrFloor(); floor < prereqInfo.maxSourceFloorForDownDir; floor++) {
          liftTimeLineList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
          currLift.moveUp();
        }
      }

      boolean liftMovementFlag = false;
      for (int floor = currLift.getCurrFloor(); floor >= prereqInfo.minDestFloorForDownDir; floor--) {

        final Tuple tuple = prereqInfo.callTimePerFloorForDownDir.get(floor);
        if (tuple != null && tuple.getTime() <= timeUnit) {
          liftTimeLineList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_OPEN));
          liftTimeLineList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
        } else {
          liftTimeLineList.add(new LiftStateOutput(currLift.getLiftId(), timeUnit++, floor, DoorState.DOOR_CLOSE));
        }

        if (liftMovementFlag) {
          currLift.moveDown();
        }
        liftMovementFlag = true;
      }
    }

    return timeUnit;
  }

  private LiftTimelinePrereq prepInfoForLiftTimeLine(List<LiftCallRequest> liftCallRequests) {
    Map<Integer, Tuple> reqTimePerFloorForUpDir = new HashMap<>();
    Map<Integer, Tuple> reqTimePerFloorForDownDir = new HashMap<>();

    //Find min source and max dest floor
    int minSourceFloorForUpDir = Integer.MAX_VALUE;
    int maxDestFloorForUpDir = Integer.MIN_VALUE;

    int minDestFloorForDownDir = Integer.MAX_VALUE;
    int maxSourceFloorForDownDir = Integer.MIN_VALUE;

    for (LiftCallRequest req : liftCallRequests) {

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

    return new LiftTimelinePrereq(reqTimePerFloorForUpDir, reqTimePerFloorForDownDir, minSourceFloorForUpDir,
        maxDestFloorForUpDir, minDestFloorForDownDir, maxSourceFloorForDownDir);
  }

  private List<LiftCallRequest> emptyLiftQueue(Lift lift) {

    List<LiftCallRequest> requests = new ArrayList<>();

    while (lift.liftCallQueueSize() != 0) {
      requests.add(lift.dequeueLiftCall());
    }
    return requests;
  }

  private Map<LiftDirection, List<LiftCallRequest>> groupRequestsAccordingDirection(
      List<LiftCallRequest> travelRequest) {

    List<LiftCallRequest> upList = new ArrayList<>();
    List<LiftCallRequest> downList = new ArrayList<>();

    Set<Integer> allFloorsForUpDir = new HashSet<>();
    Set<Integer> allFloorsForDownDir = new HashSet<>();

    int minTimeForUpDir = Integer.MAX_VALUE;
    int minTimeForDownDir = Integer.MAX_VALUE;
    for (LiftCallRequest req : travelRequest) {

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

  private boolean isGoingUp(LiftCallRequest req) {
    return req.getSource() < req.getDestination();
  }


  private Map<Integer, Lift> getLiftsWithEnqueuedReq(int buildingId, List<LiftCallRequest> reqList) {

    Map<Integer, Lift> lifPerLiftId = new HashMap<>();

    for (LiftCallRequest req : reqList) {
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


  private Comparator<LiftCallRequest> getComparator() {

    return new Comparator<LiftCallRequest>() {
      @Override
      public int compare(LiftCallRequest o1, LiftCallRequest o2) {

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

  private static class LiftTimelinePrereq {

    final Map<Integer, Tuple> callTimePerFloorForUpDir;
    final Map<Integer, Tuple> callTimePerFloorForDownDir;

    //Find min source and max dest floor
    final int minSourceFloorForUpDir;
    final int maxDestFloorForUpDir;

    final int minDestFloorForDownDir;
    final int maxSourceFloorForDownDir;

    public LiftTimelinePrereq(Map<Integer, Tuple> reqTimePerFloorForUpDir,
        Map<Integer, Tuple> reqTimePerFloorForDownDir, int minSourceFloorForUpDir, int maxDestFloorForUpDir,
        int minDestFloorForDownDir, int maxSourceFloorForDownDir) {
      this.callTimePerFloorForUpDir = reqTimePerFloorForUpDir;
      this.callTimePerFloorForDownDir = reqTimePerFloorForDownDir;
      this.minSourceFloorForUpDir = minSourceFloorForUpDir;
      this.maxDestFloorForUpDir = maxDestFloorForUpDir;
      this.minDestFloorForDownDir = minDestFloorForDownDir;
      this.maxSourceFloorForDownDir = maxSourceFloorForDownDir;
    }


  }
}
