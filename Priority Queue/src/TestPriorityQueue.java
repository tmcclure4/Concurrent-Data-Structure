import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Ali on 4/25/2017.
 */
public class TestPriorityQueue implements Runnable{
    PriorityBlockingQueue<Integer> queue;
    int pid;
    static long[] timeRecords = new long[20];
    public static void main(String[] args) throws InterruptedException {
        final int samples = 100;
        try {
            long timeTotal = 0;             // In nanoseconds

            PriorityBlockingQueue<Integer> queue;
            Thread[] t = new Thread[20];

            for(int i = 0; i < samples; i++) {
                queue = new PriorityBlockingQueue<Integer>();
                t = new Thread[20];
                for(int ct = 0; ct < 20; ct++) {
                    t[ct] = new Thread(new TestPriorityQueue(queue, ct));
                }
                for(int ct = 0; ct < 20; ct++) {
                    t[ct].start();
                }
                for(int ct = 0; ct < 20; ct++) {
                    t[ct].join();
                    timeTotal += timeRecords[ct];
                }
            }

            // Report test results - take average.
            System.out.println("= = = = = = = = = = = = = = =");
            System.out.println("Java library PriorityBlockingQueue test");
            System.out.println("Avg time(nanosec) per round: " + timeTotal/samples/20);//TimeUnit.NANOSECONDS.toMillis(timeTotal/samples));
            System.out.println("= = = = = = = = = = = = = = =");
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

    }

    public TestPriorityQueue(PriorityBlockingQueue<Integer> queue, int pid) {
        this.queue = queue;
        this.pid = pid;
    }

    @Override
    public void run() {
        try {
            long timeStart, timeStop;       // In nanoseconds
            timeStart = System.nanoTime();
            for(int i = 0; i < 100; i++) {
                queue.add(pid);
                queue.add(pid);
                queue.add(pid);
                queue.add(queue.poll());
                queue.add(queue.poll());
                queue.poll();
            }
            timeStop = System.nanoTime();
            timeRecords[pid] = timeStop - timeStart;
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
