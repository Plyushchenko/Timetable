package timetable;

import org.w3c.dom.NamedNodeMap;

import java.util.*;

class SchoolClass extends TimetableXMLObject {
    /*
    Format:
    <class id="E06839D1C346DD93" name="5-а" short="5-а" teacherid="" classroomids="" grade="5"
    partner_id=""/>
    */
    private final List<Lesson> lessons = new ArrayList<>();
    private final List<SchoolGroup> schoolGroups = new ArrayList<>();
    private final String name;

    SchoolClass(NamedNodeMap attributes) {
        super(attributes);
        GetterById.addSchoolClassById(getId(), this);
        name = getStringValue(NAME);
    }

    void addLesson(Lesson lesson) {
        for (int i = 0; i < lesson.getPerWeek(); i++) {
            lessons.add(lesson);
        }
    }

    List<Lesson> getLessons() {
        return lessons;
    }

    String getName() {
        return name;
    }

    void addSchoolGroup(SchoolGroup schoolGroup) {
        int divisionTag = schoolGroup.getDivisionTag();
        for (SchoolGroup current : schoolGroups) {
            if (current.getDivisionTag() == divisionTag && divisionTag != 0) {
                schoolGroup.setPairSchoolGroup(current);
                current.setPairSchoolGroup(schoolGroup);
                break;
            }
        }
        schoolGroups.add(schoolGroup);
    }

    List<SchoolGroup> getSchoolGroups() {
        return schoolGroups;
    }
}

