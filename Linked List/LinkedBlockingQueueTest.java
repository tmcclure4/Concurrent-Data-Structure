import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;


class LinkedBlockingQueueTest implements Runnable {
    LinkedBlockingQueue<Integer> BlockingQ;
    static long[] timeRecords = new long[20];
    int tid;
    public LinkedBlockingQueueTest(LinkedBlockingQueue<Integer> BlockingQ, int tid) {
        this.BlockingQ = BlockingQ;
        this.tid = tid;
    };
    public void run() {
        try {
            long timeStart, timeStop;       // In nanoseconds
            timeStart = System.nanoTime();
            for(int i = 0; i < 100; i++) {
                BlockingQ.put(new Integer(this.tid));
                BlockingQ.put(new Integer(this.tid));
                BlockingQ.put(new Integer(this.tid));
                BlockingQ.put(BlockingQ.poll());
                BlockingQ.put(BlockingQ.poll());
                BlockingQ.poll();
            }
            timeStop = System.nanoTime();
            timeRecords[this.tid] = timeStop - timeStart;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void testBlockingQ() {
        final int samples = 100;
        try {
            long timeTotal = 0;             // In nanoseconds

            LinkedBlockingQueue<Integer> BlockingQ;
            Thread[] t = new Thread[20];

            for(int i = 0; i < samples; i++) {
                BlockingQ = new LinkedBlockingQueue<Integer>();
                t = new Thread[20];
                for(int ct = 0; ct < 20; ct++) {
                    t[ct] = new Thread(new LinkedBlockingQueueTest(BlockingQ, ct));
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
            System.out.println("Java library LinkedBlockingQueue test");
            System.out.println("Avg time(nanosec) per round: " + timeTotal/samples/20);//TimeUnit.NANOSECONDS.toMillis(timeTotal/samples));
            System.out.println("= = = = = = = = = = = = = = =");
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        testBlockingQ();
    }
}
