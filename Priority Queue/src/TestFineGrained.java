import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ali on 4/25/2017.
 */
public class TestFineGrained {
    static PriorityQueueFineGrained<Integer> testing = new PriorityQueueFineGrained<>();
    static AtomicInteger j = new AtomicInteger();
    public static void main(String[] args) throws InterruptedException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                testing.add(j.getAndIncrement());
                testing.add(j.getAndIncrement());
                testing.add(j.getAndIncrement());
                synchronized (testing) {
                    System.out.println(Thread.currentThread() + " " + testing.poll());
                }
                synchronized (testing) {
                    System.out.println(Thread.currentThread() + " " + testing.poll());
                }
//                System.out.println(testing.peek());
            }
        };

        Set<Thread> threads = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            threads.add(new Thread(r));
        }
        for(Thread t: threads) {
            t.start();
        }
        for(Thread t: threads) {
            t.join();
        }
        System.out.println(testing.toString());

    }
}
