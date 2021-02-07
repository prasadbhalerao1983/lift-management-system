package com.lift.service;

import com.lift.consts.LiftState;
import com.lift.vo.Building;
import com.lift.vo.Lift;
import com.lift.vo.LiftCallRequest;
import java.util.List;
import java.util.Map;

public class NearestLiftDispatcherServiceImpl implements LiftDispatcherService {

  //Key: buildingId, Building, builing contains lifts
  private final Map<Integer, Building> buildingsPerId;

  public NearestLiftDispatcherServiceImpl(Map<Integer, Building> buildingsPerId) {
    this.buildingsPerId = buildingsPerId;
  }

  /**
   * Nearest Car algorithm.
   * d= abs(car floor - landing floor (floor from which lift is called))
   * and N is the number of floors.
   *
   * Four simple rule for lift selections.
   * 1. FS = N + 1 - (d - 1) = N + 2 - d
   * This rule will come into effect if the elevator car is moving towards the landing
   * call and the call is set in the same direction.
   *
   * 2. FS = N + 1 - d
   * This rule will come into effect if the elevator car is moving towards the landing
   * call but the call is set to the opposite direction.
   *
   * 3. FS = 1
   * This rule will come into effect if the elevator car is already moving away from
   * the landing call (the elevator is responding to some other call).
   *
   * 4. FS = N + 1 - d
   * This rule will come into effect if the elevator car is idle.
   */
  @Override
  public Lift getNearestLift(int buildingId, LiftCallRequest req) {

    final Building building = buildingsPerId.get(buildingId);
    final List<Lift> lifts = building.getLifts();

    int fs = 1;
    Lift selectedLift = lifts.get(0);
    for (Lift lift : lifts) {
      //distance between call floor and lift curr floor
      final int distance = Math.abs(req.getSource() - lift.getCurrFloor());

      int newFs = 1;
      if (lift.getLiftState() == LiftState.IDLE) {
        newFs = building.getNumOfFloors() + 2 - distance;
      } else if (lift.getLiftState() == LiftState.GOING_DOWN) {

        //lift moved away from the lift-call floor
        if (req.getSource() > lift.getCurrFloor()) {
          newFs = 1;
        } else if (req.getSource() < lift.getCurrFloor() && !isGoingUp(req.getSource(), req.getDestination())) {
          //if the elevator car is moving towards the landing call
          // and the call is set in the same direction
          newFs = building.getNumOfFloors() + 3 - distance;
        } else if (req.getSource() < lift.getCurrFloor() && isGoingUp(req.getSource(), req.getDestination())) {
          //This rule will come into effect if the elevator car is moving towards the landing
          //call but the call is set to the opposite direction.
          newFs = building.getNumOfFloors() + 1 - distance;
        }
      } else if (lift.getLiftState() == LiftState.GOING_UP) {

        //lift moved away from the lift-call floor
        if (req.getSource() < lift.getCurrFloor()) {
          newFs = 1;
        } else if (req.getSource() > lift.getCurrFloor() && isGoingUp(req.getSource(), req.getDestination())) {
          //if the elevator car is moving towards the landing call
          // and the call is set in the same direction
          newFs = building.getNumOfFloors() + 3 - distance;
        } else if (req.getSource() > lift.getCurrFloor() && !isGoingUp(req.getSource(), req.getDestination())) {
          //This rule will come into effect if the elevator car is moving towards the landing
          //call but the call is set to the opposite direction.
          newFs = building.getNumOfFloors() + 1 - distance;
        }
      }

      if (newFs > fs) {
        fs = newFs;
        selectedLift = lift;

      }
    }
    LiftState state = null;
    if (isGoingUp(req.getSource(), req.getDestination())) {
      state = LiftState.GOING_UP;
    } else {
      state = LiftState.GOING_DOWN;
    }
    selectedLift.setLiftState(state);

    return selectedLift;
  }

  private boolean isGoingUp(int source, int destination) {
    return source < destination ? true : false;
  }

}
