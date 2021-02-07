package com.lift.service;

import com.lift.consts.LiftState;
import com.lift.vo.Building;
import com.lift.vo.Lift;
import com.lift.vo.LiftCallRequest;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class NearestLiftDispatcherServiceImplTest extends TestCase {

  @Test
  public void testGetNearestLift() {
    int buildingId = 1;
    Map<Integer, Building> buildingsPerId = new HashMap<>();
    buildingsPerId.put(buildingId, new Building(buildingId, 10, 4));

    NearestLiftDispatcherServiceImpl service = new NearestLiftDispatcherServiceImpl(buildingsPerId);

    LiftCallRequest req1 = new LiftCallRequest(0,0,5);
    final Lift nearestLift1 = service.getNearestLift(buildingId, req1);
    Assert.assertEquals("Nearest Lift",1,nearestLift1.getLiftId());

    LiftCallRequest req2 = new LiftCallRequest(0,4,6);
    final Lift nearestLift2 = service.getNearestLift(buildingId, req2);
    Assert.assertEquals("Nearest Lift",1,nearestLift2.getLiftId());

    LiftCallRequest req3 = new LiftCallRequest(0,3,0);
    final Lift nearestLift3 = service.getNearestLift(buildingId, req3);
    Assert.assertEquals("Nearest Lift",2,nearestLift3.getLiftId());

    LiftCallRequest req4 = new LiftCallRequest(0,5,3);
    final Lift nearestLift4 = service.getNearestLift(buildingId, req4);
    Assert.assertEquals("Nearest Lift",3,nearestLift4.getLiftId());

    LiftCallRequest req5 = new LiftCallRequest(0,3,10);
    final Lift nearestLift5 = service.getNearestLift(buildingId, req5);
    Assert.assertEquals("Nearest Lift",1,nearestLift5.getLiftId());

    LiftCallRequest req6 = new LiftCallRequest(0,2,1);
    final Lift nearestLift6 = service.getNearestLift(buildingId, req6);
    Assert.assertEquals("Nearest Lift",2,nearestLift6.getLiftId());

  }
}