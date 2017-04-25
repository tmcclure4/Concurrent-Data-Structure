
public class test extends Thread{
	PriorityQueueLockFree testing = new PriorityQueueLockFree();
	boolean BOOLEAN=true;
	public void run(){
		if(BOOLEAN){
			BOOLEAN=false;
			System.out.println("in thread");
			//PriorityQueueLockFree testing = new PriorityQueueLockFree();
			testing.add(10);
			testing.offer(60);		
			testing.offer(40);
			testing.offer(20);
			testing.remove(20);
			testing.remove(3);
			testing.printNodes();
			System.out.println("out of thread");
		}
		else{
			System.out.println("in other code");
			testing.add(65);
			testing.offer(25);
			testing.offer(105);
			testing.offer(595);
			testing.remove(985);
			testing.remove(0);
			testing.printNodes();
			System.out.println("out of thread");
		}
		
	}
	
	public static void main(String[] args){
		
		test t1 = new test();
		t1.start();
		System.out.println("temp");
		test t2=new test();
		
		t2.start();
		

		/*testing.offer(5);
		testing.printNodes();
		
		testing.offer(1000);
		testing.printNodes();
		*/
		/*
		 * PriorityQueueLockFree testing = new PriorityQueueLockFree();
		testing.add(20);
		testing.printNodes();
		
		testing.add(60);
		testing.printNodes();
		
		testing.add(20);
		testing.printNodes();
		
		testing.add(5);
		testing.printNodes();
		
		testing.add(1000);
		testing.printNodes();
		 */
		//System.out.println(testing.contains(5));
		//System.out.println(testing.contains(15));
		//System.out.println(testing.contains(1000));
		//PriorityQueueNode testing = new PriorityQueueNode(10);
		/*Thread test1=new Thread();
		Thread test2=new Thread();
		test1.start();
		test2.start();
		
		*/
		//testing.add(5);
		//testing.add(2);
	//	testing.add(10);
		//testing.add(70);
		//testing.add(54);
		//testing.add(32);
		//testing.add(1);
		//testing.add(70);
		//Object[] tempthing=testing.toArray();
		//testing.print();
		//testing.remove(5);
		//testing.print();
			
		
	}

	
}
