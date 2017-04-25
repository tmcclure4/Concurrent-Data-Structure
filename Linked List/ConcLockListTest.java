import java.util.*;
import java.util.concurrent.TimeUnit;


class ConcLockListTest implements Runnable {
    ConcLockList BlockingL;
    static long[] timeRecords = new long[20];
    int tid;
    public ConcLockListTest(ConcLockList BlockingL, int tid) {
        this.BlockingL = BlockingL;
        this.tid = tid;
    };
    public void run() {
        try {
            long timeStart, timeStop;       // In nanoseconds
            timeStart = System.nanoTime();
                for(int i = 0; i < 100; i++) {
                    BlockingL.add(new Integer(this.tid));
                    BlockingL.add(new Integer(this.tid));
                    BlockingL.add(new Integer(this.tid));
                    BlockingL.add(BlockingL.poll());
                    BlockingL.add(BlockingL.poll());
                    BlockingL.poll();
                }
            timeStop = System.nanoTime();
            timeRecords[this.tid] = timeStop - timeStart;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void testBlockingL() {
        final int samples = 100;
        try {
            long timeTotal = 0;             // In nanoseconds

            ConcLockList BlockingL;
            Thread[] t = new Thread[20];

            for(int i = 0; i < samples; i++) {
                BlockingL = new ConcLockList();
                t = new Thread[20];
                for(int ct = 0; ct < 20; ct++) {
                    t[ct] = new Thread(new ConcLockListTest(BlockingL, ct));
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
            System.out.println("Java library ConcLockList test");
            System.out.println("Avg time(nanosec) per round: " + timeTotal/samples/20);//TimeUnit.NANOSECONDS.toMillis(timeTotal/samples));
            System.out.println("= = = = = = = = = = = = = = =");
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        testBlockingL();
    }
}
