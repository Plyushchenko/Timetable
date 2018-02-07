import javafx.util.Pair;
import org.w3c.dom.NamedNodeMap;
import sun.font.AttributeValues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchoolClass extends TimetableXMLObject {
    /*
    Format:
    <class id="E06839D1C346DD93" name="5-а" short="5-а" teacherid="" classroomids="" grade="5" partner_id=""/>
     */
    private final List<Lesson> lessons = new ArrayList<>();
    private final List<SchoolGroup> schoolGroups = new ArrayList<>();
    private final List<Lesson> differentLessonsByGroups = new ArrayList<>();
    private Lesson pairedDoubleSameLesson;
    private Lesson pairedDoubleDifferentLesson;
    private final static String NAME = "name";
    private final String name;

    protected SchoolClass(NamedNodeMap attributes) {
        super(attributes);
        GetterById.addSchoolClassById(getId(), this);
        name = getStringValue("name");
    }

    public int getPerWeek() {
        return lessons.size();
    }

    public void addSchoolGroup(SchoolGroup schoolGroup) {
        schoolGroups.add(schoolGroup);
    }


    public void addLesson(Lesson lesson) {
        int perWeek = lesson.getPerWeek();
        if (lesson.getLessonType() == LessonType.DOUBLE_SAME) {
            if (pairedDoubleSameLesson == null) {
                pairedDoubleSameLesson = lesson;
                return;
            }
        } else  if (lesson.getLessonType() == LessonType.DOUBLE_DIFFERENT) {
            if (pairedDoubleDifferentLesson == null ||
                    pairedDoubleDifferentLesson.getSubjectName().equals(lesson.getSubjectName())) {
                pairedDoubleDifferentLesson = lesson;
                return;
            }
        }
        for (int i = 0; i < perWeek; i++) {
            lessons.add(lesson);
        }
    }

    public List<SchoolGroup> getSchoolGroups() {
        return schoolGroups;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public String getName() {
        return name;
    }

    public Lesson getPairedDoubleSameLesson() {
        return pairedDoubleSameLesson;
    }

    public Lesson getPairedDoubleDifferentLesson() {
        return pairedDoubleDifferentLesson;
    }

}

