import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayTimeSlot {
    public static final int DAYS = 6;
    public static final int LESSONS = 8;
    private final int dayNumber;
    private final int lessonNumber;
    public static final List<List<DayTimeSlot>> slotByDayAndTime = new ArrayList<>();

    static {
        for (int i = 0; i < DAYS; i++) {
             List<DayTimeSlot> slotsForCurrentDay = new ArrayList<>();
             for (int j  = 0; j < LESSONS; j++) {
                 slotsForCurrentDay.add(new DayTimeSlot(i, j));
             }
             slotByDayAndTime.add(slotsForCurrentDay);
         }
    }

    public DayTimeSlot(int dayNumber, int lessonNumber) {
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
