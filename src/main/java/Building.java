import java.util.HashSet;
import java.util.Set;

public class Building {

    final Set<Subject> possibleSubjects = new HashSet<>();
    private final int id;

    public Building(int id) {
        this.id = id;
        GetterById.addBuildingById(id, this);
    }

    public void addSubject(Subject subject) {
        possibleSubjects.add(subject);
    }

    public boolean isPossibleSubject(Subject subject) {
        //System.out.println(possibleSubjects.size());
        return possibleSubjects.contains(subject);
    }

    public int getId() {
        return id;
    }

    public boolean isNotPossibleSubject(Subject subject) {
        return !isPossibleSubject(subject);
    }
}
