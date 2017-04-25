
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;




public class PriorityQueueLockFree<T extends Comparable<T>, E> {
	
	
	private PriorityQueueNode head = new PriorityQueueNode(null);
	private PriorityQueueNode tail= new PriorityQueueNode(null);
	private AtomicReference<PriorityQueueNode> atomicRefTemp=new AtomicReference<PriorityQueueNode>(null);
	private AtomicReference<PriorityQueueNode> atomicRefTail= new AtomicReference<PriorityQueueNode>(null);
	private AtomicReference<PriorityQueueNode> atomicRefHead=new AtomicReference<PriorityQueueNode>(null);
	private AtomicInteger numberOfElements = new AtomicInteger();
	
	
	/***********************************************************************************************************************
	 * This method inserts the specified element into this priority queue.
	 */
	public boolean add(E input){//PriorityQueueNode previousNode
		PriorityQueueNode newNode=new PriorityQueueNode((Comparable)input);
		PriorityQueueNode tempNode=head;//used to iterate through the linked list
		PriorityQueueNode lastNodeGone=tempNode;
		
		if(head.getData()!=null){//not first element
			
			do{
				atomicRefTemp.set(tempNode);     //newNode.getData().compareTo(tempNode.getData())>0
				if(tempNode.getData().compareTo(newNode.getData())>0){////new value is less than the current node, insert the node
					newNode.setNext(tempNode);
					newNode.setPrevious(tempNode.getPrevious());
					//newNode.setPrevious(tempNode.getPrevious());
					if(atomicRefTemp.compareAndSet(tempNode, newNode)){
						if(newNode.getPrevious()==null){
							head=newNode;
						}
						else tempNode.getPrevious().setNext(newNode);
						
						tempNode.setPrevious(newNode);
						
						numberOfElements.incrementAndGet();
						return true;
					}
				}
				lastNodeGone=tempNode;
				tempNode=tempNode.getNext();
			}while(tempNode!=null);
				
			
			//if it gets here then that means its the highest data in the priority queue, lowest priority
			newNode.setPrevious(lastNodeGone);
			if(atomicRefTail.compareAndSet(lastNodeGone, newNode)){
				lastNodeGone.setNext(newNode);
				tail=newNode;
				return true;
			}
			
		}
		else{//first element
			if(atomicRefHead.compareAndSet(null,newNode)){
				atomicRefTail.compareAndSet(null, newNode);
				head=newNode;
				tail=newNode;
				numberOfElements.incrementAndGet();
				return true;
			}
		}
		
		Thread.yield();
		return false;
	}
	
	
	/*************************************************************************************************************************
	 * This method removes all of the elements from this priority queue.
	 */
	public void clear(){
		//just clear the head
		head=null;
	}
	
	/************************************************************************************************************************
	 * This method returns the comparator used to order the elements in this queue, 
	 * or null if this queue is sorted according to the natural ordering of its elements.
	 */
	public Comparator<? super E> comparator(){
		return null;
	}
	
	/************************************************************************************************************************
	 * 
	 */
	public boolean contains(Object o){
		PriorityQueueNode tempNode=head;
		while(true){
			if(tempNode.getData().equals(o)){
				return true;
			}
			tempNode=tempNode.getNext();
			if(tempNode==null) break;
		}
		
		return false;
	}
	
	/************************************************************************************************************************
	 * 
	 */
	public Iterator<E> iterator(){
		return null;
	}
	
	/************************************************************************************************************************
	 * This method inserts the specified element into this priority queue.
	 */
	public boolean offer(E e){//its the same as the add method
		return this.add(e);
		
	}
	
	/************************************************************************************************************************
	 * 
	 */
	public E peek(){
		return null;
		
	}
	
	/************************************************************************************************************************
	 * 
	 */
	public E poll(){
		return null;
		
	}
	
	/************************************************************************************************************************
	 * This method removes a single instance of the specified element from this queue, if it is present
	 */
	public boolean remove(Object o){
		PriorityQueueNode tempNode=head;//used to iterate through the linked list
		PriorityQueueNode lastNodeGone=tempNode;
		
		if(tempNode.getData()!=null){//not first node
			do{
				atomicRefTemp.set(tempNode);
				if(tempNode.getData().compareTo(o)==0){//its the same
					if(atomicRefTemp.compareAndSet(tempNode, null)){
						tempNode.getPrevious().setNext(tempNode.getNext());
						tempNode.getNext().setPrevious(tempNode.getPrevious());
						break;
					}
				}
				lastNodeGone=tempNode;
				tempNode=tempNode.getNext();
			}while(tempNode!=null);
		}
		
		return false;
		
	}
	
	/************************************************************************************************************************
	 * 
	 */
	public int size(){
		return 0;
		
	}
	
	/************************************************************************************************************************
	 * 
	 */
	public Object[] toArray(){
		return null;
		
	}
	
	/************************************************************************************************************************
	 * 
	 */
	public <T> T[] toArray(T[] a){
		return a;
		
	}
	
	/************************************************************************************************************************
	 * This prints the linked list
	 */
	public void printNodes(){
		PriorityQueueNode printNode = head;
		if(head!=null){//take into account if theres no element
			do{
				System.out.println(printNode.getData());
				printNode=printNode.getNext();
			}while(printNode!=null);
			
			System.out.println();//add new line to make it easier to read
		}
		else{
			System.out.println("No Elements\n");
		}
		
	}
	
		
}

/*
while(tempNode.hasNext()){
				atomicRefTemp.set(tempNode);
				if(newNode.getData().compareTo(tempNode.getData())>0){////new value is greater than the current node, insert the node
					newNode.setNext(tempNode);
					newNode.setPrevious(tempNode.getPrevious());
					//newNode.setPrevious(tempNode.getPrevious());
					if(atomicRefTemp.compareAndSet(tempNode, newNode)){
						tempNode.setPrevious(newNode);
						if(newNode.getPrevious()==null){
							head=newNode;
						}
						numberOfElements.incrementAndGet();
						return true;
					}
				}
				tempNode=tempNode.getNext();
			}
*/



/*import java.util.Comparator;
import java.util.Iterator;

public class PriorityQueueLockFree<T extends Comparable<T>, E> {
	
	private LockfreeIterator i = new LockfreeIterator();
	private LockfreeLinkedList linkedList = new LockfreeLinkedList();
	
	*//***********************************************************************************************************************
	 * This method inserts the specified element into this priority queue.
	 *//*
	public boolean add(E input){
		if(i.get()!=null){
			while(i.hasNext()){
				if( ((Comparable<T>) input).compareTo((T) i.get())>0){//new value is greater than the current node, insert the node
					linkedList.insert(i,input);
					System.out.println(i.get()+"\n");
					return true;
				}
				i.next();
			}
			
		}
		else{//first element
			linkedList.insert(i,input);
			System.out.println(i.get()+"\n");
			return true;
		}
		
		return false;
		
		
	}
	
	
	*//*************************************************************************************************************************
	 * This method removes all of the elements from this priority queue.
	 *//*
	public void clear(){
		//just clear the head
		
	}
	
	*//************************************************************************************************************************
	 * This method returns the comparator used to order the elements in this queue, 
	 * or null if this queue is sorted according to the natural ordering of its elements.
	 *//*
	public Comparator<? super E> comparator(){
		return null;
	}
	
	*//************************************************************************************************************************
	 * 
	 *//*
	public boolean contains(Object o){
		
		
		return false;
	}
	
	*//************************************************************************************************************************
	 * 
	 *//*
	public Iterator<E> iterator(){
		return null;
	}
	
	*//************************************************************************************************************************
	 * This method inserts the specified element into this priority queue.
	 *//*
	public boolean offer(E e){//its the same as the add method
		return this.add(e);
		
	}
	
	*//************************************************************************************************************************
	 * 
	 *//*
	public E peek(){
		return null;
		
	}
	
	*//************************************************************************************************************************
	 * 
	 *//*
	public E poll(){
		return null;
		
	}
	
	*//************************************************************************************************************************
	 * 
	 *//*
	public boolean remove(Object o){
		return false;
		
	}
	
	*//************************************************************************************************************************
	 * 
	 *//*
	public int size(){
		return 0;
		
	}
	
	*//************************************************************************************************************************
	 * 
	 *//*
	public Object[] toArray(){
		return null;
		
	}
	
	*//************************************************************************************************************************
	 * 
	 *//*
	public <T> T[] toArray(T[] a){
		return a;
		
	}
	
	*//************************************************************************************************************************
	 * This prints the linked list
	 *//*
	public void printNodes(){
		
		if(i.get()!=null){//take into account if theres no element
			while(true){
				System.out.println(i.get());
				System.out.println(i.hasNext());
				if(i.hasNext()) i.next();
				else break;
			}
			
			System.out.println();//add new line to make it easier to read
		}
		else{
			System.out.println("No Elements\n");
		}
		
	}
}*/













