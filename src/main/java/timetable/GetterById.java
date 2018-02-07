package timetable;

import java.util.HashMap;
import java.util.Map;

class GetterById {
    private final static Map<String, SchoolClass> schoolClassById = new HashMap<>();
    private final static Map<String, Teacher> teacherById = new HashMap<>();
    private final static Map<String, Subject> subjectById = new HashMap<>();
    private final static Map<Integer, Building> buildingById = new HashMap<>();
    private final static Map<String, SchoolGroup> schoolGroupById = new HashMap<>();

    static SchoolClass getSchoolClassById(String id) {
        return schoolClassById.get(id);
    }

    static void addSchoolClassById(String id, SchoolClass schoolClass) {
        schoolClassById.put(id, schoolClass);
    }

    static Teacher getTeacherById(String id) {
        return teacherById.get(id);
    }

    static void addTeacherById(String id, Teacher teacher) {
        teacherById.put(id, teacher);
    }

    static Subject getSubjectById(String id) {
        return subjectById.get(id);
    }

    static void addSubjectById(String id, Subject subject) {
        subjectById.put(id, subject);
    }

    static Building getBuildingById(int id) {
        return buildingById.get(id);
    }

    static void addBuildingById(int id, Building building) {
        buildingById.put(id, building);
    }

    static SchoolGroup getSchoolGroupById(String groupId) {
        return schoolGroupById.get(groupId);
    }

    static void addSchoolGroupById(String groupId, SchoolGroup schoolGroup) {
        schoolGroupById.put(groupId, schoolGroup);
    }
}
