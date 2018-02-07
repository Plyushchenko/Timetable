package timetable;

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
    final static Path RESOURCES_PATH
            = Paths.get("src", "main", "resources");
    private final static Path TIMETABLE_XML_PATH = Paths.get(RESOURCES_PATH.toString(), "timetable.xml");
    private final static String TIMETABLE_XML_PATH_AS_STRING = TIMETABLE_XML_PATH.toString();
    private final static File TIMETABLE_XML_FILE = new File(TIMETABLE_XML_PATH_AS_STRING);
    private final static String SUBJECTS = "subjects";
    private final static String CLASSES = "classes";
    private final static String GROUPS = "groups";
    private final static String LESSONS = "lessons";
    private final static String TEACHERS = "teachers";
    private final static Set<String> REQUIRED_DATA = Stream.of(
            SUBJECTS,
            CLASSES,
            GROUPS,
            LESSONS,
            TEACHERS
    ).collect(toSet());
    private final static List<Subject> subjects = new ArrayList<>();
    private final static List<SchoolClass> schoolClasses = new ArrayList<>();
    private static final double TEMPERATURE = 2000000;
    private static final double COOLING_RATE = 0.99;

    public static void main(String[] args) {
        try {
            extractRequiredInformation(TIMETABLE_XML_FILE);
            createBuildings(subjects);
            Timetable timetable = new Timetable(schoolClasses).simulatedAnnealing(TEMPERATURE,
                    COOLING_RATE);
            timetable.print();
            System.out.println("PENALTY IS " + timetable.evaluatePenalty());
            System.out.println("DONE");

        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private static void extractRequiredInformation(File xmlFile) throws IOException,
            SAXException, ParserConfigurationException {
        Node rootNode = getRootNode(xmlFile);
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
