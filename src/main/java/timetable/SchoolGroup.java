package timetable;

import org.w3c.dom.NamedNodeMap;

class SchoolGroup extends TimetableXMLObject {
    /*
    Format:
    <group id="1F10BBFDB6A647A7" name="Entire class" classid="E06839D1C346DD93"
    studentids="" entireclass="1" divisiontag="0" studentcount=""/>
     */
    private final static String CLASS_ID = "classid";
    private final static String NAME = "name";
    private final static String ENTIRE_CLASS = "entireclass";
    private final boolean entireClass;

    SchoolGroup(NamedNodeMap attributes) {
        super(attributes);
        String schoolClassId = getStringValue(CLASS_ID);
        SchoolClass schoolClass = GetterById.getSchoolClassById(schoolClassId);
        entireClass = getStringValue(ENTIRE_CLASS).equals("1");
        GetterById.addSchoolGroupById(getId(), this);
    }

    private boolean isEntireClass() {
        return entireClass;
    }

    boolean isNotEntireClass() {
        return !isEntireClass();
    }
}
