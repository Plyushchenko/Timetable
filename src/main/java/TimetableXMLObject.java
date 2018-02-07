import org.w3c.dom.NamedNodeMap;

public abstract class TimetableXMLObject {
    public static final String ID = "id";
    private final String id;
    protected final NamedNodeMap attributes;

    protected TimetableXMLObject(NamedNodeMap attributes) {
        this.attributes = attributes;
        id = getStringValue(ID);
    }

    public String getId() {
        return id;
    }

    protected String getStringValue(String namedItem) {
        return attributes.getNamedItem(namedItem).getNodeValue();
    }
}
