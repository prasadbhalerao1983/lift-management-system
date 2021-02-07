package com.lift.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Building {

  private final int buildingId;
  private final int numOfFloors;
  private final int numOfLifts;

  private final List<Lift> lifts;

  public Building(int buildingId, int numOfFloors, int numOfLifts) {
    this.buildingId = buildingId;
    this.numOfFloors = numOfFloors;
    this.numOfLifts = numOfLifts;

    List<Lift> tempLifts = new ArrayList<>();

    for (int liftId = 1; liftId <= numOfLifts; liftId++) {
      tempLifts.add(new Lift(liftId,0,numOfFloors));
    }
    this.lifts = Collections.unmodifiableList(tempLifts);
  }

  public int getBuildingId() {
    return buildingId;
  }

  public int getNumOfFloors() {
    return numOfFloors;
  }

  public int getNumOfLifts() {
    return numOfLifts;
  }

  public List<Lift> getLifts() {
    return lifts;
  }
}
