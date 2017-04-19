import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class PriorityQueue<T extends Comparable<T>, E> {
	
	private ArrayList<T> heap;//array list used as a heap
	
	
	
	PriorityQueue(){
		heap=new ArrayList<T>();
		heap.add(null);//index 0 is nothing in a heap, so make it null
	}
	
	
	/****************************************************************************************************************
	 * This method inserts the specified element into this priority queue. 
	 * @param input
	 * @return- boolean value whether the element was added successfully 
	 */
	//figure out when i would return false
	public boolean add(T input){
		if(heap.size()==0){//index 0 should always be null
			heap.add(null);
			heap.add(input);
		}
		else{
			heap.add(input);//put new element in the back of the array
			
			this.organizeHeap();
		}
		return true;
	}
	
	/****************************************************************************************************************
	 * This method removes all of the elements from this priority queue.
	 */
	public void clear(){
		for(int count=heap.size()-1;count>=0;count--){
			heap.remove(count);
		}
	}
	
	
	/****************************************************************************************************************
	 * 
	 */
	//TODO
	public Comparator<? super E> comparator(){
		return null;
	}
	
	/****************************************************************************************************************
	 * This method returns true if this queue contains the specified element.
	 */
	public boolean contains(Object o){
		boolean firstTime = true;//do not want to consider element at index 0 which is null
		for(T tempArray:heap){
			if(!firstTime){
				if(tempArray.equals(o)){
					return true;
				}
			}
			else//first time going through
				firstTime=false;
		}
		return false;
	}
	
	
	/****************************************************************************************************************
	 * 
	 */
	//TODO
	public Iterator<E> iterator(){
		return null;
	}
	
	
	/****************************************************************************************************************
	 * Copied the code from the add function
	 * This method inserts the specified element into this priority queue.
	 */
	public boolean offer(E e){
		if(heap.size()==0){//index 0 should always be null
			heap.add(null);
			heap.add((T) e);
		}
		else{
			heap.add((T) e);//put new element in the back of the array
			
			this.organizeHeap();
		}
		return true;
	}
	
	
	/****************************************************************************************************************
	 * This method retrieves, but does not remove, the head of this queue, 
	 * or returns null if this queue is empty.
	 */
	public E peek(){
		if(heap.size()>1)
			return (E) heap.get(1);
		else
			return null;
	}
	
	/****************************************************************************************************************
	 * This method retrieves and removes the head of this queue, 
	 * or returns null if this queue is empty.
	 */
	public E poll(){
		if(heap.size()>1){
			E queueHead=(E) heap.get(1);//get the top index
			removeAndReorder(heap);
			return queueHead;
		}
		else
			return null;
	}
	

	/****************************************************************************************************************
	 * This method removes a single instance of the specified 
	 * element from this queue, if it is present.
	 */
	public boolean remove(Object o){
		for(int count=1; count<heap.size(); count++){
			if(heap.get(count).equals(o)){//found the element so remove it and reorder the heap
				
				//put the right most node in the node that was just removed and remove the right most node
				heap.set(count,heap.get(heap.size()-1));
				heap.remove(heap.size()-1);
				reorderHeapTopBottom(count, heap);
				return true;
			}
		}
		return false;
	}
	
	
	/****************************************************************************************************************
	 * This method returns the number of elements in this collection.
	 */
	public int size(){
		return heap.size()-1;//take away 1 for the null at index 0
	}
	
	
	/****************************************************************************************************************
	 * This method returns an array containing all of the elements in this queue.
	 */
	public Object[] toArray(){
		return orderElements();
	}
	
	
	public <T> T[] toArray(T[] a){
		return a;
	}
	
	
	/****************************************************************************************************************
	 * prints the value of the heap
	 */
	public void print(){
		int index=0;
		for(T temp: heap){
			System.out.println("Index = " + index + " ==> " +temp);
			index++;
		}
		System.out.println();
	}
	
	
	/****************************************************************************************************************
	 * This organizes the heap based on the concept of parent node must be greater than it's leaf nodes
	 */
	protected void organizeHeap(){
		//if the new value is greater than the parent, swap the two values
		//keep doing this until the leaf node is less than the parent, of the new value is the root
		int index=heap.size()-1;
		T tempStorage;
		while(index!=1){
			if((heap.get(index)).compareTo(heap.get(index/2))>0){//new value is greater than the parent node, swap the two values
				tempStorage=heap.get(index);
				heap.set(index,heap.get(index/2));
				heap.set(index/2, tempStorage);
				index=index/2;//set the new index value
			}
			else{//new value isn't greater than the parent
				break;
			}
		}
	}
	
	/****************************************************************************************************************
	 * This removes the top object on the heap and
	 * reorganizes the heap 
	 */
	protected void removeAndReorder(ArrayList<T> tempheap) {
		tempheap.set(1, tempheap.get(tempheap.size()-1));//replace the root with the rightmost node and remove the last node
		tempheap.remove(tempheap.size()-1);
		reorderHeapTopBottom(1, tempheap);
	}
	
	
	/****************************************************************************************************************
	 * This reorders the heap when the root node needs to move down the heap
	 */
	protected void reorderHeapTopBottom(int startIndex, ArrayList<T> tempheap){
		int parentLeaf = startIndex;
		int leftLeaf = parentLeaf*2;
		int rightLeaf = leftLeaf+1;
		while(rightLeaf < tempheap.size()){
			//make sure at least one child node is greater than the parent node
			if(tempheap.get(leftLeaf).compareTo(tempheap.get(parentLeaf))>0 || tempheap.get(rightLeaf).compareTo(tempheap.get(parentLeaf))>0){
				if(tempheap.get(leftLeaf).compareTo(tempheap.get(rightLeaf))>0){//left leaf is greater than the right leaf
					//swap the left leaf and parent nodes
					T temp=(T) tempheap.get(leftLeaf);
					tempheap.set(leftLeaf, tempheap.get(parentLeaf));
					tempheap.set(parentLeaf, temp);
					parentLeaf=leftLeaf;
				}
				else{//right node is greater
					//swap the right leaf and parent nodes
					T temp=(T) tempheap.get(rightLeaf);
					tempheap.set(rightLeaf, tempheap.get(parentLeaf));
					tempheap.set(parentLeaf, temp);
					parentLeaf=rightLeaf;
				}
				leftLeaf=parentLeaf*2;
				rightLeaf=leftLeaf+1;
			}
			else
				break;
		}
		
		if(leftLeaf < tempheap.size()){//there is a left node but no right node, check if child is greater than parent
			if(tempheap.get(leftLeaf).compareTo(tempheap.get(parentLeaf))>0){//left leaf is greater than the parent
				//swap the left leaf and parent nodes
				T temp=(T) tempheap.get(leftLeaf);
				tempheap.set(leftLeaf, tempheap.get(parentLeaf));
				tempheap.set(parentLeaf, temp);
				parentLeaf=leftLeaf;
			}
		}
	}
	
	
	/****************************************************************************************************************
	 * This orders the elements in the priority from max to min
	 */
	protected Object[] orderElements(){
		Object[] elementArray = new Object[heap.size()-1];
		
		//copy the heap
		ArrayList<T> copyheap = new ArrayList<T>(heap);
		int heapSize=copyheap.size();
		for(int count=0;count < (heapSize-1); count++){
			elementArray[count]=copyheap.get(1);
			removeAndReorder(copyheap);
		}
		return elementArray;
		
	}
	
	
}
