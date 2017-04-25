import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueueLockFree<E> extends AbstractQueue<E> implements Iterable<E> {

    private class Debug {
        AtomicInteger addFailCount = new AtomicInteger();
        AtomicInteger addPassCount = new AtomicInteger();
        AtomicInteger removeFailCount = new AtomicInteger();
        AtomicInteger removePassCount = new AtomicInteger();
    }

    private class Tuple {
        Node aux;
        Node data;
        public Tuple (Node a, Node d) {
            aux = a;
            data = d;
        }
        public boolean equals(Object o) {
            Tuple n = (Tuple) o;
            return (aux == null && n.aux == null || aux.equals(n.aux)) && (data == null && n.data == null || data.equals(n.data));
        }
    }

    private class Node implements Comparable<Node> {
        E element;
//        AtomicReference<Tuple> next;
        AtomicReference<Node> nextAux;
        AtomicReference<Node> nextData;
        Comparator<? super E> comparator;

        Node(E element, Comparator<? super E> comparator) {
            this.element = element;
            this.comparator = comparator;
            this.nextAux = new AtomicReference<>(null);
            this.nextData = new AtomicReference<>(null);
//            this.next = new AtomicReference<>(new Tuple(null, null));
        }

        @Override
        public int compareTo(Node o) {
            if (comparator != null) {
                return comparator.compare(element, o.element);
            }
            return ((Comparable<E>) element).compareTo(o.element);
        }

        public String toString() {
            return element.toString();
        }
    }
    private class AuxiliaryNode extends Node {
        AuxiliaryNode() {
            super(null, null);
        }
    }

    private Comparator<? super E> comparator;
    private ReentrantLock addRemoveHeap;
    private Node head;
    private Debug debug;

    public PriorityQueueLockFree() {
        head = new Node(null, comparator);
        head.nextAux.set(new AuxiliaryNode());
        debug = new Debug();
    }

    public PriorityQueueLockFree(Collection<? extends E> c) {
        this();
        for (E element : c) {
            add(element);
        }
    }

    public PriorityQueueLockFree(Comparator<? super E> comparator) {
        this();
        this.comparator = comparator;

    }


    /****************************************************************************************************************
     * This method inserts the specified element into this priority queue.
     * @param input
     * @return- boolean value whether the element was added successfully
     */
    public boolean add(E input) {
//        Node newNode = new Node(input, comparator);
//        Node newAux = new AuxiliaryNode();
//        Tuple t = new Tuple(newAux, null);
//        newNode.next.set(t);
//        while(true) {
//            Node thisData = this.head;
//            Tuple tuple = thisData.next.get();
//            Node thisAux = tuple.aux;
//            Node nextData = thisAux.next.get().data;
//            while(nextData != null && nextData.compareTo(newNode) < 0) {
//                thisData = nextData;
//                tuple = thisData.next.get();
//                thisAux = tuple.aux;
//                nextData = thisAux.next.get().data;
//            }
//            newAux.next.set(new Tuple(null, nextData));
//            if(thisAux.next.compareAndSet(tuple,newNode)) {
//                debug.addPassCount.incrementAndGet();
//                return true;
//            }
//            else {
//                debug.addFailCount.incrementAndGet();
//                Thread.yield();
//            }
//        }
        Node newNode = new Node(input, comparator);
        Node newAux = new AuxiliaryNode();
        newNode.nextAux.set(newAux);
        while(true) {
            Node thisData = this.head;
            Node thisAux = thisData.nextAux.get();
            Node nextData = thisAux.nextData.get();
            while(nextData != null && nextData.compareTo(newNode) < 0) {
                thisData = nextData;
                thisAux = thisData.nextAux.get();
                nextData = thisAux.nextData.get();
            }
            newAux.nextAux.set(null);
            newAux.nextData.set(nextData);
            if(thisAux.nextData.compareAndSet(nextData,newNode)) {
                debug.addPassCount.incrementAndGet();
                return true;
            }
            else {
                debug.addFailCount.incrementAndGet();
                Thread.yield();
            }
        }
    }

    /****************************************************************************************************************
     * This method removes all of the elements from this priority queue.
     */
    public void clear() {
        Node headAux = head.nextAux.get();
        headAux.nextData.set(null);
    }


    /****************************************************************************************************************
     * Returns the comparator used to order the elements in this queue, or null if this queue is sorted according to
     * the natural ordering of its elements.
     */
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /****************************************************************************************************************
     * This method returns true if this queue contains the specified element.
     */
    public boolean contains(Object o) {
        Node thisData = this.head;
        Node thisAux = thisData.nextAux.get();
        Node nextData = thisAux.nextData.get();
        while(nextData != null) {
            thisData = nextData;
            thisAux = nextData.nextAux.get();
            nextData = thisAux.nextData.get();
            if(thisData.element.equals(o))
                return true;
        }
        return false;
    }


    /****************************************************************************************************************
     * Returns an iterator over the elements in this queue.
     */
    public Iterator<E> iterator() {
        return new HeapIterator(head);
    }

    private class HeapIterator implements Iterator<E> {
        private Node node;

        public HeapIterator(Node head) {
            this.node = head;
        }

        @Override
        public boolean hasNext() {
            Node n = node.nextAux.get();
            return n.nextData.get() != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            node = node.nextAux.get();
            node = node.nextData.get();
            return node.element;
        }

		@Override
		public void remove() {
            PriorityQueueLockFree.this.remove(node);
		}
    }


    /****************************************************************************************************************
     * Copied the code from the add function
     * This method inserts the specified element into this priority queue.
     */
    public boolean offer(E e) {
        return add(e);
    }


    /****************************************************************************************************************
     * This method retrieves, but does not remove, the head of this queue,
     * or returns null if this queue is empty.
     */
    public E peek() {
        Node headAux = head.nextAux.get();
        Node top = headAux.nextData.get();
        if(top == null) {
            return null;
        }
        return top.element;
    }


    /****************************************************************************************************************
     * This method retrieves and removes the head of this queue,
     * or returns null if this queue is empty.
     */
    public E poll() {
        Node thisData = this.head;
        Node thisAux = thisData.nextAux.get();
        Node nextData = thisAux.nextData.get();
        while(true) {
            nextData = thisAux.nextData.get();
            if(nextData == null) return null;
            Node nextAux = nextData.nextAux.get();
            thisAux.nextAux.set(nextAux);
            if(thisAux.nextData.compareAndSet(nextData,nextAux.nextData.get())) {
                debug.removePassCount.incrementAndGet();
                update(thisData);
                break;
            } else {
                debug.removeFailCount.incrementAndGet();
                Thread.yield();
            }
        }
        return nextData == null? null: nextData.element;
    }

    private void update(Node data) {
        Node aux = data.nextAux.get();
        Node next = aux.nextAux.get();
        while(next != null && (next instanceof PriorityQueueLockFree.AuxiliaryNode)) {
            if (!aux.nextData.get().equals(next.nextData.get()))
                break;
//            while (true)
                if (data.nextAux.compareAndSet(aux, next)) break;
//                else Thread.yield();
            aux = next;
            next = next.nextAux.get();
        }
    }


    /****************************************************************************************************************
     * This method removes a single instance of the specified
     * element from this queue, if it is present.
     */
    public boolean remove(Object o) {
        while(true) {
            Node thisData = this.head;
            Node thisAux = thisData.nextAux.get();
            Node nextData = thisAux.nextData.get();
            while(nextData != null) {
                if(nextData.element.equals(o))
                    break;
                thisData = nextData;
                thisAux = nextData.nextAux.get();
                nextData = thisAux.nextData.get();
            }
            if(nextData == null) return false;
            Node nextAux = nextData.nextAux.get();
            thisAux.nextAux.set(nextAux);
            if(thisAux.nextData.compareAndSet(nextData,nextAux.nextData.get())) {
                update(thisData);
                debug.removePassCount.incrementAndGet();
                break;
            } else {
                debug.removeFailCount.incrementAndGet();
                Thread.yield();
            }
        }
        return true;
    }


    /****************************************************************************************************************
     * This method returns the number of elements in this collection.
     */
    public int size() {
        int size = 0;
        Node thisData = this.head;
        Node thisAux = thisData.nextAux.get();
        Node nextData = thisAux.nextData.get();
        while(nextData != null) {
            thisData = nextData;
            thisAux = thisData.nextAux.get();
            nextData = thisAux.nextData.get();
            size++;
        }
        return size;
    }


    /****************************************************************************************************************
     * prints the values of the queue
     */
    public String toString() {
        String out = "[";
        String temp = "";
        Node thisData = this.head;
        Node thisAux = thisData.nextAux.get();
        Node nextData = thisAux.nextData.get();
        while(nextData != null) {
            thisData = nextData;
            thisAux = nextData.nextAux.get();
            nextData = thisAux.nextData.get();
            out += temp + thisData.element.toString();
            temp = ", ";
        }
        out += "]";
        return out;
    }
    public void debug() {
//        return debug;
        System.out.println("Delete Fail: " + debug.removeFailCount.get() + "; delete success: "+ debug.removePassCount.get());
        System.out.println("Add Fail: " + debug.addFailCount.get() + "; add success: "+ debug.addFailCount.get());
    }

}
