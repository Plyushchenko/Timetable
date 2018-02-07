package timetable;

import org.w3c.dom.NamedNodeMap;

public abstract class TimetableXMLObject {
    private final static String ID = "id";
    final static String NAME = "name";
    private final String id;
    private final NamedNodeMap attributes;

    TimetableXMLObject(NamedNodeMap attributes) {
        this.attributes = attributes;
        id = getStringValue(ID);
    }

    public String getId() {
        return id;
    }

    String getStringValue(String namedItem) {
        return attributes.getNamedItem(namedItem).getNodeValue();
    }
}
