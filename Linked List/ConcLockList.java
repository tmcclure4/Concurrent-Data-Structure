import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcLockList { //Adapted from Herlihy's Book
  private Node head;
  private Node tail;
  public ConcLockList() {
    head      = new Node(Integer.MIN_VALUE);
    tail      = new Node(null);
    head.next = tail;
  }
  public boolean insert(int index, Object item) {
    if(index < 0) return false;
    int currIndex = 0;
    head.lock();
    Node pred = head;
    try {
      Node curr = pred.next;
      curr.lock();
      try {
        while (currIndex < index) {
          if(curr == this.tail) {
              return false;
          }
          currIndex++;
          pred.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock();
        }
        Node newNode = new Node(item);
        newNode.next = curr;
        pred.next = newNode;
        return true;
      } finally {
        curr.unlock();
      }
    } finally {
      pred.unlock();
    }
  }
  public void add(Object item) {
      Node newNode = new Node();
      Node lastTail = this.tail;
      lastTail.lock();
      while(lastTail.next != null)  {
          lastTail.unlock();
          lastTail = lastTail.next;
          lastTail.lock();
      }
      lastTail.item = item;
      lastTail.next = newNode;
      this.tail = newNode;
      lastTail.unlock();
  }
  public Object remove(int index) {
    if(index < 0) return null;
    Node pred = null, curr = null;
    int currIndex = 0;
    head.lock();
    try {
      pred = head;
      curr = pred.next;
      curr.lock();
      try {
        while (currIndex < index) {
          if(curr == this.tail) return null;
          currIndex++;
          pred.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock();
        }
        pred.next = curr.next;      // Delete current node
        return curr.item;
      } finally {
        curr.unlock();
      }
    } finally {
      pred.unlock();
    }
  }
  public boolean remove(Object item) {
    Node pred = null, curr = null;
    head.lock();
    try {
      pred = head;
      curr = pred.next;
      curr.lock();
      try {
        while (item.equals(curr.item) != true && curr != this.tail) {
          pred.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock();
        }
        if(curr == this.tail) return false;
        else {
            pred.next = curr.next;
            return true;
        }
      } finally {
        curr.unlock();
      }
    } finally {
      pred.unlock();
    }
  }
  public Object poll() {
      return remove(0);
  }
  public boolean contains(Object item) {
    Node pred = null, curr = null;
    head.lock();
    try {
      pred = head;
      curr = pred.next;
      curr.lock();
      try {
        while (curr != this.tail && item.equals(curr.item) != true) {
          pred.unlock();
          pred = curr;
          curr = curr.next;
          curr.lock();
        }
        if(curr == this.tail) return false;
        else return true;
      } finally {
        curr.unlock();
      }
    } finally {
      pred.unlock();
    }
  }
  private class Node {
    Object item;
    Node next = null;
    Lock lock;
    Node() {
        this.item = null;
        this.lock = new ReentrantLock();
    }
    Node(Object item) {
      this.item = item;
      this.lock = new ReentrantLock();
    }
    void lock() {lock.lock();}
    void unlock() {lock.unlock();}
  }
}

