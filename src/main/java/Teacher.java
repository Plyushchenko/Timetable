import javafx.util.Pair;
import org.w3c.dom.NamedNodeMap;

import java.util.HashSet;
import java.util.Set;

public class Teacher extends TimetableXMLObject {
    /*
    Format:
    <teacher id="E8755AC1BC56DB7C" firstname="Анна Анатольевна" lastname="Агафонова"
    name="Агафонова Анна Анатольевна" short="Агафонова AA" gender="F" color="#FF0000"
    email="" mobile="" partner_id=""/>
     */
    private final static String NAME = "name";
    private final static String LAST_NAME = "lastname";
    private final String name;
    private final String lastName;

    public Teacher(NamedNodeMap attributes) {
        super(attributes);
        GetterById.addTeacherById(getId(), this);
        name = getStringValue(NAME);
        lastName = getStringValue(LAST_NAME);

    }

    /*public void addPossibleLesson(Lesson lesson) {
        possibleLessons.add(lesson);
    }
*/
    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }
}
