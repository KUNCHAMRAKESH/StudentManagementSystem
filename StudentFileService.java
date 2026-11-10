
import java.io.*;
import java.util.HashMap;

public class StudentFileService {

    private final String fileName = "students.dat";

    public void saveStudents(HashMap<Integer, Student> students) {

        try (ObjectOutputStream out =
             new ObjectOutputStream(new FileOutputStream(fileName))) {

            out.writeObject(students);

        } catch (IOException e) {
            throw new RuntimeException("Unable to save students", e);
        }
    }

    @SuppressWarnings("unchecked")
    public HashMap<Integer, Student> loadStudents() {

        File file = new File(fileName);

        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream in =
             new ObjectInputStream(new FileInputStream(file))) {

            return (HashMap<Integer, Student>) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Unable to load students", e);
        }
    }
}
