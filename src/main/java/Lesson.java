import javafx.util.Pair;
import org.w3c.dom.NamedNodeMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lesson extends TimetableXMLObject {
    /*
    Format:
     <lesson id="C0029B0B2F9BDF59" classids="9A5AFE9EF73732AA" subjectid="E5AF72819DFF5192" periodspercard="1"
     periodsperweek="2.0" teacherids="FDF1633E7761AC0F" classroomids="" groupids="02E1D1C53DE4F814"
     capacity="*" seminargroup="" termsdefid="6E9537C4E45BEDBD" weeksdefid="73E75C404FFBAFCD" daysdefid="C6200ECA844AAE66" partner_id=""/>
     */
    private static final String TEACHER_ID = "teacherids";
    private static final String CLASS_ID = "classids";
    private static final String GROUP_ID = "groupids";
    private static final String SUBJECT_ID = "subjectid";
    private static final String ENGLISH_ID = "127E27B14174157A";
    private static final String PER_WEEK = "periodsperweek";
    private final Teacher teacher;
    private final int perWeek;
    private final LessonType lessonType;
    private final String subjectName;
    private final Subject subject;

    public Lesson(NamedNodeMap attributes) {
        super(attributes);
        String teacherId = attributes.getNamedItem(TEACHER_ID).getNodeValue();
        teacher = GetterById.getTeacherById(teacherId);
        String classId = getStringValue(CLASS_ID);
        SchoolClass schoolClass = GetterById.getSchoolClassById(classId);
        String groupId = getStringValue(GROUP_ID);
        SchoolGroup schoolGroup = GetterById.getSchoolGroupById(groupId);
        String subjectId = getStringValue(SUBJECT_ID);
        subject = GetterById.getSubjectById(subjectId);
        subjectName = subject.getShortName();
        perWeek = (int)Double.parseDouble(getStringValue(PER_WEEK));
        if (subjectId.equals(ENGLISH_ID)) {
            lessonType = LessonType.DOUBLE_SAME;
        } else if (schoolGroup.isNotEntireClass()) {
            lessonType = LessonType.DOUBLE_DIFFERENT;
        } else {
            lessonType = LessonType.SINGLE;
        }
        schoolClass.addLesson(this);
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public int getPerWeek() {
        return perWeek;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public LessonType getLessonType() {
        return lessonType;
    }

    public Subject getSubject() {
        return subject;
    }
}
