package timetable;

import java.util.HashSet;
import java.util.Set;

public class Building {

    private final Set<Subject> possibleSubjects = new HashSet<>();
    private final int id;

    Building(int id) {
        this.id = id;
        GetterById.addBuildingById(id, this);
    }

    void addSubject(Subject subject) {
        possibleSubjects.add(subject);
    }

    public int getId() {
        return id;
    }

    private boolean isPossibleSubject(Subject subject) {
        return possibleSubjects.contains(subject);
    }

    boolean isNotPossibleSubject(Subject subject) {
        return !isPossibleSubject(subject);
    }
}
