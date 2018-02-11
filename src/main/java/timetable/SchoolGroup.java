package timetable;

import org.w3c.dom.NamedNodeMap;

import java.util.List;

class SchoolGroup extends TimetableXMLObject {
    /*
    Format:
    <group id="1F10BBFDB6A647A7" name="Entire class" classid="E06839D1C346DD93"
    studentids="" entireclass="1" divisiontag="0" studentcount=""/>
     */
    private final static String CLASS_ID = "classid";
    private final static String ENTIRE_CLASS = "entireclass";
    private final static String DIVISION_TAG = "divisiontag";
    private final boolean entireClass;
    private final int divisionTag;
    private final SchoolClass schoolClass;
    private SchoolGroup pairSchoolGroup;

    SchoolGroup(NamedNodeMap attributes) {
        super(attributes);
        String schoolClassId = getStringValue(CLASS_ID);
        schoolClass = GetterById.getSchoolClassById(schoolClassId);
        entireClass = getStringValue(ENTIRE_CLASS).equals("1");
        divisionTag = Integer.parseInt(getStringValue(DIVISION_TAG));
        schoolClass.addSchoolGroup(this);
        GetterById.addSchoolGroupById(getId(), this);
    }

    boolean isEntireClass() {
        return entireClass;
    }

    int getDivisionTag() {
        return divisionTag;
    }

    void setPairSchoolGroup(SchoolGroup schoolGroup) {
        this.pairSchoolGroup = schoolGroup;
    }

    SchoolGroup getPairSchoolGroup() {
        return pairSchoolGroup;
    }

    List<SchoolGroup> getSchoolGroupsOfSchoolClass() {
        return schoolClass.getSchoolGroups();
    }

    SchoolClass getSchoolClass() {
        return schoolClass;
    }
}
