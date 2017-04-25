import java.util.concurrent.atomic.AtomicReference;


public class PriorityQueueNode<T extends Comparable<T>, E> {
	private PriorityQueueNode next;
	private PriorityQueueNode previous;
	private T data;
	

	PriorityQueueNode(){}
	
	PriorityQueueNode(T input){
		this.next=null;
		this.previous=null;
		this.data=input;
	}
	
	public PriorityQueueNode getNext(){
		return next;
	}
	
	public PriorityQueueNode getPrevious(){
		return previous;
	}
	public T getData(){
		return data;
	}
	public void setNext(PriorityQueueNode value){
		this.next=value;
	}
	public void setPrevious(PriorityQueueNode value){
		this.previous=value;
	}
	public void setData(T value){
		this.data=value;
	}
	
	
		
}
