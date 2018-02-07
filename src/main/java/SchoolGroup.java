import org.w3c.dom.NamedNodeMap;
import sun.font.AttributeValues;

import java.util.Objects;

public class SchoolGroup extends TimetableXMLObject {
    /*
    Format:
    <group id="1F10BBFDB6A647A7" name="Entire class" classid="E06839D1C346DD93"
    studentids="" entireclass="1" divisiontag="0" studentcount=""/>
     */
    private static final String CLASS_ID = "classid";
    private static final String NAME = "name";
    private static final String ENTIRE_CLASS = "entireclass";
    private final SchoolClass schoolClass;
    private final String name;
    private final boolean entireClass;

    protected SchoolGroup(NamedNodeMap attributes) {
        super(attributes);
        String schoolClassId =  attributes.getNamedItem(CLASS_ID).getNodeValue();
        schoolClass = GetterById.getSchoolClassById(schoolClassId);
        schoolClass.addSchoolGroup(this);
        name = attributes.getNamedItem(NAME).getNodeValue();
        entireClass = attributes.getNamedItem(ENTIRE_CLASS).getNodeValue().equals("1");
        GetterById.addSchoolGroupById(getId(), this);
    }

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }

    public String getName() {
        return name;
    }

    public boolean isEntireClass() {
        return entireClass;
    }

    public boolean isNotEntireClass() {
        return !isEntireClass();
    }
}
