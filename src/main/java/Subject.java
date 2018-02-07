import org.w3c.dom.NamedNodeMap;

public class Subject extends TimetableXMLObject {
    /*
    Format:
    <subject id="E49460261EB6948A" name="Биология" short="Биол" partner_id=""/>
     */
    private final String shortName;
    private static final String SHORT_NAME = "short";

    public Subject(NamedNodeMap attributes) {
        super(attributes);
        GetterById.addSubjectById(getId(), this);
        shortName = getStringValue(SHORT_NAME);
    }

    public String getShortName() {
        return shortName;
    }
}
