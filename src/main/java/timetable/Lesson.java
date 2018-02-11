package timetable;

import javafx.util.Pair;
import org.w3c.dom.NamedNodeMap;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class Lesson extends TimetableXMLObject {
    /*
    Format:
     <lesson id="C0029B0B2F9BDF59" classids="9A5AFE9EF73732AA" subjectid="E5AF72819DFF5192"
     periodspercard="1" periodsperweek="2.0" teacherids="FDF1633E7761AC0F"
     classroomids="" groupids="02E1D1C53DE4F814" capacity="*" seminargroup=""
     termsdefid="6E9537C4E45BEDBD" weeksdefid="73E75C404FFBAFCD"
     daysdefid="C6200ECA844AAE66" partner_id=""/>
     */
    /* Only one teacher for the subject which class should divide for */
    private final static Set<String> SPECIAL_SCHOOL_CLASSES = Stream.of(
            "8F23B6E97D96A8FD",
            "5F10BDB4B4D7D565",
            "0421921F1F79DD82",
            "B94BAD5B15B42CF1",
            "50E1056D97BC107B",
            "559F3D0D3337E57D",
            "E0845F9C59BB47AD"
    ).collect(toSet());
    private final static Pair<String, String> SPECIAL_SCHOOL_CLASS_WITH_SUBJECT
            = new Pair<>("A946F8BE37565A6D", "FDF98D50C526934D");
    private final static String TEACHER_ID = "teacherids";
    private final static String CLASS_ID = "classids";
    private final static String GROUP_ID = "groupids";
    private final static String SUBJECT_ID = "subjectid";
    final static String ENGLISH_ID = "127E27B14174157A";
    private final static String PER_WEEK = "periodsperweek";
    private final Teacher teacher;
    private final int perWeek;
    private final String subjectName;
    private final Subject subject;
    private final SchoolClass schoolClass;
    private SchoolGroup schoolGroup;
    private final boolean entireClass;

    Lesson(NamedNodeMap attributes) {
        super(attributes);
        String teacherId = attributes.getNamedItem(TEACHER_ID).getNodeValue();
        teacher = GetterById.getTeacherById(teacherId);
        String classId = getStringValue(CLASS_ID);
        schoolClass = GetterById.getSchoolClassById(classId);
        String groupId = getStringValue(GROUP_ID);
        schoolGroup = GetterById.getSchoolGroupById(groupId);
        String subjectId = getStringValue(SUBJECT_ID);
        subject = GetterById.getSubjectById(subjectId);
        subjectName = subject.getShortName();
        perWeek = (int)Double.parseDouble(getStringValue(PER_WEEK));
        entireClass = schoolGroup.isEntireClass();
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

    Subject getSubject() {
        return subject;
    }

    SchoolGroup getSchoolGroup() {
        return schoolGroup;
    }

    public boolean isEntireClass() {
        return entireClass;
    }

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }
}
