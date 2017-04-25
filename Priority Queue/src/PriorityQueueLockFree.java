import java.util.concurrent.atomic.AtomicReference;




public class PriorityQueueLockFree<T, E> {
	
	
	private PriorityQueueNode head = new PriorityQueueNode(null);
	private PriorityQueueNode tail= new PriorityQueueNode(null);
	private AtomicReference<PriorityQueueNode> atomicRefTemp=new AtomicReference<PriorityQueueNode>(null);
	private AtomicReference<PriorityQueueNode> atomicRefTail= new AtomicReference<PriorityQueueNode>(null);
	private AtomicReference<PriorityQueueNode> atomicRefHead=new AtomicReference<PriorityQueueNode>(null);
	
	
	
	
	//add element
	public boolean add(E input){//PriorityQueueNode previousNode
		PriorityQueueNode newNode=new PriorityQueueNode((Comparable)input);
		PriorityQueueNode tempNode=head;//used to iterate through the linked list
		
		if(head.getData()!=null){
			while(tempNode!=null){
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
						return true;
					}
				}
			}
			
			//if it gets here then that means its the smallest data in the priority queue
			newNode.setPrevious(tempNode);
			if(atomicRefTail.compareAndSet(tempNode, newNode)){
				tempNode.setNext(newNode);
				tail=tempNode;
				return true;
			}
			
			
		}
		else{//first element
			if(atomicRefHead.compareAndSet(null,newNode)){
				atomicRefTail.compareAndSet(null, newNode);
				head=newNode;
				tail=newNode;
				return true;
			}
		}
		
		Thread.yield();
		return false;
	}
		
	public void printNodes(){
		PriorityQueueNode printNode = head;
		if(head.getData()!=null){//take into account if theres no element
			do{
				System.out.println(printNode.getData());
				printNode=printNode.getNext();
			}while(printNode!=null);
			
			System.out.println();//add new line to make it easier to read
		}
		
	}
	
		
}

