
import java.io.Serializable;

public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    private String courseName;
    private String duration;

    public Course(String courseName, String duration) {
        this.courseName = courseName;
        this.duration = duration;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDuration() {
        return duration;
    }
}
