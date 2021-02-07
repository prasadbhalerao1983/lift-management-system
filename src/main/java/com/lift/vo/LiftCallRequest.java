package com.lift.vo;

public class LiftCallRequest {

  private final int time;
  private final int source;
  private final int destination;

  public LiftCallRequest(int time, int source, int destination) {
    this.time = time;
    this.source = source;
    this.destination = destination;
  }

  public int getTime() {
    return time;
  }

  public int getSource() {
    return source;
  }

  public int getDestination() {
    return destination;
  }
}
