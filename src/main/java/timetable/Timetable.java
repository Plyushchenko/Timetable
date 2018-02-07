package timetable;

import javafx.util.Pair;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class Timetable {
    private final static Set<String> onlySecondBuildingTeachersLastNames = Stream.of(
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
    private final static int PENALTY_CLASS_AT_FIRST_BUILDING = 10;
    private final static int PENALTY_TEACHER_AT_FIRST_BUILDING = 10;
    private final static int PENALTY_TEACHER_HAS_MORE_THAN_ONE_LESSON_AT_ONCE = 25;
    private final static int PENALTY_WRONG_BUILDING = 50;
    private final static int PENALTY_OPENING = 2;
    private final static int PENALTY_TEACHER_CHANGES_BUILDING_WITHOUT_PAUSE = 10;
    private final static int PENALTY_TOO_MANY_CHANGES_PER_DAY = 25;
    private final static int PENALTY_TOO_MANY_CHANGES_PER_WEEK = 10;
    private final static int ALLOWED_CHANGES_PER_DAY = 1;
    private final static int ALLOWED_CHANGES_PER_WEEK = 2;
    private static final int BOUND = 0;
    private final Map<Pair<DayTimeSlot, SchoolClass>, Lesson> lessonByTime;
    private final Map<Pair<Integer, SchoolClass>, Building> buildingByDay;
    private final List<SchoolClass> schoolClasses;
    private final Set<SchoolClass> onlySecondBuildingSchoolClasses;
    private final static Random random = new Random(System.currentTimeMillis());

    Timetable(List<SchoolClass> schoolClasses) {
        this.schoolClasses = schoolClasses;
        lessonByTime = new HashMap<>();
        buildingByDay = new HashMap<>();
        onlySecondBuildingSchoolClasses = new HashSet<>();
        setLessonsRandomly();
        findOnlySecondBuildingSchoolClasses();
        setBuildingsRandomly();
    }

    private void setBuildingsRandomly() {
        for (SchoolClass schoolClass: schoolClasses) {
            for (int i = 0; i < DayTimeSlot.DAYS; i++) {
                Pair<Integer, SchoolClass> current = new Pair<>(i, schoolClass);
                int buildingId;
                if (onlySecondBuildingSchoolClasses.contains(schoolClass)) {
                    buildingId = 2;
                } else {
                    buildingId = random.nextInt(2) + 1;
                }
                Building building = GetterById.getBuildingById(buildingId);
                buildingByDay.put(current, building);
            }
        }
    }

    private void findOnlySecondBuildingSchoolClasses() {
        for (SchoolClass schoolClass: schoolClasses) {
            String name = schoolClass.getName();
            boolean onlySecondBuilding = name.startsWith("5")
                    || name.startsWith("6")
                    || name.startsWith("7");
            if (onlySecondBuilding) {
                onlySecondBuildingSchoolClasses.add(schoolClass);
            }
        }
    }

    private void setLessonsRandomly() {
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
    }

    private Timetable(List<SchoolClass> schoolClasses,
                      Map<Pair<DayTimeSlot, SchoolClass>, Lesson> lessonByTime,
                      Map<Pair<Integer, SchoolClass>, Building> buildingByDay,
                      Set<SchoolClass> onlySecondBuildingSchoolClasses) {
        this.schoolClasses = schoolClasses;
        this.lessonByTime = lessonByTime;
        this.buildingByDay = buildingByDay;
        this.onlySecondBuildingSchoolClasses = onlySecondBuildingSchoolClasses;
    }

    private static List<Integer> splitNumber(int n, int m) {
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

    int evaluatePenalty() {
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
                    penalty += evaluatePenaltyOfLesson(i, j, schoolClass, lesson,
                            buildingsOfTeachers, lessonsOfTeachers);
                    if (lesson.getLessonType() == LessonType.DOUBLE_SAME &&
                            !schoolClass.getPairedDoubleSameLesson().equals(lesson)) {
                        penalty += evaluatePenaltyOfLesson(i, j, schoolClass,
                                schoolClass.getPairedDoubleSameLesson(),
                                buildingsOfTeachers, lessonsOfTeachers);
                    }
                    if (lesson.getLessonType() == LessonType.DOUBLE_DIFFERENT &&
                            !schoolClass.getPairedDoubleDifferentLesson().equals(lesson)) {
                        penalty += evaluatePenaltyOfLesson(i, j, schoolClass,
                                schoolClass.getPairedDoubleDifferentLesson(),
                                buildingsOfTeachers, lessonsOfTeachers);
                    }
                }
                penalty += evaluatePenaltyOfHavingMoreThenOneLessonAtOnce(lessonsOfTeachers);
            }
            for (SchoolClass schoolClass : schoolClasses) {
                List<Boolean> isLesson = new ArrayList<>();
                for (int j = 0; j < DayTimeSlot.LESSONS; j++) {
                    Lesson lesson = lessonByTime.get(
                            new Pair<>(DayTimeSlot.slotByDayAndTime.get(i).get(j), schoolClass)
                    );
                    isLesson.add (lesson != null);
                }
                penalty += evaluatePenaltyOfOpenings(isLesson);

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

    private int evaluatePenaltyOfHavingMoreThenOneLessonAtOnce(Map<Teacher,
            Set<Lesson>> lessonsOfTeachers) {
        int penalty = 0;
        for (Set<Lesson> lessonsOfTeacher : lessonsOfTeachers.values()) {
            penalty += (lessonsOfTeacher.size() - 1) *
                    PENALTY_TEACHER_HAS_MORE_THAN_ONE_LESSON_AT_ONCE;
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
            int pos;
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
        return penalty;
    }

    private int evaluatePenaltyOfOpenings(List<Boolean> isLesson) {
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

    private int evaluatePenaltyOfLesson(int i, int j, SchoolClass schoolClass, Lesson lesson,
                                Map<Teacher, List<List<Integer>>> buildingsOfTeachers,
                                Map<Teacher, Set<Lesson>> lessonsOfTeachers) {
        Teacher teacher = lesson.getTeacher();
        Building building = buildingByDay.get(new Pair<>(i, schoolClass));
        int buildingId = building.getId();
        lessonsOfTeachers.putIfAbsent(teacher, new HashSet<>());
        lessonsOfTeachers.get(teacher).add(lesson);
        buildingsOfTeachers.putIfAbsent(teacher, emptyBuildingIds());
        buildingsOfTeachers.get(teacher).get(i).set(j, buildingId);
        Subject subject = lesson.getSubject();
        if (building.isNotPossibleSubject(subject)) {
            return PENALTY_WRONG_BUILDING;
        }
        return 0;
    }

    private static List<List<Integer>> emptyBuildingIds() {
        List<List<Integer>> buildingIds = new ArrayList<>();
        for (int k = 0; k < DayTimeSlot.DAYS; k++) {
            List<Integer> kthDay = new ArrayList<>();
            for (int q = 0; q < DayTimeSlot.LESSONS; q++) {
                kthDay.add(0);
            }
            buildingIds.add(kthDay);
        }
        return buildingIds;
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
        return new Timetable(
                schoolClasses,
                modifiedLessonByTime,
                buildingByDay,
                onlySecondBuildingSchoolClasses
        );
    }

    private Timetable changeBuildings(SchoolClass schoolClass, int i) {
        Pair<Integer, SchoolClass> current = new Pair<>(i, schoolClass);
        Building building = buildingByDay.get(current);
        int buildingId = 3 - building.getId();
        building = GetterById.getBuildingById(buildingId);
        Map<Pair<Integer, SchoolClass>, Building> modifiedBuildingByDay = new HashMap<>();
        buildingByDay.forEach(modifiedBuildingByDay::put);
        modifiedBuildingByDay.put(current, building);
        return new Timetable(
                schoolClasses,
                lessonByTime,
                modifiedBuildingByDay,
                onlySecondBuildingSchoolClasses
        );
    }

    Timetable simulatedAnnealing(double temperature, double coolingRate) {
        Timetable currentTimetable = this;
        Timetable bestTimetable = currentTimetable;
        int bestPenalty = evaluatePenalty();
        int initialPenalty = bestPenalty;
        for (long i = 0; bestPenalty > BOUND; i++) {
            if (i % 1000 == 0) {
                System.out.println(i + " ITERATIONS");
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
            if (currentPenalty < bestPenalty) {
                bestTimetable = currentTimetable;
                bestPenalty = currentPenalty;
                System.out.println("BEST RESULT IS " + bestPenalty + ", INITIAL ONE IS " + initialPenalty);
            }
            temperature *= coolingRate;
        }
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
            neighbourTimetable = swapLessons(schoolClass,
                    dayNumber, lessonNumber,
                    dayNumber2, lessonNumber2
            );
        }
        return neighbourTimetable;
    }

    void print() throws IOException {
        Path timetablePath = Files.createFile(Paths.get(Main.RESOURCES_PATH.toString(),
                "timetable-" + Integer.toString(random.nextInt()) + ".xml"));
        Path templatePath = Paths.get(Main.RESOURCES_PATH.toString(), "template.xml");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                timetablePath.toString(), true)));
        FileChannel source = new FileInputStream(templatePath.toFile()).getChannel();
        FileChannel destination = new FileOutputStream(timetablePath.toFile()).getChannel();
        destination.transferFrom(source, 0, source.size());

        out.println("   <cards options=\"canadd,export:silent\" columns=\"lessonid,period,days,weeks,terms,classroomids\">");
        for (int i = 0; i < 6; i++) {
            for (SchoolClass schoolClass: schoolClasses) {
                System.out.println("CLASS IS " + schoolClass.getName() + ", DAY = " + i + ", BUILDING = " + buildingByDay.get(new Pair<>(i, schoolClass)).getId());
                for (int j = 0; j < 8; j++) {
                    Lesson lesson = lessonByTime.get(new Pair<>(DayTimeSlot.slotByDayAndTime.get(i).get(j), schoolClass));
                    if (lesson == null) {
                        System.out.println("LESSON #" + j + " NULL");
                        continue;
                    }
                    printCard(out, i, j, lesson);
                    String a = lesson.getSubjectName();
                    String b = lesson.getTeacher().getName();
                    System.out.println("LESSON #" + j + " " + a + " " + b);
                    if (lesson.getLessonType() == LessonType.DOUBLE_SAME
                            && !schoolClass.getPairedDoubleSameLesson().equals(lesson)) {
                        Lesson t = schoolClass.getPairedDoubleSameLesson();
                        printCard(out, i, j, t);
                        System.out.println("LESSON #" + j + " " + t.getSubjectName() + " " + t.getTeacher().getName() + " DOUBLE_SAME");
                    }
                    if (lesson.getLessonType() == LessonType.DOUBLE_DIFFERENT
                            && !schoolClass.getPairedDoubleDifferentLesson().equals(lesson)) {
                        Lesson t = schoolClass.getPairedDoubleDifferentLesson();
                        printCard(out, i, j, t);
                        System.out.println("LESSON #" + j + " " + t.getSubjectName() + " " + t.getTeacher().getName() + " DOUBLE_DIFFERENT");
                    }
                }
            }
        }
        out.println("   </cards>");
        out.println("</timetable>");
        out.close();
        System.out.println("XML FILE IS AT " + timetablePath.toString());

    }

    private void printCard(PrintWriter out, int i, int j, Lesson lesson) {
        /*
        Format:
        <card lessonid="EC9D7298C0920B8D" classroomids="" period="2" weeks="1" terms="1" days="100000"/>
         */
        out.print("      <card lessonid=\"" + lesson.getId()
                + "\" classroomids=\"\" period=\"" + (j + 1) + "\" weeks=\"1\" terms=\"1\" days=\"");
        for (int k = 0; k < i; k++)
            out.print("0");
        out.print("1");
        for (int k = i + 1; k < DayTimeSlot.DAYS; k++) {
            out.print("0");
        }
        out.println("\"/>");
    }
}
