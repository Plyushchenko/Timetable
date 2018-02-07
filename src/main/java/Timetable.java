import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Timetable {
    private static final Set<String> onlySecondBuildingTeachersLastNames = Stream.of(
            "Петрова",
            "Проценко",
            "Болдырева",
            "Елисеева",
            "Куликова",
            "Севастьянова",
            "Семенова",
            "Ларионова",
            "Колонистова",
            "Краснова",
            "Бикташева",
            "Погорелова",
            "Баранова",
            "Комарова",
            "Григорьева",
            "Попов",
            "Умеров"
    ).collect(Collectors.toSet());
    private static final int PENALTY_CLASS_AT_FIRST_BUILDING = 10;
    private static final int PENALTY_TEACHER_AT_FIRST_BUILDING = 10;
    private static final int PENALTY_TEACHER_HAS_MORE_THAN_ONE_LESSON_AT_ONCE = 25;
    private static final int PENALTY_WRONG_BUILDING = 50;
    private static final int PENALTY_OPENING = 2;
    private static final int PENALTY_TEACHER_CHANGES_BUILDING_WITHOUT_PAUSE = 10;
    private static final int PENALTY_TOO_MANY_CHANGES_PER_DAY = 25;
    private static final int PENALTY_TOO_MANY_CHANGES_PER_WEEK = 10;
    private static final int ALLOWED_CHANGES_PER_DAY = 1;
    private static final int ALLOWED_CHANGES_PER_WEEK = 2;
    private final Map<Pair<DayTimeSlot, SchoolClass>, Lesson> lessonByTime;
    private final Map<Pair<Integer, SchoolClass>, Building> buildingByDay;
    private final List<SchoolClass> schoolClasses;
    private final Set<SchoolClass> onlySecondBuildingSchoolClasses;
    private final Random random = new Random(System.currentTimeMillis());
    boolean xxx = false;

    public Timetable(List<SchoolClass> schoolClasses) {
        this.schoolClasses = schoolClasses;
        lessonByTime = new HashMap<>();
        buildingByDay = new HashMap<>();
        onlySecondBuildingSchoolClasses = new HashSet<>();
        for (SchoolClass schoolClass: schoolClasses) {
            Collections.shuffle(schoolClass.getLessons());
            int perWeek = schoolClass.getPerWeek();
            List<Integer> numberOfLessonsByDay = splitNumber(perWeek, DayTimeSlot.DAYS);
            for (int i = 0, cur = 0; i < DayTimeSlot.DAYS; i++) {
                for (int j = 0; j < numberOfLessonsByDay.get(i); j++, cur++) {
                    lessonByTime.put(
                            new Pair<>(DayTimeSlot.slotByDayAndTime.get(i).get(j), schoolClass),
                            schoolClass.getLessons().get(cur)
                    );
                }
            }
        }
        for (SchoolClass schoolClass: schoolClasses) {
            String name = schoolClass.getName();
            boolean onlySecondBuilding = name.startsWith("5") || name.startsWith("6") || name.startsWith("7");
            if (onlySecondBuilding) {
                onlySecondBuildingSchoolClasses.add(schoolClass);
                System.out.println(name);
                System.out.println(schoolClass.getPairedDoubleDifferentLesson().getSubjectName());
                System.out.println(schoolClass.getPairedDoubleSameLesson().getSubjectName());
            }
            for (int i = 0; i < DayTimeSlot.DAYS; i++) {
                Pair<Integer, SchoolClass> current = new Pair<>(i, schoolClass);
                int buildingId;
                if (onlySecondBuilding) {
                    buildingId = 2;
                } else {
                    buildingId = random.nextInt(2) + 1;
                }
                Building building = GetterById.getBuildingById(buildingId);
                buildingByDay.put(current, building);
            }
        }
    }

    public Timetable(List<SchoolClass> schoolClasses,
                     Map<Pair<DayTimeSlot, SchoolClass>, Lesson> lessonByTime,
                     Map<Pair<Integer, SchoolClass>, Building> buildingByDay,
                     Set<SchoolClass> onlySecondBuildingSchoolClasses) {
        this.schoolClasses = schoolClasses;
        this.lessonByTime = lessonByTime;
        this.buildingByDay = buildingByDay;
        this.onlySecondBuildingSchoolClasses = onlySecondBuildingSchoolClasses;
    }

    private static List<Integer> splitNumber(int n, int m) {
        Random random = new Random();
        while (true) {
            int sum = 0;
            List<Integer> result = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int x = random.nextInt(4) + 5;
                sum += x;
                result.add(x);
            }
            if (sum == n && result.size() == m)
                return result;
        }
    }

    public int evaluatePenalty() {
        int penalty = 0;
        Map<Teacher, List<List<Integer>>> buildingsOfTeachers = new HashMap<>();
        for (int i = 0; i < DayTimeSlot.DAYS; i++) {
            for (int j = 0; j < DayTimeSlot.LESSONS; j++) {
                Map<Teacher, Set<Lesson>> lessonsOfTeachers = new HashMap<>();
                for (SchoolClass schoolClass : schoolClasses) {
                    Lesson lesson = lessonByTime.get(
                            new Pair<>(DayTimeSlot.slotByDayAndTime.get(i).get(j), schoolClass)
                    );
                    if (lesson == null) {
                        continue;
                    }
                    penalty += evaluatePenalty(i, j, schoolClass, lesson,
                            buildingsOfTeachers, lessonsOfTeachers);
                    if (lesson.getLessonType() == LessonType.DOUBLE_SAME &&
                            !schoolClass.getPairedDoubleSameLesson().equals(lesson)) {
                        penalty += evaluatePenalty(i, j, schoolClass,
                                schoolClass.getPairedDoubleSameLesson(),
                                buildingsOfTeachers, lessonsOfTeachers);
                    }
                    if (lesson.getLessonType() == LessonType.DOUBLE_DIFFERENT &&
                            !schoolClass.getPairedDoubleDifferentLesson().equals(lesson)) {
                        penalty += evaluatePenalty(i, j, schoolClass,
                                schoolClass.getPairedDoubleDifferentLesson(),
                                buildingsOfTeachers, lessonsOfTeachers);
                    }
                }
                for (Set<Lesson> lessonsOfTeacher : lessonsOfTeachers.values()) {
                    penalty += (lessonsOfTeacher.size() - 1) *
                            PENALTY_TEACHER_HAS_MORE_THAN_ONE_LESSON_AT_ONCE;
                }
            }
            for (SchoolClass schoolClass : schoolClasses) {
                List<Boolean> isLesson = new ArrayList<>();
                for (int j = 0; j < DayTimeSlot.LESSONS; j++) {
                    Lesson lesson = lessonByTime.get(
                            new Pair<>(DayTimeSlot.slotByDayAndTime.get(i).get(j), schoolClass)
                    );
                    isLesson.add (lesson != null);
                }
                penalty += evaluatePenalty(isLesson);

                if (onlySecondBuildingSchoolClasses.contains(schoolClass)
                        && buildingByDay.get(new Pair<>(i, schoolClass)).getId() != 2) {
                    penalty += PENALTY_CLASS_AT_FIRST_BUILDING;
                }
            }
        }
        for (Teacher teacher: buildingsOfTeachers.keySet()) {
            penalty += evaluatePenaltyOfBuildingChange(buildingsOfTeachers.get(teacher),
                    onlySecondBuildingTeachersLastNames.contains(teacher.getLastName()));
        }
        return penalty;
    }

    private int evaluatePenaltyOfBuildingChange(List<List<Integer>> buildingsOfTeacher,
                                                boolean onlySecondBuilding) {
        int penalty = 0;
        int changesPerWeek = 0;
        for (List<Integer> current: buildingsOfTeacher) {
            int changesPerDay = 0;
            int l = 0;
            while (l < current.size() && current.get(l) == 0) {
                l++;
            }
            int r = current.size() - 1;
            while (r >= l && current.get(r) == 0) {
                r--;
            }
            if (l >= current.size()) {
                continue;
            }
            int last = current.get(l);
            int pos = l;
            for (int i = l + 1; i <= r; i++) {
                int x = current.get(i);
                if (onlySecondBuilding && x != 2) {
                    penalty += PENALTY_TEACHER_AT_FIRST_BUILDING;
                }
                if (x == 0) {
                    continue;
                }
                if (x != last) {
                    changesPerDay++;
                    pos = i;
                    last = x;
                    if (i <= pos + 1) {
                        penalty += PENALTY_TEACHER_CHANGES_BUILDING_WITHOUT_PAUSE;
                    }
                }
            }
            if (changesPerDay > ALLOWED_CHANGES_PER_DAY) {
                penalty += PENALTY_TOO_MANY_CHANGES_PER_DAY * (changesPerDay - ALLOWED_CHANGES_PER_DAY);
            }
            changesPerWeek += changesPerDay;
        }
        if (changesPerWeek > ALLOWED_CHANGES_PER_WEEK) {
            penalty += PENALTY_TOO_MANY_CHANGES_PER_WEEK * (changesPerWeek - ALLOWED_CHANGES_PER_WEEK);
        }
        //System.out.println(penalty);
        return penalty;
    }

    private int evaluatePenalty(List<Boolean> isLesson) {
        int penalty = 0;
        int l = 0;
        while (l < isLesson.size() && !isLesson.get(l)) {
            l++;
        }
        int r = isLesson.size() - 1;
        while (r >= l && !isLesson.get(r)) {
            r--;
        }
        for (int i = l; i <= r; i++) {
            if (!isLesson.get(i)) {
                penalty += PENALTY_OPENING;
            }
        }
        return penalty;
    }

    private int evaluatePenalty(int i, int j, SchoolClass schoolClass, Lesson lesson,
                                Map<Teacher, List<List<Integer>>> buildingsOfTeachers,
                                Map<Teacher, Set<Lesson>> lessonsOfTeachers) {
        Teacher teacher = lesson.getTeacher();
        Building building = buildingByDay.get(new Pair<>(i, schoolClass));
        int buildingId = building.getId();
        lessonsOfTeachers.putIfAbsent(teacher, new HashSet<>());
        lessonsOfTeachers.get(teacher).add(lesson);
        List<List<Integer>> buildingIds = new ArrayList<>();
        for (int k = 0; k < DayTimeSlot.DAYS; k++) {
            List<Integer> kthDay = new ArrayList<>();
            for (int q = 0; q < DayTimeSlot.LESSONS; q++) {
                kthDay.add(0);
            }
            buildingIds.add(kthDay);
        }
        buildingsOfTeachers.putIfAbsent(teacher, buildingIds);
        buildingsOfTeachers.get(teacher).get(i).set(j, buildingId);
        Subject subject = lesson.getSubject();
        if (building.isNotPossibleSubject(subject)) {
            return PENALTY_WRONG_BUILDING;
        }
        return 0;
    }

    private void swapLessons() {
              for (SchoolClass schoolClass: schoolClasses) {
            for (int i = 0; i < DayTimeSlot.DAYS; i++) {
                for (int j = 0; j < DayTimeSlot.LESSONS; j++) {
                    for (int k = i; k  < DayTimeSlot.DAYS; k++) {
                        for (int q = (i == k) ? j + 1 : 0; q < DayTimeSlot.LESSONS; q++) {
                            swapLessons(schoolClass, i, j, k, q);
                        }
                    }
                }
            }
        }
    }

    private Timetable swapLessons(SchoolClass schoolClass, int i, int j, int k, int q) {
        Map<Pair<DayTimeSlot, SchoolClass>, Lesson> modifiedLessonByTime = new HashMap<>();
        lessonByTime.forEach(modifiedLessonByTime::put);
        Pair<DayTimeSlot, SchoolClass> p1 = new Pair<>(
                DayTimeSlot.slotByDayAndTime.get(i).get(j),
                schoolClass
        );
        Pair<DayTimeSlot, SchoolClass> p2 = new Pair<>(
                DayTimeSlot.slotByDayAndTime.get(k).get(q),
                schoolClass
        );
        Lesson l1 = lessonByTime.get(p1);
        Lesson l2 = lessonByTime.get(p2);
        modifiedLessonByTime.put(p1, l2);
        modifiedLessonByTime.put(p2, l1);
        Timetable modifiedTimetable = new Timetable(
                schoolClasses,
                modifiedLessonByTime,
                buildingByDay,
                onlySecondBuildingSchoolClasses
        );
        return modifiedTimetable;
    }

    private void changeBuildings() {
        for (SchoolClass schoolClass: schoolClasses) {
            for (int i = 0; i < DayTimeSlot.DAYS; i++) {
                changeBuildings(schoolClass, i);
            }
        }
    }

    private Timetable changeBuildings(SchoolClass schoolClass, int i) {
        Pair<Integer, SchoolClass> current = new Pair<>(i, schoolClass);
        Building building = buildingByDay.get(current);
        int buildingId = 3 - building.getId();
        building = GetterById.getBuildingById(buildingId);
        Map<Pair<Integer, SchoolClass>, Building> modifiedBuildingByDay = new HashMap<>();
        buildingByDay.forEach(modifiedBuildingByDay::put);
        modifiedBuildingByDay.put(current, building);
        Timetable modifiedTimetable = new Timetable(
                schoolClasses,
                lessonByTime,
                modifiedBuildingByDay,
                onlySecondBuildingSchoolClasses
        );
        return modifiedTimetable;
    }

    public Timetable simulatedAnnealing(double temperature, double coolingRate) {
        Timetable currentTimetable = this;
        Timetable bestTimetable = currentTimetable;
        int bestPenalty = evaluatePenalty();
        int goal = (int)((double)(bestPenalty * 2) / 5);
        for (long i = 0; i < 50000; i++) {
            if (i % 1000 == 0) {
                System.out.print(i + " ");
                if (i % 10000 == 0)
                    System.out.println();
                if (i % 2500 == 0)
                    System.out.println(temperature + " " + coolingRate);
            }
            Timetable neighbourTimetable = currentTimetable.generateNeighbour();
            int neighbourPenalty = neighbourTimetable.evaluatePenalty();
            int currentPenalty = currentTimetable.evaluatePenalty();
            if (neighbourPenalty < currentPenalty) {
                currentTimetable = neighbourTimetable;
                currentPenalty = neighbourPenalty;
            } else if ((neighbourPenalty - currentPenalty) / temperature < random.nextDouble()) {
                currentTimetable = neighbourTimetable;
                currentPenalty = neighbourPenalty;
            }
            //System.out.println(currentPenalty + " " + neighbourPenalty + " " + bestPenalty);
            if (currentPenalty < bestPenalty) {
                bestTimetable = currentTimetable;
                bestPenalty = currentPenalty;
                System.out.println(" BEST " + bestPenalty);
            }
            temperature *= coolingRate;
            //System.out.println(temperature);
        }
        //System.out.println(bestPenalty);
        return bestTimetable;
    }

    private Timetable generateNeighbour() {
        Timetable neighbourTimetable;
        int schoolClassIndex = random.nextInt(schoolClasses.size());
        SchoolClass schoolClass = schoolClasses.get(schoolClassIndex);
        int dayNumber = random.nextInt(DayTimeSlot.DAYS);
        if (random.nextBoolean()) {
            neighbourTimetable = changeBuildings(schoolClass, dayNumber);
        } else {
            int lessonNumber = random.nextInt(DayTimeSlot.LESSONS);
            int dayNumber2 = random.nextInt(DayTimeSlot.DAYS);
            int lessonNumber2 = random.nextInt(DayTimeSlot.LESSONS);
            neighbourTimetable = swapLessons(schoolClass, dayNumber, lessonNumber, dayNumber2, lessonNumber2);
        }
        return neighbourTimetable;
    }

    //TODO XML и парные уроки
    public void print() {
        for (int i = 0; i < 6; i++) {
            for (SchoolClass schoolClass: schoolClasses) {
                System.out.println("CLASS IS " + schoolClass.getName() + ", DAY = " + i + ", BUILDING = " + buildingByDay.get(new Pair<>(i, schoolClass)).getId());
                for (int j = 0; j < 8; j++) {
                    Lesson lesson = lessonByTime.get(new Pair<>(DayTimeSlot.slotByDayAndTime.get(i).get(j), schoolClass));
                    if (lesson == null) {
                        System.out.println("LESSON #" + j + " NULL");
                        continue;
                    }
                    String a = lesson.getSubjectName();
                    String b = lesson.getTeacher().getName();
                    System.out.println("LESSON #" + j + " " + a + " " + b);
                    if (lesson.getLessonType() == LessonType.DOUBLE_SAME
                            && !schoolClass.getPairedDoubleSameLesson().equals(lesson)) {
                        Lesson t = schoolClass.getPairedDoubleSameLesson();
                        System.out.println("LESSON #" + j + " " + t.getSubjectName() + " " + t.getTeacher().getName() + " DOUBLE_SAME");
                    }
                    if (lesson.getLessonType() == LessonType.DOUBLE_DIFFERENT
                            && !schoolClass.getPairedDoubleDifferentLesson().equals(lesson)) {
                        Lesson t = schoolClass.getPairedDoubleDifferentLesson();
                        System.out.println("LESSON #" + j + " " + t.getSubjectName() + " " + t.getTeacher().getName() + " DOUBLE_DIFFERENT");
                    }

                }
            }
        }
    }
}
