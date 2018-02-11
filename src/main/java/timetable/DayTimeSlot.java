package timetable;

import java.util.ArrayList;
import java.util.List;

class DayTimeSlot {
    final static int DAYS = 6;
    final static int LESSONS = 8;
    final static DayTimeSlot[][] slotByDayAndTime = new DayTimeSlot[DAYS][LESSONS];
    private final int dayNumber;
    private final int lessonNumber;

    static {
        for (int i = 0; i < DAYS; i++) {
            for (int j  = 0; j < LESSONS; j++) {
                slotByDayAndTime[i][j] = new DayTimeSlot(i, j);
            }
        }
    }

    private DayTimeSlot(int dayNumber, int lessonNumber) {
        this.dayNumber = dayNumber;
        this.lessonNumber = lessonNumber;
    }

}
