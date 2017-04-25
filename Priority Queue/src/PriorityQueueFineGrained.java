import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueueFineGrained<E> extends AbstractQueue<E> implements Iterable<E> {

    private class Node implements Comparable<Node> {
        E element;
        ReentrantLock lock;
        Comparator<? super E> comparator;
        Node next;

        Node(E element, Comparator<? super E> comparator) {
            this.element = element;
            this.comparator = comparator;
            this.lock = new ReentrantLock();
            this.next = null;
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
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

    private Comparator<? super E> comparator;
    private ReentrantLock addRemoveHeap;
    private Node head;

    public PriorityQueueFineGrained() {
        head = new Node(null, comparator);
    }

    public PriorityQueueFineGrained(Collection<? extends E> c) {
        this();
        for (E element : c) {
            add(element);
        }
    }

    public PriorityQueueFineGrained(Comparator<? super E> comparator) {
        this();
        this.comparator = comparator;

    }


    /****************************************************************************************************************
     * This method inserts the specified element into this priority queue.
     * @param input
     * @return- boolean value whether the element was added successfully
     */
    public boolean add(E input) {
        Node e = new Node(input, comparator);
        Node head = this.head;
        head.lock();
        try {
            Node next = head.next;
            while (next != null && next.compareTo(e) < 0) {
                next.lock();
                head.unlock();
                head = next;
                next = next.next;
            }
            e.next = next;
            head.next = e;
        } finally {
            head.unlock();
        }
        return true;
    }

    /****************************************************************************************************************
     * This method removes all of the elements from this priority queue.
     */
    public void clear() {
        head.next = null;
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
        Node head = this.head;
        head.lock();
        try {
            Node next = head.next;
            while (next != null) {
                next.lock();
                head.unlock();
                head = next;
                next = next.next;
                if (head.element.equals(o))
                    return true;
            }
        } finally {
            head.unlock();
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
            return node.next != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            node = node.next;
            return node.element;
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
        head.lock();
        try {
            Node next = head.next;
            if (next != null) {
                next.lock();
                try {
                    return next.element;
                } finally {
                    next.unlock();
                }
            }
        } finally {
            head.unlock();
        }
        return null;
    }


    /****************************************************************************************************************
     * This method retrieves and removes the head of this queue,
     * or returns null if this queue is empty.
     */
    public E poll() {
        head.lock();
        try {
            Node next = head.next;
            if (next != null) {
                next.lock();
                try {
                    head.next = next.next;
                    return next.element;
                } finally {
                    next.unlock();
                }
            }
        } finally {
            head.unlock();
        }
        return null;
    }


    /****************************************************************************************************************
     * This method removes a single instance of the specified
     * element from this queue, if it is present.
     */
    public boolean remove(Object o) {
        Node head = this.head;
        head.lock();
        try {
            Node next = head.next;
            while (next != null) {
                next.lock();
                if (next.element.equals(o)) {
                    head.next = next.next;
                    next.unlock();
                    return true;
                }
                head.unlock();
                head = next;
                next = next.next;
            }
        } finally {
            head.unlock();
        }
        return false;
    }


    /****************************************************************************************************************
     * This method returns the number of elements in this collection.
     */
    public int size() {
        int size = 0;
        Node head = this.head;
        head.lock();
        try {
            Node next = head.next;
            while (next != null) {
                next.lock();
                head.unlock();
                head = next;
                next = next.next;
                size++;
            }
        } finally {
            head.unlock();
        }
        return size;
    }


    /****************************************************************************************************************
     * prints the values of the queue
     */
    public String toString() {
        String out = "[";
        String temp = "";
        Node head = this.head;
        head.lock();
        try {
            Node next = head.next;
            while (next != null) {
                next.lock();
                head.unlock();
                head = next;
                next = next.next;
                out += temp + head.element.toString();
                temp = ", ";
            }
        } finally {
            head.unlock();
        }
        out += "]";
        return out;
    }

}
