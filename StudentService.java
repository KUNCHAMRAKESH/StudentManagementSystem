
import java.util.*;

public class StudentService {

    private HashMap<Integer, Student> students;
    private StudentFileService fileService;

    public StudentService() {
        fileService = new StudentFileService();
        students = fileService.loadStudents();
    }

    public synchronized void addStudent(Student student) {

        if (students.containsKey(student.getId())) {
            throw new IllegalArgumentException("Student ID already exists");
        }

        students.put(student.getId(), student);
        save();
    }

    public synchronized ArrayList<Student> getAllStudents() {
        return new ArrayList<>(students.values());
    }

    public synchronized Student searchById(int id) {
        return students.get(id);
    }

    public synchronized ArrayList<Student> searchByName(String name) {

        ArrayList<Student> result = new ArrayList<>();

        for (Student student : students.values()) {
            if (student.getName().toLowerCase()
                    .contains(name.toLowerCase())) {
                result.add(student);
            }
        }

        return result;
    }

    public synchronized void updateStudent(
            int id, double marks, int attendance) {

        Student student = students.get(id);

        if (student == null) {
            throw new IllegalArgumentException("Student not found");
        }

        student.setMarks(marks);
        student.setAttendance(attendance);
        save();
    }

    public synchronized void removeStudent(int id) {

        if (students.remove(id) == null) {
            throw new IllegalArgumentException("Student not found");
        }

        save();
    }

    public synchronized Student getTopper() {

        return students.values().stream()
                .max(Comparator.comparingDouble(Student::getMarks))
                .orElse(null);
    }

    private void save() {
        fileService.saveStudents(students);
    }
}
