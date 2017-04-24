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
           System.out.println("CAS failure");
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
            // Debug print
            // System.out.println("Update iterator");
            // Only set PrevDataNode.next when no nodes are added in between
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
                break;
            }
            else {
                Thread.yield();
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
                tail.data = data;
                this.tail = newTailNode;
                this.length.incrementAndGet();
                return;
            }
            else {
                Thread.yield();
            }
        }
    }

    public boolean delete(LockfreeIterator i) {
        while(true) {
            if(this.length.get() == 0) return false;
            Node nextAuxNode = i.DataNode.nextNode();
            if(i.AuxNode.compareAndSetNext(i.DataNode, nextAuxNode)) {
                this.length.decrementAndGet();
                break;
            }
            i.update();                 // You cannot miss this.
        }
        i.update();
        return true;
    }
}
