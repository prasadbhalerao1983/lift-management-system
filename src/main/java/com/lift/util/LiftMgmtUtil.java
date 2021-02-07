package com.lift.util;

import com.lift.vo.LiftStateOutput;
import java.util.List;
import java.util.Map;

public class LiftMgmtUtil {

  private LiftMgmtUtil() {
  }

  public static void printLiftTimeLine(Map<Integer, List<LiftStateOutput>> timeLinePerLiftId, int noOfLifts) {

    final int timelineSize = timeLinePerLiftId.get(1).size();

    for (int i = 0; i < timelineSize; i++) {

      boolean flag = true;
      for (int liftId = 1; liftId <= noOfLifts; liftId++) {

        final List<LiftStateOutput> timeline = timeLinePerLiftId.get(liftId);

        if (flag) {
          System.out.println("T=" + timeline.get(i).getTime());
        }
        System.out.print(
            "LIFT_NUM_" + timeline.get(i).getLiftId() + " --> " + timeline.get(i).getCurrFloor() + "(" + timeline.get(i)
                .getDoorState().strValue() + ") , ");

        flag = false;

      }
      System.out.println("");
    }
  }
}
