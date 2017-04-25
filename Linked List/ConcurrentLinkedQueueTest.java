import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;


class ConcurrentLinkedQueueTest implements Runnable {
    ConcurrentLinkedQueue<Integer> UnblockingQ;
    static long[] timeRecords = new long[20];
    int tid;
    public ConcurrentLinkedQueueTest(ConcurrentLinkedQueue<Integer> UnblockingQ, int tid) {
        this.UnblockingQ = UnblockingQ;
        this.tid = tid;
    };
    public void run() {
        try {
            long timeStart, timeStop;       // In nanoseconds
            timeStart = System.nanoTime();
            for(int i = 0; i < 100; i++) {
                UnblockingQ.add(new Integer(this.tid));
                UnblockingQ.add(new Integer(this.tid));
                UnblockingQ.add(new Integer(this.tid));
                UnblockingQ.add(UnblockingQ.poll());
                UnblockingQ.add(UnblockingQ.poll());
                UnblockingQ.poll();
            }
            timeStop = System.nanoTime();
            timeRecords[this.tid] = timeStop - timeStart;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void testUnblockingQ() {
        final int samples = 100;
        try {
            long timeTotal = 0;             // In nanoseconds

            ConcurrentLinkedQueue<Integer> UnblockingQ;
            Thread[] t = new Thread[20];

            for(int i = 0; i < samples; i++) {
                UnblockingQ = new ConcurrentLinkedQueue<Integer>();
                t = new Thread[20];
                for(int ct = 0; ct < 20; ct++) {
                    t[ct] = new Thread(new ConcurrentLinkedQueueTest(UnblockingQ, ct));
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
            System.out.println("Java library ConcurrentLinkedQueue test");
            System.out.println("Avg time(nanosec) per round: " + timeTotal/samples/20);//TimeUnit.NANOSECONDS.toMillis(timeTotal/samples));
            System.out.println("= = = = = = = = = = = = = = =");
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        testUnblockingQ();
    }
}
