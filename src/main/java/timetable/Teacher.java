package timetable;

import org.w3c.dom.NamedNodeMap;

class Teacher extends TimetableXMLObject {
    /*
    Format:
    <teacher id="E8755AC1BC56DB7C" firstname="Анна Анатольевна" lastname="Агафонова"
    name="Агафонова Анна Анатольевна" short="Агафонова AA" gender="F" color="#FF0000"
    email="" mobile="" partner_id=""/>
     */

    private final static String LAST_NAME = "lastname";
    private final String name;
    private final String lastName;

    Teacher(NamedNodeMap attributes) {
        super(attributes);
        GetterById.addTeacherById(getId(), this);
        name = getStringValue(NAME);
        lastName = getStringValue(LAST_NAME);

    }

    String getName() {
        return name;
    }

    String getLastName() {
        return lastName;
    }
}
