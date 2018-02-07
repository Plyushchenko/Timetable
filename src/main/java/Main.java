import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class Main {
    public static final Path TIMETABLE_XML_PATH
            = Paths.get("src", "main", "resources", "timetable.xml");
    public static final String TIMETABLE_XML_PATH_AS_STRING = TIMETABLE_XML_PATH.toString();
    public static final File TIMETABLE_XML_FILE = new File(TIMETABLE_XML_PATH_AS_STRING);
    private static final String SUBJECTS = "subjects";
    private static final String CLASSES = "classes";
    private static final String GROUPS = "groups";
    private static final String LESSONS = "lessons";
    private static final String TEACHERS = "teachers";
    private static final Set<String> REQUIRED_DATA = Stream.of(
            SUBJECTS,
            CLASSES,
            GROUPS,
            LESSONS,
            TEACHERS
    ).collect(toSet());

    public static void main(String[] args) {
        /*while(true) {*/
        List<Subject> subjects = new ArrayList<>();
        List<SchoolClass> schoolClasses = new ArrayList<>();
        try {
            Node rootNode = getRootNode(TIMETABLE_XML_FILE);
            NodeList rootChildren = rootNode.getChildNodes();
            for (int i = 0; i < rootChildren.getLength(); i++) {
                Node child = rootChildren.item(i);
                String childName = child.getNodeName();
                if (REQUIRED_DATA.contains(childName)) {
                    List<NamedNodeMap> attributes = extractChildrenAttributes(child);
                    for (NamedNodeMap currentAttributes: attributes) {
                        switch (childName) {
                            case GROUPS:
                                new SchoolGroup(currentAttributes);
                                break;
                            case SUBJECTS:
                                subjects.add(new Subject(currentAttributes));
                                break;
                            case CLASSES:
                                schoolClasses.add(new SchoolClass(currentAttributes));
                                break;
                            case LESSONS:
                                new Lesson(currentAttributes);
                                break;
                            case TEACHERS:
                                new Teacher(currentAttributes);
                                break;
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
        }
        createBuildings(subjects);
        long a = System.currentTimeMillis();
        Timetable timetable = new Timetable(schoolClasses);
        //System.out.println(timetable.evaluatePenalty());
        for (int i = 0; i < 10; i++) {
            timetable = timetable.simulatedAnnealing(20000, 0.99);
            if (timetable.evaluatePenalty() == 0) { ;
                timetable.print();
                System.out.println("PENALTY IS " + timetable.evaluatePenalty());
                System.out.println("DONE");
                return;
            }
        }
        //timetable.generateNeighbours();
        long b = System.currentTimeMillis();
        System.out.println((b - a) +  "ms");
        /*}*/
    }


    private static void createBuildings(List<Subject> subjects)  {
        Set<Subject> forbiddenSubjectsForSecondBuilding = Stream.of(
                "E1376672B6D4C7D2",
                "FDF98D50C526934D"
        ).map(GetterById::getSubjectById).collect(toSet());
        Building firstBuilding = new Building(1);
        Building secondBuilding = new Building(2);
        for (Subject subject: subjects) {
            firstBuilding.addSubject(subject);
            if (!forbiddenSubjectsForSecondBuilding.contains(subject)) {
                secondBuilding.addSubject(subject);
            } else {
                System.out.println(subject.getShortName());
            }
        }
    }

    private static List<NamedNodeMap> extractChildrenAttributes(Node node) {
        NodeList children = node.getChildNodes();
        List<NamedNodeMap> result = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            NamedNodeMap attributes = child.getAttributes();
            if (attributes == null) {
                continue;
            }
            result.add(attributes);
        }
        return result;
    }

    private static Node getRootNode(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        return document.getChildNodes().item(0);
    }
}
