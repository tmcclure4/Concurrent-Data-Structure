import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue<E> extends AbstractQueue<E> {

    private class Entry implements Comparable<Entry> {
        E element;
        ReentrantLock lock;
        Comparator<? super E> comparator;

        Entry(E element, Comparator<? super E> comparator) {
            this.element = element;
            this.comparator = comparator;
            this.lock = new ReentrantLock();
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }

        @Override
        public int compareTo(Entry o) {
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
    private ArrayList<Entry> heap;//array list used as a heap
    private ReentrantLock addRemoveHeap;

    public PriorityQueue() {
        this(11);
    }

    public PriorityQueue(Collection<? extends E> c) {
        this();
        for (E element : c) {
            add(element);
        }
    }

    public PriorityQueue(int initialCapacity) {
        heap = new ArrayList<Entry>(initialCapacity);
        addRemoveHeap = new ReentrantLock();
    }

    public PriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        this(initialCapacity);
        this.comparator = comparator;

    }


    /****************************************************************************************************************
     * This method inserts the specified element into this priority queue.
     * @param input
     * @return- boolean value whether the element was added successfully
     */
    //figure out when i would return false
    public boolean add(E input) {
        int i = -1;
        boolean success = true;
        Entry entry = new Entry(input, comparator);
        entry.lock();
        try {
            addRemoveHeap.lock();
            try {
                i = heap.size();
                success = heap.add(entry);
            } catch (Exception e) {
                return false;
            } finally {
                addRemoveHeap.unlock();
            }
            if (!success || i < 0) return false;
            heapifyUp(i);
        } finally {
            if(entry.lock.isLocked())
                entry.unlock();
        }

        return true;
    }

    private int heapifyUp(int i) {
        Entry front = heap.get(i);
        // heapify up
        while (i > 0) {
            i = (i - 1) / 2;
            Entry parent = heap.get(i);
            parent.lock();
            if(front.compareTo(parent) == -1) {
                swap(front, parent);
                front.unlock();
                front = parent;
            } else {
                parent.unlock();
                front.unlock();
                break;
            }
        }

        return i;
    }

    /****************************************************************************************************************
     * This method removes all of the elements from this priority queue.
     */
    public void clear() {
        addRemoveHeap.lock();
        try {
            heap.clear();
        } finally {
            addRemoveHeap.unlock();
        }
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
        for (Entry temp : heap) {
            if (temp.element.equals(o)) {
                return true;
            }
        }
        return false;
    }


    /****************************************************************************************************************
     * Returns an iterator over the elements in this queue.
     */
    public Iterator<E> iterator() {
        return new HeapIterator(heap);
    }

    private class HeapIterator implements Iterator<E> {
        private Iterator<Entry> heap;

        public HeapIterator(ArrayList<Entry> heap) {
            this.heap = heap.iterator();
        }

        @Override
        public boolean hasNext() {
            return heap.hasNext();
        }

        @Override
        public E next() {
            return heap.next().element;
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
        if (!heap.isEmpty()) {
            Entry e = heap.get(0);
            e.lock();
            E ret;
            try {
                ret = e.element;
            } finally {
                e.unlock();
            }
            return ret;
        }
        return null;
    }


    /****************************************************************************************************************
     * This method retrieves and removes the head of this queue,
     * or returns null if this queue is empty.
     */
    public E poll() {
        // lock first element
        // lock last element
        // swap elements
        // heapify down
        // return
        E returnValue = null;
        if (heap.size() > 1) {
            Entry front = heap.get(0);
            front.lock();
            returnValue = removeAndHeapify(0);
        } else {
            addRemoveHeap.lock();
            try {
                if (!heap.isEmpty()) {
                    return heap.remove(0).element;
                }
            } finally {
                addRemoveHeap.unlock();
            }
        }
        return returnValue;
    }

    private E removeAndHeapify(int i) {
        Entry front = heap.get(i);
        E returnValue = null;
        try {
            int endIndex = heap.size() - 1;
            Entry end = heap.get(endIndex);
            end.lock();
            try {
                addRemoveHeap.lock();
                try {
                    returnValue = swap(end, front);
                    heap.remove(end);
                } finally {
                    addRemoveHeap.unlock();
                }
            } finally {
                end.unlock();
            }
            heapifyUp(i);
            front = heap.get(i);

            // heapify down
            while (2 * i + 1 < heap.size()) {
                Entry left = heap.get(2 * i + 1);
                left.lock();
                Entry right = null;
                if (2 * i + 2 < heap.size()) {
                    right = heap.get(2 * i + 2);
                    right.lock();
                }
                if (left.compareTo(front) == -1 || (right != null && right.compareTo(front) == -1)) {
                    if (right == null || left.compareTo(right) == -1) {
                        swap(front, left);
                        i = 2 * i + 1;
                        front.unlock();
                        front = left;
                        if(right != null)
                            right.unlock();
                    } else {
                        swap(front, right);
                        i = 2 * i + 2;
                        front.unlock();
                        front = right;
                        left.unlock();
                    }
                } else {
                    left.unlock();
                    if(right != null) {
                        right.unlock();
                    }
                    break;
                }
            }
        } finally {
            front.unlock();
        }
        return returnValue;
    }

    private E swap(Entry a, Entry b) {
        E temp = b.element;
        b.element = a.element;
        a.element = temp;
        return temp;
    }


    /****************************************************************************************************************
     * This method removes a single instance of the specified
     * element from this queue, if it is present.
     */
    public boolean remove(Object o) {
        int i = -1;
        for (int count = 0; count < heap.size(); count++) {
            Entry e = heap.get(count);
            e.lock();
            try {
                if (e.equals(o)) {//found the element so remove it and reorder the heap
                    i = count;
                    removeAndHeapify(i);
                }
            } finally {
                if (e.lock.isLocked())
                    e.unlock();
            }
        }
        return false;
    }


    /****************************************************************************************************************
     * This method returns the number of elements in this collection.
     */
    public int size() {
        return heap.size();//take away 1 for the null at index 0
    }


    /****************************************************************************************************************
     * This method returns an array containing all of the elements in this queue.
     */
    public Object[] toArray() {
        return heap.toArray();
    }


    public <T> T[] toArray(T[] a) {
        return heap.toArray(a);
    }


    /****************************************************************************************************************
     * prints the value of the heap
     */
    public void print() {
        int index = 0;
        for (Entry temp : heap) {
            System.out.println("Index = " + index + " ==> " + temp);
            index++;
        }
        System.out.println();
    }


//    /****************************************************************************************************************
//     * This organizes the heap based on the concept of parent node must be greater than it's leaf nodes
//     */
//    protected void organizeHeap() {
//        //if the new value is greater than the parent, swap the two values
//        //keep doing this until the leaf node is less than the parent, of the new value is the root
//        int index = heap.size() - 1;
//        T tempStorage;
//        while (index != 1) {
//            if ((heap.get(index)).compareTo(heap.get(index / 2)) > 0) {//new value is greater than the parent node, swap the two values
//                tempStorage = heap.get(index);
//                heap.set(index, heap.get(index / 2));
//                heap.set(index / 2, tempStorage);
//                index = index / 2;//set the new index value
//            } else {//new value isn't greater than the parent
//                break;
//            }
//        }
//    }
//
//    /****************************************************************************************************************
//     * This removes the top object on the heap and
//     * reorganizes the heap
//     */
//    protected void removeAndReorder(ArrayList<T> tempheap) {
//        tempheap.set(1, tempheap.get(tempheap.size() - 1));//replace the root with the rightmost node and remove the last node
//        tempheap.remove(tempheap.size() - 1);
//        reorderHeapTopBottom(1, tempheap);
//    }
//
//
//    /****************************************************************************************************************
//     * This reorders the heap when the root node needs to move down the heap
//     */
//    protected void reorderHeapTopBottom(int startIndex, ArrayList<T> tempheap) {
//        int parentLeaf = startIndex;
//        int leftLeaf = parentLeaf * 2;
//        int rightLeaf = leftLeaf + 1;
//        while (rightLeaf < tempheap.size()) {
//            //make sure at least one child node is greater than the parent node
//            if (tempheap.get(leftLeaf).compareTo(tempheap.get(parentLeaf)) > 0 || tempheap.get(rightLeaf).compareTo(tempheap.get(parentLeaf)) > 0) {
//                if (tempheap.get(leftLeaf).compareTo(tempheap.get(rightLeaf)) > 0) {//left leaf is greater than the right leaf
//                    //swap the left leaf and parent nodes
//                    T temp = (T) tempheap.get(leftLeaf);
//                    tempheap.set(leftLeaf, tempheap.get(parentLeaf));
//                    tempheap.set(parentLeaf, temp);
//                    parentLeaf = leftLeaf;
//                } else {//right node is greater
//                    //swap the right leaf and parent nodes
//                    T temp = (T) tempheap.get(rightLeaf);
//                    tempheap.set(rightLeaf, tempheap.get(parentLeaf));
//                    tempheap.set(parentLeaf, temp);
//                    parentLeaf = rightLeaf;
//                }
//                leftLeaf = parentLeaf * 2;
//                rightLeaf = leftLeaf + 1;
//            } else
//                break;
//        }
//
//        if (leftLeaf < tempheap.size()) {//there is a left node but no right node, check if child is greater than parent
//            if (tempheap.get(leftLeaf).compareTo(tempheap.get(parentLeaf)) > 0) {//left leaf is greater than the parent
//                //swap the left leaf and parent nodes
//                T temp = (T) tempheap.get(leftLeaf);
//                tempheap.set(leftLeaf, tempheap.get(parentLeaf));
//                tempheap.set(parentLeaf, temp);
//                parentLeaf = leftLeaf;
//            }
//        }
//    }
//
//
//    /****************************************************************************************************************
//     * This orders the elements in the priority from max to min
//     */
//    protected Object[] orderElements() {
//        Object[] elementArray = new Object[heap.size() - 1];
//
//        //copy the heap
//        ArrayList<T> copyheap = new ArrayList<T>(heap);
//        int heapSize = copyheap.size();
//        for (int count = 0; count < (heapSize - 1); count++) {
//            elementArray[count] = copyheap.get(1);
//            removeAndReorder(copyheap);
//        }
//        return elementArray;
//
//    }


}
