// M/M/2 Queue Simulation

import java.util.Random;
import java.io.PrintWriter;


public class Assignment3 {
	
	
	public static void main(String[] args) throws Exception {
		int customers = 100000;
		double arrivalRate1 = 5;
		double arrivalRate2 = 15;
		double serviceTime = 12;
		int debugLength = 50;
		
		QueueSimulation sim1 = new QueueSimulation(customers, arrivalRate1, arrivalRate2, 
													serviceTime, debugLength);
		sim1.run();
		
	} // end main
	
	public static class QueueSimulation {
		
		private int customerTotal;
		private int currentCustomer;
		private double lambda1;
		private double lambda2;
		private double serviceTime;
		private int debugLength;
		private Random rand;
		private PrintWriter outFile;
		private double time;
		private Queue delayed;
		private Queue future;
		private boolean server1Free;
		private boolean server2Free;

		
		public QueueSimulation(int c, double ar1, double ar2, double st, int dl) throws Exception {
			customerTotal = c;
			currentCustomer = 1;
			lambda1 = ar1;
			lambda2 = ar2;
			serviceTime = st;
			debugLength = dl;
			rand = new Random();
			outFile = new PrintWriter("debug.txt");
			delayed = new Queue();
			future = new Queue();
			server1Free = true;
			server2Free = true;
			time = 0.0;	
		} // end constructor()
		
		
		
		// runs the simulation
		public void run() {	
			initialize();
			
				
			while(currentCustomer <= customerTotal) {
				stepToNextEvent();
				if(debugLength > 0) debugOutput();
			}
	
			outFile.close();
			System.out.println("Simulation Successful.");
		} // end run()
		
		
		
		// generates the first two arrivals from each stream
		//  and puts them on the future events list
		public void initialize() {
			arrivalStream1();
			arrivalStream2();
			debugOutput();
		} // end initialize()
		
		

		// updates the simulation clock to the next imminent event
		//  possible events are arrival and departure
		public void stepToNextEvent() {
			Node n = future.pop();
			time = n.time;
			
			
			// handle arrival event
			if(n.type.equals("A")) {
				
				// replace event with an event from the stream it came from
				if(n.origin.equals("A1")) arrivalStream1();
				else arrivalStream2();
				
		
				// check server status
				if(serversBusy()) delayed.add(n);
				else addToServer(n);
			}
			
			// handle departure event
			else {
				
				// add customer from delayed list to a server
				if(delayedHasCustomer()) {
					Node m = delayed.pop();
					addToServer(m);
				}
				else {
					// free the server the event was on
					if(n.origin.equals("S1")) server1Free = true;
					else server2Free = true;
				}
			}
		} // end stepToNextEvent()
		
		
		
		// adds given node to a free server
		public void addToServer(Node n) {
			n.type = "D";
			n.time = time + (Math.log(rand.nextDouble())/(-serviceTime));
			if(server1Free) {
				n.origin = "S1";
				server1Free = false;
			}
			else {
				n.origin = "S2";
				server2Free = false;
			}
			future.add(n);
		} // end addToServer()
		
		
			
		// generates a customer from stream1, adds it to future events list
		public void arrivalStream1() {
			double t = time +(Math.log(rand.nextDouble())/(-lambda1));
			Node n = new Node("A", "A1", currentCustomer, t);
			future.add(n);
			currentCustomer++;
		} // end arrivalStream1()
		
		
		
		// generates a customer from stream2, adds it to future events list
		public void arrivalStream2() {
			double t = time +(Math.log(rand.nextDouble())/(-lambda2));
			Node n = new Node("A", "A2", currentCustomer, t);
			future.add(n);
			currentCustomer++;
		} // end arrivalStream2()
		
		

		// returns true if the delayed events list has customers
		public boolean delayedHasCustomer() {
			if(delayed.head == null) return false;
			else return true;
		} // end delayedHasCustomer()
		
		
		
		// returns true if both servers are busy, false otherwise
		public boolean serversBusy() {
			if(server1Free == false && server2Free == false) return true;
			else return false;
		} // end serversBusy()
		
		
		
		//
		public void debugOutput() {
			//outFile.println("debug count:" + debugLength);
			outFile.println("current time: " + time);
			//outFile.println("future length:" + future.length);
			//outFile.println("delayed length:" + delayed.length);
			outFile.println("future event queue:");
			future.output();
			outFile.println("delayed event queue:");
			delayed.output();
			outFile.print("server1: ");
			if(server1Free) outFile.print("Idle ");
			else outFile.print("Busy ");
			outFile.print("server2: ");
			if(server2Free) outFile.println("Idle ");
			else outFile.println("Busy ");
			outFile.print("--------------------------------------------------\n");
			outFile.println();
			debugLength--;
		} // debugOutput()
	
		
		
		// 
		public class Queue {
			private Node head;
			private int length;
			
			public Queue() {
				head = null;
				length = 0;
			} // end constructor
			
			
			
			// adds given node to Queue in time order
			public void add(Node n) {
				length++;
				if(head == null) head = n;
				else if(n.time < head.time) {
					n.next = head;
					head = n;
				} 
				else {
					Node nav = head;
					while(nav.next != null) {
						if(n.time < nav.next.time) {
							n.next = nav.next;
							nav.next = n;
							return;
						}
						nav = nav.next;
					}
					nav.next = n;
				}
			} // end add()
			
			
			
			// returns the head of the queue
			public Node pop() {
				Node ret = head;
				head = ret.next;
				ret.next = null;
				length--;
				return ret;
			} // end pop()
			
			
			
			// prints queue to debug file
			public void output() {
				Node nav = head;
				while(nav != null) {
					outFile.print("\t[" + nav.type);
					outFile.print(", customer:" + String.format("%02d", nav.customer));
					outFile.print(",\t" + nav.time);
					//outFile.print("," + nav.origin); // for my debug
					outFile.println("]");
					nav = nav.next;
				}
			} // end output()
			
		} // end class: Queue
		
		
		
		public class Node {
			private String type;
			private String origin;
			private int customer;
			private double time;
			private Node next;
			
			
			public Node(String typeArg, String s, int c, double t) {
				type = typeArg;
				origin = s;
				customer = c;
				time = t;	
				next = null;
			} // end constructor
			
		} // end class: Node
		
	} // end class: QueueSimulation
	
} // end wrapper class