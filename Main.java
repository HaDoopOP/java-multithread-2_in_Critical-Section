
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Main {

    private static final int STARTING_NUM_STUDENTS_IN_LIBRARY = 2;
    private static final int ENTERING_NUM_STUDENTS_TO_LIBRARY = 3;

    // The program starts with already students in the library reading
    private static List<Student> studentsInLibrary = new ArrayList<>();
    private static Semaphore librarySemaphore = new Semaphore(1);

    // Add to the number of students in the library this
    // This function is protected by a semaphore to avoid race conditions
    public static void addStudentToLibrary(Student student) throws Exception {
        librarySemaphore.acquire();

        // When a new student comes inside the library, we signal those students
        // who want to leave (but cannot leave yet) that a new student entered
        // so they can probably leave now
        for (Student stud : studentsInLibrary) {
            if (stud.getState() == Student.WAIT_LEAVING) {
                stud.receiveNotification();
            }
        }

        studentsInLibrary.add(student);
        librarySemaphore.release();
    }

    // Deduct the number of students in the library
    // This function is protected by a semaphore to avoid race condition
    public static boolean removeStudentFromLibrary(Student student) throws Exception {
        librarySemaphore.acquire();

        boolean canLeave;

        // Student can leave as long as there are more than 2 students in the library
        if (studentsInLibrary.size() > 2) {
            canLeave = true;
        } else {
            int numStudentsStudying = 0;

            // Student cannot leave when there is at least 1 student studying
            // But they can leave if no more student is studying
            // Count how many students are studying at the moment
            for (Student stud : studentsInLibrary) {
                if (stud.getState() == Student.STUDYING) {
                    numStudentsStudying++;
                }
            }

            canLeave = numStudentsStudying == 0;
        }

        if (canLeave) {
            studentsInLibrary.remove(student);

            // Notify those other waiting student that a student as left
            for (Student stud : studentsInLibrary) {
                stud.receiveNotification();
            }
        }

        librarySemaphore.release();

        return canLeave;
    }

    // Return the number of students in the library
    public static int getNumStudentsInLibrary() throws Exception {
        librarySemaphore.acquire();
        int numStudents = studentsInLibrary.size();
        librarySemaphore.release();

        return numStudents;
    }

    // Entry point of the program
    public static void main(String[] args) throws Exception {
        int nextStudentID = 1;

        // Initially start the students that are in the library as studying
        for (int i = 0; i < STARTING_NUM_STUDENTS_IN_LIBRARY; i++) {
            Student student = new Student(nextStudentID++, Student.STUDYING);
            addStudentToLibrary(student);
            
            new Thread(student).start();
        }
        
        // This will give time for the first threads to execute before we execute the
        // entering students
        Thread.sleep(2000);
        
        // Then the rest of the student are still about to enter
        for(int i = 0; i < ENTERING_NUM_STUDENTS_TO_LIBRARY; i++) {
            new Thread(new Student(nextStudentID++, Student.ENTERING)).start();
        }
    }
}
