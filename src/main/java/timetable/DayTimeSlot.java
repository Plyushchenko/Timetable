package timetable;

import java.util.ArrayList;
import java.util.List;

public class DayTimeSlot {
    final static int DAYS = 6;
    final static int LESSONS = 8;
    private final int dayNumber;
    private final int lessonNumber;
    final static List<List<DayTimeSlot>> slotByDayAndTime = new ArrayList<>();

    static {
        for (int i = 0; i < DAYS; i++) {
             List<DayTimeSlot> slotsForCurrentDay = new ArrayList<>();
             for (int j  = 0; j < LESSONS; j++) {
                 slotsForCurrentDay.add(new DayTimeSlot(i, j));
             }
             slotByDayAndTime.add(slotsForCurrentDay);
         }
    }

    private DayTimeSlot(int dayNumber, int lessonNumber) {
        this.dayNumber = dayNumber;
        this.lessonNumber = lessonNumber;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public int getLessonNumber() {
        return lessonNumber;
    }
}
