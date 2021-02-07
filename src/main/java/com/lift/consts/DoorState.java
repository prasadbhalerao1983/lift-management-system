package com.lift.consts;

public enum DoorState {
  DOOR_OPEN("OPEN"), DOOR_CLOSE("CLOSE");

  private String value;

  DoorState(String val) {
    this.value = val;
  }

  public String strValue() {
    return this.value;
  }
}
