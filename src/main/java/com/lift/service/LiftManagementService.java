package com.lift.service;

import com.lift.vo.Lift;
import com.lift.vo.LiftStateOutput;
import com.lift.vo.LiftCallRequest;
import java.util.List;

public interface LiftManagementService {

  void initBuildingLifts(int buildingId, int numOfFloors, int numOfLifts);

  List<LiftStateOutput> travel(int buildingId, List<LiftCallRequest> travelRequest);

}
