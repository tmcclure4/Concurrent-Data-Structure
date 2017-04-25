import java.util.*;
import java.util.concurrent.TimeUnit;

class LockfreeLinkedListTest implements Runnable {
    LockfreeLinkedList LockfreeL;
    static long[] timeRecords = new long[20];
    int tid;
    public LockfreeLinkedListTest(LockfreeLinkedList LockfreeL, int tid) {
        this.LockfreeL = LockfreeL;
        this.tid = tid;
    };
    public void run() {
        try {
            long timeStart, timeStop;       // In nanoseconds
            timeStart = System.nanoTime();
            /*
            LockfreeIterator iter;
            for(int i = 0; i < 50; i++) {
                iter = LockfreeL.begin();
                for(int j = 0; j < 2; j++) {
                    LockfreeL.insert(iter, new Integer(this.tid));
                    iter.next();
                }
            }
            for(int i = 0; i < 200; i++) {
                // Add to the tail
                LockfreeL.add(new Integer(this.tid));
                LockfreeL.add(new Integer(this.tid));
                // Move to the 25th node
                iter = LockfreeL.begin();
                for(int j = 0; j < 25; j++) {
                    iter.next();
                }
                // Delete 2 nodes, move forward 2 nodes and then insert 3 nodes
                LockfreeL.delete(iter); LockfreeL.delete(iter);
                iter.next(); iter.next();
                LockfreeL.insert(iter, new Integer(this.tid));
                LockfreeL.insert(iter, new Integer(this.tid));
                LockfreeL.insert(iter, new Integer(this.tid));
            }
            */
            for(int i = 0; i < 100; i++) {
                LockfreeL.add(new Integer(this.tid));
                LockfreeL.add(new Integer(this.tid));
                LockfreeL.add(new Integer(this.tid));
                LockfreeL.add(LockfreeL.poll());
                LockfreeL.add(LockfreeL.poll());
                LockfreeL.poll();
            }
            timeStop = System.nanoTime();
            this.timeRecords[this.tid] = timeStop - timeStart;

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void testLockfreeL(boolean measureTime) {
        final int samples = 100;
        try {
            long timeTotal = 0;             // In nanoseconds
            ArrayList<Integer> OperationCounts;

            LockfreeLinkedList LockfreeL = new LockfreeLinkedList();
            Thread[] t = new Thread[20];
            for(int ct = 0; ct < 20; ct++) {
                t[ct] = new Thread(new LockfreeLinkedListTest(LockfreeL, ct));
            }
            for(int ct = 0; ct < 20; ct++) {
                t[ct].start();
            }
            for(int ct = 0; ct < 20; ct++) {
                t[ct].join();
                timeTotal += timeRecords[ct];
            }
            OperationCounts = LockfreeL.getCounts();

            if(measureTime == false) {
                HashMap<Object, Integer> nodeCount = new HashMap<Object, Integer>();
                for(int ct = 0; ct < 20; ct++) {
                    nodeCount.put(new Integer(ct), 0);
                }
                LockfreeIterator iter = LockfreeL.begin();
                for(; iter.hasNext(); iter.next()) {
                    nodeCount.put(iter.get(), nodeCount.get(iter.get())+1);
                    //System.out.print(iter.get() + "->");
                }
                //System.out.println("\n");
                System.out.println(nodeCount);
                int total = 0;
                for(Integer value:nodeCount.values()) {
                    total += value;
                }
                System.out.println("Total node count: " + total);
                LockfreeL.reportOperationCounts();
            }
            else {
                for(int i = 0; i < samples - 1; i++) {
                    LockfreeL = new LockfreeLinkedList();
                    t = new Thread[20];
                    for(int ct = 0; ct < 20; ct++) {
                        t[ct] = new Thread(new LockfreeLinkedListTest(LockfreeL, ct));
                    }
                    for(int ct = 0; ct < 20; ct++) {
                        t[ct].start();
                    }
                    for(int ct = 0; ct < 20; ct++) {
                        t[ct].join();
                        timeTotal += timeRecords[ct];
                    }
                    ArrayList<Integer> oneCounts = LockfreeL.getCounts();
                    for(int j = 0; j < 6; j++) {
                        OperationCounts.set(j, OperationCounts.get(j)+oneCounts.get(j));
                    }
                }
                
                // Report test results - take average.
                System.out.println("= = = = = = = = = = = = = = =");
                System.out.println("Avg time(nanosec) per round: " + timeTotal/20/samples);//TimeUnit.NANOSECONDS.toMillis(timeTotal/samples));
                System.out.println("Delete Fail: " + OperationCounts.get(0) / samples+ "; delete success: "+ OperationCounts.get(1) / samples);
                System.out.println("Insert Fail: " + OperationCounts.get(2) / samples+ "; insert success: "+ OperationCounts.get(3) / samples);
                System.out.println("Add Fail: " + OperationCounts.get(4) / samples + "; add success: "+ OperationCounts.get(5) / samples);
                System.out.println("= = = = = = = = = = = = = = =");
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        testLockfreeL(true);
    }
}
