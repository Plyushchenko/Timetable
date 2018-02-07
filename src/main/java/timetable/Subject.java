package timetable;

import org.w3c.dom.NamedNodeMap;

class Subject extends TimetableXMLObject {
    /*
    Format:
    <subject id="E49460261EB6948A" name="Биология" short="Биол" partner_id=""/>
     */
    private final String shortName;
    private final static String SHORT_NAME = "short";

    Subject(NamedNodeMap attributes) {
        super(attributes);
        GetterById.addSubjectById(getId(), this);
        shortName = getStringValue(SHORT_NAME);
    }

    String getShortName() {
        return shortName;
    }
}
