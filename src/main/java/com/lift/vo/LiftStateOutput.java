package com.lift.vo;

import com.lift.consts.DoorState;
import java.util.Objects;

public class LiftStateOutput {

  private int liftId;
  private int time;
  private int currFloor;
  private DoorState doorState;

  public LiftStateOutput(int liftId, int time, int currFloor, DoorState doorState) {
    this.liftId = liftId;
    this.time = time;
    this.currFloor = currFloor;
    this.doorState = doorState;
  }

  public int getLiftId() {
    return liftId;
  }

  public int getTime() {
    return time;
  }

  public int getCurrFloor() {
    return currFloor;
  }

  public DoorState getDoorState() {
    return doorState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LiftStateOutput that = (LiftStateOutput) o;
    return liftId == that.liftId && time == that.time && currFloor == that.currFloor && doorState == that.doorState;
  }

  @Override
  public int hashCode() {
    return Objects.hash(liftId, time, currFloor, doorState);
  }

  @Override
  public String toString() {
    return "LiftStateOutput{" + "liftId=" + liftId + ", time=" + time + ", currFloor=" + currFloor + ", doorState="
        + doorState + '}';
  }
}
