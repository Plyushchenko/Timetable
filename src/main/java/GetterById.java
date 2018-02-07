import java.util.HashMap;
import java.util.Map;

public class GetterById {
    private final static Map<String, SchoolClass> schoolClassById = new HashMap<>();
    private final static Map<String, Teacher> teacherById = new HashMap<>();
    private final static Map<String, Subject> subjectById = new HashMap<>();
    private final static Map<Integer, Building> buildingById = new HashMap<>();
    private final static Map<String, SchoolGroup> schoolGroupById = new HashMap<>();

    public static SchoolClass getSchoolClassById(String id) {
        return schoolClassById.get(id);
    }

    public static Teacher getTeacherById(String id) {
        return teacherById.get(id);
    }

    public static void addSchoolClassById(String id, SchoolClass schoolClass) {
        schoolClassById.put(id, schoolClass);
    }

    public static void addTeacherById(String id, Teacher teacher) {
        teacherById.put(id, teacher);
    }

    public static void addSubjectById(String id, Subject subject) {
        subjectById.put(id, subject);
    }

    public static Subject getSubjectById(String id) {
        return subjectById.get(id);
    }

    public static void addBuildingById(int id, Building building) {
        buildingById.put(id, building);
    }

    public static Building getBuildingById(int id) {
        return buildingById.get(id);
    }

    public static SchoolGroup getSchoolGroupById(String groupId) {
        return schoolGroupById.get(groupId);
    }

    public static void addSchoolGroupById(String groupId, SchoolGroup schoolGroup) {
        schoolGroupById.put(groupId, schoolGroup);
    }
}
