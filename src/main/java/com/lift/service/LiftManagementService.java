package com.lift.service;

import com.lift.vo.Lift;
import com.lift.vo.LiftStateOutput;
import com.lift.vo.LiftCallRequest;
import java.util.List;
import java.util.Map;

public interface LiftManagementService {

  void initBuildingLifts(int buildingId, int numOfFloors, int numOfLifts);

  Map<Integer, List<LiftStateOutput>> createLiftTravelTimeline(int buildingId, List<LiftCallRequest> travelRequests);
}
