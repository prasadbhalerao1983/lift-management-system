package com.lift.service;

import com.lift.consts.DoorState;
import com.lift.util.LiftMgmtUtil;
import com.lift.vo.LiftCallRequest;
import com.lift.vo.LiftStateOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class LiftManagementServiceImplTest {

  //CASE: Single lift, all request going up
  @Test
  public void testTravel1() {

    LiftManagementServiceImpl service = new LiftManagementServiceImpl();
    final int numOfLifts = 1;
    service.initBuildingLifts(1, 10, numOfLifts);

    final List<LiftCallRequest> list = getLiftRequestsForTestTravel1();

    final Map<Integer, List<LiftStateOutput>> timeLinePerLift = service.createLiftTravelTimeline(1, list);

    final List<LiftStateOutput> expectedResults = getExpectedResultForTestTravel1();

    Assert.assertTrue(timeLinePerLift.size() == 1);
    int expectedLiftId = 1;
    Assert.assertTrue(timeLinePerLift.containsKey(expectedLiftId));

    final List<LiftStateOutput> actualTimeline = timeLinePerLift.get(expectedLiftId);

    for (int ctr = 0; ctr < expectedResults.size(); ctr++) {
      Assert.assertEquals(expectedResults.get(ctr), actualTimeline.get(ctr));
    }

    LiftMgmtUtil.printLiftTimeLine(timeLinePerLift, numOfLifts);
  }

  //CASE: Single lift, request going up as well down
  @Test
  public void testTravel2() {
    final int numOfLifts = 1;
    LiftManagementServiceImpl service = new LiftManagementServiceImpl();
    service.initBuildingLifts(1, 10, numOfLifts);

    final List<LiftCallRequest> list = getLiftRequestsForTestTravel2();

    final Map<Integer, List<LiftStateOutput>> timeLinePerLift = service.createLiftTravelTimeline(1, list);

    final List<LiftStateOutput> expectedResults = getExpectedResultForTestTravel2();

    Assert.assertTrue(timeLinePerLift.size() == 1);
    int expectedLiftId = 1;
    Assert.assertTrue(timeLinePerLift.containsKey(expectedLiftId));

    final List<LiftStateOutput> actualTimeline = timeLinePerLift.get(expectedLiftId);

    for (int ctr = 0; ctr < expectedResults.size(); ctr++) {
      Assert.assertEquals(expectedResults.get(ctr), actualTimeline.get(ctr));
    }

    LiftMgmtUtil.printLiftTimeLine(timeLinePerLift, numOfLifts);
  }


  //CASE: 3 lifts, request going up as well down
  @Test
  public void testTravel3() {

    LiftManagementServiceImpl service = new LiftManagementServiceImpl();
    final int numOfLifts = 3;
    service.initBuildingLifts(1, 10, numOfLifts);

    final List<LiftCallRequest> list = getLiftRequestsForTestTravel3();

    final Map<Integer, List<LiftStateOutput>> timeLinePerLift = service.createLiftTravelTimeline(1, list);

    final Map<Integer, List<LiftStateOutput>> expectedTimeLine = getExpectedResultForTestTravel3();

    Assert.assertTrue(timeLinePerLift.containsKey(1));
    Assert.assertTrue(timeLinePerLift.containsKey(2));

    final List<LiftStateOutput> actualLift1Timline = timeLinePerLift.get(1);
    final List<LiftStateOutput> actualLift2Timline = timeLinePerLift.get(2);

    final List<LiftStateOutput> expectedLift1Timline = expectedTimeLine.get(1);
    final List<LiftStateOutput> expectedLift2Timline = expectedTimeLine.get(2);

    for (int ctr = 0; ctr < expectedLift1Timline.size(); ctr++) {
      Assert.assertEquals(expectedLift1Timline.get(ctr), actualLift1Timline.get(ctr));
      Assert.assertEquals(expectedLift2Timline.get(ctr), actualLift2Timline.get(ctr));
    }

    LiftMgmtUtil.printLiftTimeLine(timeLinePerLift, numOfLifts);
  }

  private void printTravelTrace(List<LiftStateOutput> timeLine) {

    for (LiftStateOutput op : timeLine) {

      System.out.println("T=" + op.getTime());
      System.out
          .println("LIFT " + op.getLiftId() + "-->" + op.getCurrFloor() + "(" + op.getDoorState().strValue() + ")");
    }
  }


  //2 Up request but at different times (Number of lifts: 1).
  private List<LiftCallRequest> getLiftRequestsForTestTravel1() {
    List<LiftCallRequest> list = new ArrayList<>();
    list.add(new LiftCallRequest(0, 0, 7));
    list.add(new LiftCallRequest(2, 4, 6));
    return list;
  }

  //2 Up request at different times and
  //2 down  request at different times. (Number of lifts: 1).
  private List<LiftCallRequest> getLiftRequestsForTestTravel2() {
    List<LiftCallRequest> list = new ArrayList<>();
    list.add(new LiftCallRequest(0, 0, 7));
    list.add(new LiftCallRequest(2, 4, 6));
    list.add(new LiftCallRequest(0, 3, 0));
    list.add(new LiftCallRequest(11, 8, 1));
    return list;
  }

  //2 Up request at different times and
  //1 down  request at different times. (Number of lifts: 2).
  private List<LiftCallRequest> getLiftRequestsForTestTravel3() {
    List<LiftCallRequest> list = new ArrayList<>();
    list.add(new LiftCallRequest(0, 0, 7));
    list.add(new LiftCallRequest(2, 4, 6));
    list.add(new LiftCallRequest(0, 3, 0));
    return list;
  }


  private List<LiftStateOutput> getExpectedResultForTestTravel1() {

    List<LiftStateOutput> list = new ArrayList<>();
    list.add(new LiftStateOutput(1, 0, 0, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 1, 0, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 2, 1, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 3, 2, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 4, 3, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 5, 4, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 6, 4, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 7, 5, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 8, 6, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 9, 6, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 10, 7, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 11, 7, DoorState.DOOR_CLOSE));
    return list;
  }


  private List<LiftStateOutput> getExpectedResultForTestTravel2() {

    List<LiftStateOutput> list = new ArrayList<>();
    list.add(new LiftStateOutput(1, 0, 0, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 1, 0, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 2, 1, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 3, 2, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 4, 3, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 5, 4, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 6, 4, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 7, 5, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 8, 6, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 9, 6, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 10, 7, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 11, 7, DoorState.DOOR_CLOSE));

    list.add(new LiftStateOutput(1, 12, 8, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 13, 8, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 14, 8, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 15, 7, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 16, 6, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 17, 5, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 18, 4, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 19, 3, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 20, 3, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 21, 2, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 22, 1, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 23, 1, DoorState.DOOR_CLOSE));
    list.add(new LiftStateOutput(1, 24, 0, DoorState.DOOR_OPEN));
    list.add(new LiftStateOutput(1, 25, 0, DoorState.DOOR_CLOSE));
    return list;
  }

  private Map<Integer, List<LiftStateOutput>> getExpectedResultForTestTravel3() {

    Map<Integer, List<LiftStateOutput>> timeLinePerLift = new HashMap<>();
    List<LiftStateOutput> lift1 = new ArrayList<>();
    List<LiftStateOutput> lift2 = new ArrayList<>();
    timeLinePerLift.put(1, lift1);
    timeLinePerLift.put(2, lift2);

    List<LiftStateOutput> list = new ArrayList<>();
    lift1.add(new LiftStateOutput(1, 0, 0, DoorState.DOOR_OPEN));
    lift1.add(new LiftStateOutput(1, 1, 0, DoorState.DOOR_CLOSE));
    lift1.add(new LiftStateOutput(1, 2, 1, DoorState.DOOR_CLOSE));
    lift1.add(new LiftStateOutput(1, 3, 2, DoorState.DOOR_CLOSE));
    lift1.add(new LiftStateOutput(1, 4, 3, DoorState.DOOR_CLOSE));
    lift1.add(new LiftStateOutput(1, 5, 4, DoorState.DOOR_OPEN));
    lift1.add(new LiftStateOutput(1, 6, 4, DoorState.DOOR_CLOSE));
    lift1.add(new LiftStateOutput(1, 7, 5, DoorState.DOOR_CLOSE));
    lift1.add(new LiftStateOutput(1, 8, 6, DoorState.DOOR_OPEN));
    lift1.add(new LiftStateOutput(1, 9, 6, DoorState.DOOR_CLOSE));
    lift1.add(new LiftStateOutput(1, 10, 7, DoorState.DOOR_OPEN));
    lift1.add(new LiftStateOutput(1, 11, 7, DoorState.DOOR_CLOSE));

    lift2.add(new LiftStateOutput(2, 0, 0, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 1, 1, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 2, 2, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 3, 3, DoorState.DOOR_OPEN));
    lift2.add(new LiftStateOutput(2, 4, 3, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 5, 2, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 6, 1, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 7, 0, DoorState.DOOR_OPEN));
    lift2.add(new LiftStateOutput(2, 8, 0, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 9, 0, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 10, 0, DoorState.DOOR_CLOSE));
    lift2.add(new LiftStateOutput(2, 11, 0, DoorState.DOOR_CLOSE));

    return timeLinePerLift;
  }
}