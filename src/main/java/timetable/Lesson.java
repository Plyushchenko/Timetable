package timetable;

import org.w3c.dom.NamedNodeMap;

public class Lesson extends TimetableXMLObject {
    /*
    Format:
     <lesson id="C0029B0B2F9BDF59" classids="9A5AFE9EF73732AA" subjectid="E5AF72819DFF5192" periodspercard="1"
     periodsperweek="2.0" teacherids="FDF1633E7761AC0F" classroomids="" groupids="02E1D1C53DE4F814"
     capacity="*" seminargroup="" termsdefid="6E9537C4E45BEDBD" weeksdefid="73E75C404FFBAFCD" daysdefid="C6200ECA844AAE66" partner_id=""/>
     */
    private final static String TEACHER_ID = "teacherids";
    private final static String CLASS_ID = "classids";
    private final static String GROUP_ID = "groupids";
    private final static String SUBJECT_ID = "subjectid";
    private final static String ENGLISH_ID = "127E27B14174157A";
    private final static String PER_WEEK = "periodsperweek";
    private final Teacher teacher;
    private final int perWeek;
    private final LessonType lessonType;
    private final String subjectName;
    private final Subject subject;

    Lesson(NamedNodeMap attributes) {
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

    Teacher getTeacher() {
        return teacher;
    }

    int getPerWeek() {
        return perWeek;
    }

    String getSubjectName() {
        return subjectName;
    }

    LessonType getLessonType() {
        return lessonType;
    }

    Subject getSubject() {
        return subject;
    }
}
