package com.lift.service;

import com.lift.vo.Lift;
import com.lift.vo.LiftCallRequest;

public interface LiftDispatcherService {


  /**
   *
   * @param buildingId
   * @param request
   * @return Nearest Lift based on lift call
   */
  public Lift getNearestLift(int buildingId, LiftCallRequest request);
}
