package com.lift.exceptions;

public class BuildingInitException extends RuntimeException {

  public BuildingInitException(String message) {
    super(message);
  }

  public BuildingInitException(String message, Throwable cause) {
    super(message, cause);
  }

  public BuildingInitException(Throwable cause) {
    super(cause);
  }
}
