package timetable;

import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;
import java.util.List;

public class SchoolClass extends TimetableXMLObject {
    /*
    Format:
    <class id="E06839D1C346DD93" name="5-а" short="5-а" teacherid="" classroomids="" grade="5" partner_id=""/>
     */
    private final List<Lesson> lessons = new ArrayList<>();
    private final List<SchoolGroup> schoolGroups = new ArrayList<>();
    private Lesson pairedDoubleSameLesson;
    private Lesson pairedDoubleDifferentLesson;
    private final String name;

    SchoolClass(NamedNodeMap attributes) {
        super(attributes);
        GetterById.addSchoolClassById(getId(), this);
        name = getStringValue("name");
    }

    int getPerWeek() {
        return lessons.size();
    }

    void addLesson(Lesson lesson) {
        int perWeek = lesson.getPerWeek();
        if (lesson.getLessonType() == LessonType.DOUBLE_SAME) {
            if (pairedDoubleSameLesson == null) {
                pairedDoubleSameLesson = lesson;
                return;
            }
        } else  if (lesson.getLessonType() == LessonType.DOUBLE_DIFFERENT) {
            if (pairedDoubleDifferentLesson == null) {
                pairedDoubleDifferentLesson = lesson;
                return;
            }
        }
        for (int i = 0; i < perWeek; i++) {
            lessons.add(lesson);
        }
    }

    List<Lesson> getLessons() {
        return lessons;
    }

    String getName() {
        return name;
    }

    Lesson getPairedDoubleSameLesson() {
        return pairedDoubleSameLesson;
    }

    Lesson getPairedDoubleDifferentLesson() {
        return pairedDoubleDifferentLesson;
    }

}

