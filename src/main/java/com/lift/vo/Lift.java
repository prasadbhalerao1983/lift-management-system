package com.lift.vo;

import com.lift.consts.LiftState;
import java.util.LinkedList;
import java.util.List;

public class Lift {

  private final int liftId;

  private int currFloor;

  private int minFloor;

  private int maxFloor;

  private final List<LiftCallRequest> liftCallQueue;

  private LiftState liftState;

  public Lift(int id, int minFloor, int maxFloor) {
    this.liftId = id;
    this.minFloor = minFloor;
    this.maxFloor = maxFloor;

    //Initializing lift to 0th floor and door closed state as per the assumption
    this.currFloor = 0;
    liftState = LiftState.IDLE;
    liftCallQueue = new LinkedList<>();
  }

  public int getLiftId() {
    return liftId;
  }

  public int getCurrFloor() {
    return currFloor;
  }

  public int getMinFloor() {
    return minFloor;
  }

  public int getMaxFloor() {
    return maxFloor;
  }

  public int moveUp() {
    if (currFloor < maxFloor) {
      return ++currFloor;
    } else {
      throw new IllegalStateException("Can't move above highest floor" + currFloor);
    }
  }

  public int moveDown() {
    if (currFloor > minFloor) {
      return --currFloor;
    } else {
      throw new IllegalStateException("Can't move below lowest floor" + currFloor);
    }
  }

  public void enqueLiftCall(LiftCallRequest request) {
    liftCallQueue.add(request);
  }

  public LiftCallRequest dequeueLiftCall() {
    return liftCallQueue.remove(0);
  }

  public int liftCallQueueSize() {
    return liftCallQueue.size();
  }

  public LiftState getLiftState() {
    return liftState;
  }

  public void setLiftState(LiftState liftState) {
    this.liftState = liftState;
  }
}
