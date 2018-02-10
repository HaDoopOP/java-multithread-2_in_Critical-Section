
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Student implements Runnable {

    public static final int ENTERING = 0;
    public static final int STUDYING = 1;
    public static final int LEAVING = 2;
    public static final int WAIT_LEAVING = 3;
    public static final int LEFT_LIBRARY = 4;

    private int id;
    private int state;
    private Semaphore waitSemaphore = new Semaphore(0);

    // Create a student
    public Student(int id, int initialState) {
        this.id = id;
        this.state = initialState;
    }

    // Student receives a notification if another student entered a library or left the library
    // The student can now leave the library if this student is in the state
    // of leaving but cannot leave because there are only 2 students in the library
    public void receiveNotification() {
        if (state == WAIT_LEAVING) {
            waitSemaphore.release();
        }
    }

    // Return what the student is doing in the library
    public int getState() {
        return state;
    }

    // Entry point of running a student from a separate thread
    @Override
    public void run() {
        try {
            Random random = new Random();

            if (state == ENTERING) {
                // Student will enter randomly within the span of 1 to 5 seconds randomly
                Thread.sleep((random.nextInt(5) + 1) * 1000);
                System.out.println(System.currentTimeMillis() + ", Student " + id + " has entered the library...");
                Main.addStudentToLibrary(this);
                state = STUDYING;
            }

            if (state == STUDYING) {
                // Student will remain in the library studying within a span of 1 to 5 seconds randomly
                System.out.println(System.currentTimeMillis() + ", Student " + id + " is now studying...");
                Thread.sleep((random.nextInt(5) + 1) * 1000);
                System.out.println(System.currentTimeMillis() + ", Student " + id + " has finished studying...");
                state = LEAVING;
            }

            if (state == LEAVING) {
                if (Main.removeStudentFromLibrary(this)) {
                    System.out.println(System.currentTimeMillis() + ", Student " + id + " has left the library...");
                } else {
                    // When the student was able to remove that means it has to wait
                    // for another student to finish studying because there are only
                    // Both of them left
                    state = WAIT_LEAVING;
                    System.out.println(System.currentTimeMillis() + ", Student " + id + " cannot leave and has to wait...");
                }
            }

            // If leaving but cannot leave, wait until a new student comes or when at least 2 students left in the
            // library has finished studying
            while (state == WAIT_LEAVING) {
                waitSemaphore.acquire();

                if (Main.removeStudentFromLibrary(this)) {
                    System.out.println(System.currentTimeMillis() + ", Student " + id + " has left the library...");
                    break;
                }
            }

            state = LEFT_LIBRARY;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
