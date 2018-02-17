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
    private final static int PENALTY_TEACHER_CHANGES_BUILDING_WITHOUT_PAUSE = 10;
    private final static int PENALTY_TOO_MANY_CHANGES_PER_DAY = 25;
    private final static int PENALTY_TOO_MANY_CHANGES_PER_WEEK = 10;
    private final static int ALLOWED_CHANGES_PER_DAY = 1;
    private final static int ALLOWED_CHANGES_PER_WEEK = 2;
    private static final int BOUND = 125;
    private static final int PENALTY_OPENING = 8;
    private final Map<Pair<DayTimeSlot, SchoolGroup>, Lesson> lessonByTime;
    private final Map<Pair<Integer, SchoolClass>, Building> buildingByDay;
    private final List<SchoolClass> schoolClasses;
    private final Set<SchoolClass> onlySecondBuildingSchoolClasses;
    private final static Random random = new Random(System.currentTimeMillis());
    private final List<SchoolGroup> schoolGroups;

    Timetable(List<SchoolClass> schoolClasses, List<SchoolGroup> schoolGroups) {
        this.schoolClasses = schoolClasses;
        this.schoolGroups = schoolGroups;
        lessonByTime = new HashMap<>();
        buildingByDay = new HashMap<>();
        onlySecondBuildingSchoolClasses = new HashSet<>();
        setLessonsRandomly();
        findOnlySecondBuildingSchoolClasses();
        setBuildingsRandomly();
    }

    private Timetable(List<SchoolClass> schoolClasses,
                      List<SchoolGroup> schoolGroups,
                      Map<Pair<DayTimeSlot, SchoolGroup>, Lesson> lessonByTime,
                      Map<Pair<Integer, SchoolClass>, Building> buildingByDay,
                      Set<SchoolClass> onlySecondBuildingSchoolClasses) {
        this.schoolClasses = schoolClasses;
        this.schoolGroups = schoolGroups;
        this.lessonByTime = lessonByTime;
        this.buildingByDay = buildingByDay;
        this.onlySecondBuildingSchoolClasses = onlySecondBuildingSchoolClasses;
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
            List<Lesson> lessons = schoolClass.getLessons();
            for (Lesson lesson: lessons) {
                DayTimeSlot dayTimeSlot = getFreeDayTimeSlot(lesson);
                SchoolGroup schoolGroup = lesson.getSchoolGroup();
                lessonByTime.put(new Pair<>(dayTimeSlot, schoolGroup), lesson);
            }
        }
    }

    private DayTimeSlot getFreeDayTimeSlot(Lesson lesson) {
        do {
            int i = random.nextInt(DayTimeSlot.DAYS);
            int j = random.nextInt(DayTimeSlot.LESSONS);
            DayTimeSlot dayTimeSlot = DayTimeSlot.slotByDayAndTime[i][j];
            if (isFree(dayTimeSlot, lesson)) {
                return dayTimeSlot;
            }
        } while (true);
    }

    private boolean isFree(DayTimeSlot dayTimeSlot, Lesson lesson) {
        if (lesson == null) {
            return true;
        }
        SchoolGroup schoolGroup = lesson.getSchoolGroup();
        List<SchoolGroup> schoolGroups = schoolGroup.getSchoolGroupsOfSchoolClass();
        SchoolGroup pairSchoolGroup = schoolGroup.getPairSchoolGroup();
        boolean entireClass = schoolGroup.isEntireClass();
        for (SchoolGroup current: schoolGroups) {
            if (!entireClass && current.equals(pairSchoolGroup)) {
                continue;
            }
            if (lessonByTime.get(new Pair<>(dayTimeSlot, current)) != null) {
                return false;
            }
        }
        return true;
    }

    int evaluatePenalty() {
        int penalty = 0;
        Map<Teacher, List<List<Integer>>> buildingsOfTeachers = new HashMap<>();
        for (int day = 0; day < DayTimeSlot.DAYS; day++) {
            Map<SchoolGroup, List<Boolean>> isLessonBySchoolGroup = createIsLessonBySchoolGroup();
            for (int time = 0; time < DayTimeSlot.LESSONS; time++) {
                Map<Teacher, Set<Lesson>> lessonsOfTeachers = new HashMap<>();
                for (SchoolGroup schoolGroup: schoolGroups) {
                    List<Boolean> isLesson = isLessonBySchoolGroup.get(schoolGroup);
                    Lesson lesson = lessonByTime.get(
                            new Pair<>(DayTimeSlot.slotByDayAndTime[day][time], schoolGroup)
                    );
                    if (lesson == null) {
                        isLesson.add(time, false);
                        continue;
                    }
                    isLesson.add(time, true);
                    penalty += evaluatePenaltyOfLesson(
                            day,
                            time,
                            schoolGroup,
                            lesson,
                            buildingsOfTeachers,
                            lessonsOfTeachers
                    );
                }
                penalty += evaluatePenaltyOfHavingMoreThenOneLessonAtOnce(lessonsOfTeachers);
            }
            penalty += evaluatePenaltyOfOpenings(isLessonBySchoolGroup);
        }
        penalty += evaluatePenaltyOfClassesAtFirstBuilding();
        penalty += evaluatePenaltyOfTeachersBuildings(buildingsOfTeachers);
        return penalty;
    }

    private int evaluatePenaltyOfOpenings(Map<SchoolGroup, List<Boolean>> isLessonBySchoolGroup) {
        int penalty = 0;
        for (SchoolGroup schoolGroup: isLessonBySchoolGroup.keySet()) {
            penalty += evaluatePenaltyOfOpenings(isLessonBySchoolGroup.get(schoolGroup));
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


    private Map<SchoolGroup,List<Boolean>> createIsLessonBySchoolGroup() {
        Map<SchoolGroup, List<Boolean>> isLessonBySchoolGroup = new HashMap<>();
        for (SchoolGroup schoolGroup: schoolGroups) {
            List<Boolean> isLesson = new ArrayList<>();
            for (int time = 0; time < DayTimeSlot.LESSONS; time++) {
                isLesson.add(false);
            }
            isLessonBySchoolGroup.put(schoolGroup, isLesson);
        }
        return isLessonBySchoolGroup;
    }

    private int evaluatePenaltyOfTeachersBuildings(
            Map<Teacher, List<List<Integer>>> buildingsOfTeachers) {
        int penalty = 0;
        for (Teacher teacher: buildingsOfTeachers.keySet()) {
            penalty += evaluatePenaltyOfBuildingChange(
                    buildingsOfTeachers.get(teacher),
                    onlySecondBuildingTeachersLastNames.contains(teacher.getLastName())
            );
        }
        return penalty;
    }

    private int evaluatePenaltyOfClassesAtFirstBuilding() {
        int penalty = 0;
        for (int day = 0; day < DayTimeSlot.DAYS; day++) {
            for (SchoolClass schoolClass : schoolClasses) {
                if (onlySecondBuildingSchoolClasses.contains(schoolClass)
                        && buildingByDay.get(new Pair<>(day, schoolClass)).getId() != 2) {
                    penalty += PENALTY_CLASS_AT_FIRST_BUILDING;
                }
            }
        }
        return penalty;
    }

    private int evaluatePenaltyOfHavingMoreThenOneLessonAtOnce(Map<Teacher,
            Set<Lesson>> lessonsOfTeachers) {
        int penalty = 0;
        for (Teacher teacher: lessonsOfTeachers.keySet()) {
            Set<Lesson> lessons = lessonsOfTeachers.get(teacher);
            penalty += (lessons.size() - 1) * PENALTY_TEACHER_HAS_MORE_THAN_ONE_LESSON_AT_ONCE;
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

    private int evaluatePenaltyOfLesson(int i,
                                        int j,
                                        SchoolGroup schoolGroup, Lesson lesson,
                                        Map<Teacher, List<List<Integer>>> buildingsOfTeachers,
                                        Map<Teacher, Set<Lesson>> lessonsOfTeachers) {
        Teacher teacher = lesson.getTeacher();
        SchoolClass schoolClass = schoolGroup.getSchoolClass();
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

    private Timetable swapLessons(SchoolGroup schoolGroup, int i, int j, int k, int q) {
        Map<Pair<DayTimeSlot, SchoolGroup>, Lesson> modifiedLessonByTime = new HashMap<>();
        lessonByTime.forEach(modifiedLessonByTime::put);
        performLessonsSwap(modifiedLessonByTime, schoolGroup, i, j, k, q);
        return new Timetable(
                schoolClasses,
                schoolGroups,
                modifiedLessonByTime,
                buildingByDay,
                onlySecondBuildingSchoolClasses
        );
    }

    private void performLessonsSwap(
            Map<Pair<DayTimeSlot, SchoolGroup>, Lesson> lessonByTime,
            SchoolGroup schoolGroup,
            int i,
            int j,
            int k,
            int q) {
        Pair<DayTimeSlot, SchoolGroup> p1 = new Pair<>(
                DayTimeSlot.slotByDayAndTime[i][j],
                schoolGroup
        );
        Pair<DayTimeSlot, SchoolGroup> p2 = new Pair<>(
                DayTimeSlot.slotByDayAndTime[k][q],
                schoolGroup
        );
        Lesson l1 = lessonByTime.get(p1);
        Lesson l2 = lessonByTime.get(p2);
        lessonByTime.put(p1, l2);
        lessonByTime.put(p2, l1);
    }

    private Timetable swapLessons(SchoolClass schoolClass, int i, int j, int k, int q) {
        Map<Pair<DayTimeSlot, SchoolGroup>, Lesson> modifiedLessonByTime = new HashMap<>();
        lessonByTime.forEach(modifiedLessonByTime::put);
        List<SchoolGroup> schoolGroups = schoolClass.getSchoolGroups();
        for (SchoolGroup schoolGroup: schoolGroups) {
            performLessonsSwap(modifiedLessonByTime, schoolGroup, i, j, k, q);
        }
        return new Timetable(
                schoolClasses,
                this.schoolGroups,
                modifiedLessonByTime,
                buildingByDay,
                onlySecondBuildingSchoolClasses
        );
    }

    private Timetable changeBuildings(int day, SchoolClass schoolClass) {
        Pair<Integer, SchoolClass> current = new Pair<>(day, schoolClass);
        Building building = buildingByDay.get(current);
        int buildingId = 3 - building.getId();
        building = GetterById.getBuildingById(buildingId);
        Map<Pair<Integer, SchoolClass>, Building> modifiedBuildingByDay = new HashMap<>();
        buildingByDay.forEach(modifiedBuildingByDay::put);
        modifiedBuildingByDay.put(current, building);
        return new Timetable(
                schoolClasses,
                schoolGroups,
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
        int day = random.nextInt(DayTimeSlot.DAYS);
        int schoolClassIndex = random.nextInt(schoolClasses.size());
        SchoolClass schoolClass = schoolClasses.get(schoolClassIndex);
        if (random.nextInt(4) == 0) {
            neighbourTimetable = changeBuildings(day, schoolClass);
            return neighbourTimetable;
        } else {
            int time = random.nextInt(DayTimeSlot.LESSONS);
            int schoolGroupIndex = random.nextInt(schoolClass.getSchoolGroups().size());
            SchoolGroup schoolGroup = schoolClass.getSchoolGroups().get(schoolGroupIndex);
            Lesson lesson = lessonByTime.get(
                    new Pair<>(DayTimeSlot.slotByDayAndTime[day][time], schoolGroup)
            );

            SchoolGroup pairSchoolGroup = schoolGroup.getPairSchoolGroup();
            Lesson pairLesson = lessonByTime.get(
                    new Pair<>(DayTimeSlot.slotByDayAndTime[day][time], pairSchoolGroup)
            );
            int day2;
            int time2;
            int rnd = random.nextInt(3);
            if (pairLesson == null || rnd == 0) {
                day2 = random.nextInt(DayTimeSlot.DAYS);
                time2 = random.nextInt(DayTimeSlot.LESSONS);
                neighbourTimetable = swapLessons(schoolClass, day, time, day2, time2);
                return neighbourTimetable;
            } else {
                SchoolGroup swappedSchoolGroup = rnd == 1 ? schoolGroup : pairSchoolGroup;
                Lesson swappedLesson = swappedSchoolGroup == schoolGroup ? lesson : pairLesson;
                do {
                    day2 = random.nextInt(DayTimeSlot.DAYS);
                    time2 = random.nextInt(DayTimeSlot.LESSONS);
                    if (isFree(DayTimeSlot.slotByDayAndTime[day2][time2], swappedLesson)) {
                        neighbourTimetable = swapLessons(swappedSchoolGroup, day, time, day2, time2);
                        return neighbourTimetable;
                    }
                } while (true);
            }
        }
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

        out.println("   <cards options=\"canadd,export:silent\" columns=\"lessonid,period,days," +
                "weeks,terms,classroomids\">");

        for (int i = 0; i < 6; i++) {
            for (SchoolClass schoolClass: schoolClasses) {
                System.out.println("CLASS IS " + schoolClass.getName() + ", DAY = " + i
                        + ", BUILDING = " + buildingByDay.get(new Pair<>(i, schoolClass)).getId()
                        + " ");
                for (int j = 0; j < 8; j++) {
                    for (SchoolGroup schoolGroup: schoolClass.getSchoolGroups()) {
                        Lesson lesson = lessonByTime.get(
                                new Pair<>(DayTimeSlot.slotByDayAndTime[i][j], schoolGroup)
                        );
                        if (lesson == null) {
                            continue;
                        }
                        printCard(out, i, j, lesson);
                        String a = lesson.getSubjectName();
                        String b = lesson.getTeacher().getName();
                        String c = lesson.getSchoolGroup().getId();
                        boolean d = lesson.getSchoolGroup().isEntireClass();
                        System.out.println("CLASS: " + schoolClass.getName() + " "
                                + " LESSON #" + j + " " + a + " " + b + " " + c
                                + " IS ENTIRE CLASS " + d);
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
        <card lessonid="EC9D7298C0920B8D" classroomids="" period="2" weeks="1" terms="1"
        days="100000"/>
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
