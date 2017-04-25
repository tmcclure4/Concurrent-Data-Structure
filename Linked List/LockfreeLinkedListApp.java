import java.util.*;
import java.util.concurrent.TimeUnit;

class LockfreeLinkedListApp implements Runnable {
    LockfreeLinkedList LockfreeL;
    static long[] timeRecords = new long[5];
    int tid;
    public LockfreeLinkedListApp(LockfreeLinkedList LockfreeL, int tid) {
        this.LockfreeL = LockfreeL;
        this.tid = tid;
    };
    public void run() {
        try {
            long timeStart, timeStop;       // In nanoseconds
            timeStart = System.nanoTime();
            LockfreeIterator iter;
            for(int i = 0; i < 10; i++) {
                iter = LockfreeL.begin();
                for(int j = 0; j < 2; j++) {
                    LockfreeL.insert(iter, new Integer(this.tid));
                    iter.next();
                }
            }
            for(int i = 0; i < 10; i++) {
                // Add to the tail
                LockfreeL.add(new Integer(this.tid));
                LockfreeL.add(new Integer(this.tid));
                // Move to the 25th node
                iter = LockfreeL.begin();
                for(int j = 0; j < 10; j++) {
                    iter.next();
                }
                // Delete 2 nodes, move forward 2 nodes and then insert 3 nodes
                LockfreeL.delete(iter); LockfreeL.delete(iter);
                iter.next(); iter.next();
                LockfreeL.insert(iter, new Integer(this.tid));
                LockfreeL.insert(iter, new Integer(this.tid));
                LockfreeL.insert(iter, new Integer(this.tid));
            }
            timeStop = System.nanoTime();
            this.timeRecords[this.tid] = timeStop - timeStart;

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void testLockfreeL(boolean measureTime) {
        try {
            long timeTotal = 0;             // In nanoseconds
            ArrayList<Integer> OperationCounts;

            LockfreeLinkedList LockfreeL = new LockfreeLinkedList();
            Thread[] t = new Thread[5];
            for(int ct = 0; ct < 5; ct++) {
                t[ct] = new Thread(new LockfreeLinkedListApp(LockfreeL, ct));
            }
            for(int ct = 0; ct < 5; ct++) {
                t[ct].start();
            }
            for(int ct = 0; ct < 5; ct++) {
                t[ct].join();
                timeTotal += timeRecords[ct];
            }
            OperationCounts = LockfreeL.getCounts();

            HashMap<Object, Integer> nodeCount = new HashMap<Object, Integer>();
            for(int ct = 0; ct < 5; ct++) {
                nodeCount.put(new Integer(ct), 0);
            }
            LockfreeIterator iter = LockfreeL.begin();
            for(; iter.hasNext(); iter.next()) {
                nodeCount.put(iter.get(), nodeCount.get(iter.get())+1);
                System.out.print(iter.get() + "->");
            }
            System.out.println("\n");
            System.out.println(nodeCount);
            int total = 0;
            for(Integer value:nodeCount.values()) {
                total += value;
            }
            System.out.println("Total node count: " + total);
            LockfreeL.reportOperationCounts();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        testLockfreeL(true);
    }
}
