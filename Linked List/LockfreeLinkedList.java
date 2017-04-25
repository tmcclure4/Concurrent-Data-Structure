import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

class Node {
    Object data = null;
    AtomicReference<Node> next = new AtomicReference<Node>(null);
    boolean isAuxNode = false;
    public Node() {
    }
    public Node(Object data) {
        this.data = data;
    }

    public void setAuxNode() {
        this.isAuxNode = true;
    }
    public void setNotAuxNode() {
        this.isAuxNode = false;
    }
    public Node nextNode() {
        return next.get();
    }
    public void setNext(Node n) {
        next.set(n);
    }

    public boolean compareAndSetNext(Node OldValue, Node SetValue) {
        boolean retVal = this.next.compareAndSet(OldValue, SetValue);
        if(retVal == false) {
           // System.out.println("CAS failure");
        }
        return retVal;
    }
}

class LockfreeIterator {
    Node PrevDataNode;
    Node AuxNode;
    Node DataNode;
    public LockfreeIterator() {
        PrevDataNode = new Node();
        AuxNode = new Node();
        DataNode = new Node();
    }
    public LockfreeIterator(Node pdnode, Node anode, Node dnode) {
        this.PrevDataNode = pdnode;
        this.AuxNode = anode;
        this.DataNode = dnode;
    }

    public Object get() {
        return DataNode.data;
    }
    // Find the data node given the previous node and the auxiliary node;
    // clear redundant auxiliary nodes between the data nodes
    public void update() {
        // If AuxNode's next node is DataNode, everything is fine
        if(this.AuxNode.nextNode() == this.DataNode) return;
        // Else we set a couple of temp nodes and search for the DataNode
        Node pNode = this.AuxNode;
        Node nNode = pNode.nextNode();
        while(nNode != null && nNode.isAuxNode == true) {
            // Set PrevDataNode.next only when no nodes are added in between
            this.PrevDataNode.compareAndSetNext(pNode, nNode);
            pNode = nNode;
            nNode = nNode.nextNode();
        }
        this.AuxNode = pNode;
        this.DataNode = nNode;
    }

    public boolean hasNext() {
        return (DataNode.nextNode() != null);
    }
    public boolean next() {
        if(this.hasNext()) {
            this.PrevDataNode = DataNode;
            this.AuxNode = DataNode.nextNode();
            this.update();
            return true;
        }
        else {
            return false;
        }
    }
}

class LockfreeLinkedList {
    Node head = new Node();
    Node tail = new Node();
    AtomicInteger length = new AtomicInteger(0);

    private AtomicInteger deleteFailCount = new AtomicInteger(0);
    private AtomicInteger deleteSuccessCount = new AtomicInteger(0);
    private AtomicInteger insertFailCount = new AtomicInteger(0);
    private AtomicInteger insertSuccessCount = new AtomicInteger(0);
    private AtomicInteger addFailCount = new AtomicInteger(0);
    private AtomicInteger addSuccessCount = new AtomicInteger(0);

    public LockfreeLinkedList() {
        Node headAuxNode = new Node();
        headAuxNode.setNext(tail);
        head.setNext(headAuxNode);
    }
    public LockfreeLinkedList(int initSize) {
        try {
            if(initSize < 0)
                throw new Exception("The size of the LockfreeLinkedList should be non-negative.");
            else {
                Node headAuxNode = new Node();
                headAuxNode.setNext(tail);
                head.setNext(headAuxNode);
                if(initSize != 0) {
                    this.length.set(initSize);
                    for(int i = 0; i < initSize; i++) {
                        this.insert(this.begin(), null);
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }

    public ArrayList<Integer> getCounts() {
        ArrayList<Integer> counts = new ArrayList<Integer>();
        counts.add(deleteFailCount.get());
        counts.add(deleteSuccessCount.get());
        counts.add(insertFailCount.get());
        counts.add(insertSuccessCount.get());
        counts.add(addFailCount.get());
        counts.add(addSuccessCount.get());
        return counts;
    }
    public void reportOperationCounts() {
        System.out.println("= = = = = = = = = = = = = = =");
        System.out.println("Delete Fail: " + this.deleteFailCount + "; delete success: "+this.deleteSuccessCount);
        System.out.println("Insert Fail: " + this.insertFailCount + "; insert success: "+this.insertSuccessCount);
        System.out.println("Add Fail: " + this.addFailCount + "; add success: "+this.addSuccessCount);
        System.out.println("= = = = = = = = = = = = = = =");
    }

    public LockfreeIterator begin() {
        LockfreeIterator beg = new LockfreeIterator();
        beg.PrevDataNode = this.head;
        beg.AuxNode = beg.PrevDataNode.nextNode();
        beg.DataNode = null;

        beg.update();
        return beg;
    }
    // Insert at the position of iterator i, and move forward the iterator
    public boolean insert(LockfreeIterator i, Object data) {
        while(true) {
            i.update();                         // You cannot miss this.
            // Create new data node and its aux node
            Node newDataNode = new Node(data);
            Node newAuxNode = new Node();
            newAuxNode.setAuxNode();
            newDataNode.setNext(newAuxNode);
            newAuxNode.setNext(i.DataNode);

            if(i.AuxNode.compareAndSetNext(i.DataNode, newDataNode)) {
                this.length.incrementAndGet();
                this.insertSuccessCount.incrementAndGet();
                break;
            }
            else {
                this.insertFailCount.incrementAndGet();
            }
        }

        // Move i.DataNode forward
        i.update();
        return true;
    }
    // Add the node at the end of the list
    public void add(Object data) { 
        while(true) {
            Node newTailNode = new Node();
            Node newAuxNode = new Node();
            newAuxNode.setAuxNode();
            newAuxNode.setNext(newTailNode);
            if(this.tail.compareAndSetNext(null, newAuxNode)) {
                this.tail = newTailNode;  // Update tail immediately
                tail.data = data;
                this.length.incrementAndGet();
                this.addSuccessCount.incrementAndGet();
                return;
            }
            else {
                this.addFailCount.incrementAndGet();
            }
        }
    }

    public boolean delete(LockfreeIterator i) {
        while(true) {
            i.update();                 // You cannot miss this.
            // If the iterator is at the end of the list
            if(i.AuxNode.nextNode() == this.tail || 
               this.length.get() == 0)  {
                this.deleteFailCount.incrementAndGet();
                return false;
            }
            Node nextAuxNode = i.DataNode.nextNode();
            if(i.AuxNode.compareAndSetNext(i.DataNode, nextAuxNode)) {
                this.length.decrementAndGet();
                this.deleteSuccessCount.incrementAndGet();
                break;
            }
            else {
                this.deleteFailCount.incrementAndGet();
            }
        }
        i.update();
        return true;
    }
    public boolean poll() {
        return delete(this.begin());
    }
}
