
import java.io.Serializable;

public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private double marks;
    private int attendance;
    private StudentStatus status;
    private Course course;

    public Student(int id, String name, String email,
                   double marks, int attendance,
                   StudentStatus status, Course course) {

        if (id <= 0) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        this.id = id;
        this.name = name;
        this.email = email;
        setMarks(marks);
        setAttendance(attendance);
        this.status = status;
        this.course = course;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getMarks() { return marks; }
    public int getAttendance() { return attendance; }
    public StudentStatus getStatus() { return status; }
    public Course getCourse() { return course; }

    public void setMarks(double marks) {
        if (marks < 0 || marks > 100) {
            throw new IllegalArgumentException("Marks must be 0-100");
        }
        this.marks = marks;
    }

    public void setAttendance(int attendance) {
        if (attendance < 0 || attendance > 100) {
            throw new IllegalArgumentException("Attendance must be 0-100");
        }
        this.attendance = attendance;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public String calculateGrade() {
        if (marks >= 90) return "A+";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 40) return "D";
        return "Fail";
    }
}
