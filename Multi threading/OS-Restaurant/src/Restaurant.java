
/**
 * This is the main class which acts as an environment for the Restaurant simulation program.
 * The cook, diners are threads that are created once the number of cooks and diners are
 * determined from the input file whose location is passed as command line argument
 * 
 * @author Bhilhanan A Jeyaram
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Restaurant {

	
	public int timer;
	private int cokeMachineUsageTime = 1;
	private int frierMachineUsageTime = 3;
	private int grillMachineUsageTime = 5;
	private int clock_resolution = 10;
	
	public Lock cokeMachine;
	public Lock friesMachine;
	public Lock burgerMachine;
	
	private Clock lock_on_timer = new Clock();
	private BlockingQueue<Cook> cooks_queue;
	private BlockingQueue<Integer> tables;
	private Queue<Diner> diners;
	private CountDownLatch num_diners;
	private int num_cooks;	
	public boolean restaurantClosed = false;
	
	
	
	private class Cook implements Runnable {

		private CookProperties cook = new CookProperties(null, new Object(), new Object());

		public Cook(int id) {
			this.cook.id = id;
		}

		public void run() {
			while (!restaurantClosed) {
				if (cook.serving_diner != null) {
					System.out.println( " Clock count = " + timer+" ---- Cook " + cook.id	+ " cooking for diner " + cook.serving_diner.getId());
					int burgerCount = cook.serving_diner.getBurgerCount();
					int friesCount = cook.serving_diner.getFriesCount();
					boolean coke = cook.serving_diner.getCoke();
					
					while (friesCount>0 || coke) {//Burger is a must for all diners. So checking for fries and coke
						if (burgerCount> 0 && burgerMachine.isLockAvailable()) {
							useBurgerMachine();
							burgerMachine.unlock();
							burgerCount--;
						} else if (friesCount > 0 && friesMachine.isLockAvailable()) {
							useFrier();
							friesMachine.unlock();
							friesCount--;
						} else if (coke && cokeMachine.isLockAvailable()) {
							useCokeMachine();
							cokeMachine.unlock();
							coke = false;
						} else {
							if (burgerCount > 0
									&& (burgerCount*grillMachineUsageTime< friesCount*frierMachineUsageTime && burgerCount*grillMachineUsageTime<cokeMachineUsageTime)) {
								burgerMachine.lock();
								useBurgerMachine();
								burgerMachine.unlock();
								burgerCount--;
							} else if (friesCount > 0
									&& (friesCount * frierMachineUsageTime < burgerCount
											* grillMachineUsageTime && friesCount
											* frierMachineUsageTime < cokeMachineUsageTime)) {
								friesMachine.lock();
								useFrier();
								friesMachine.unlock();
								friesCount--;
							} else if (coke) {
								cokeMachine.lock();
								useCokeMachine();
								cokeMachine.unlock();
								coke = false;
							}
						}
					}
					while (burgerCount > 0) {
						burgerMachine.lock();
						useBurgerMachine();
						burgerMachine.unlock();
						burgerCount--;
					}
					cook.serving_diner = null;
					synchronized (cook.orderedFood) {
						cook.orderedFood.notify();
					}
				}
				// Wait for diner
				try {
					synchronized (cook.lock_onDiner) {
						cook.lock_onDiner.wait(500);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

		public void prepareOrderFor(Diner diner) {
			System.out.println("Cook " + cook.id + " assigned to "+ diner.getId());
			this.cook.serving_diner = diner;
			synchronized (cook.lock_onDiner) {
				cook.lock_onDiner.notify();
			}
		}

		public void waitForFood() {
			try {
				synchronized (cook.orderedFood) {
					cook.orderedFood.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void useCokeMachine() {
			System.out.println("Clock count = "+timer+"----- Cook "+cook.id+" started using coke machine for diner "+cook.serving_diner.getId());
			int start = timer;
			while (start + cokeMachineUsageTime > timer) {
				try {
					synchronized (lock_on_timer) {
						lock_on_timer.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Clock count = "+timer+"----- Cook"+cook.id+" finished using coke machine for diner "+cook.serving_diner.getId());
		}

		private void useFrier() {
			System.out.println("Clock count = "+timer+"---- Cook"+cook.id+" started cooking fries for diner "+cook.serving_diner.getId());
			int start = timer;
			while (start + frierMachineUsageTime > timer) {
				try {
					synchronized (lock_on_timer) {
						lock_on_timer.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Clock count = "+timer+"---- Cook "+cook.id+" finished cooking fries for diner "+cook.serving_diner.getId());
		}

		private void useBurgerMachine() {
			System.out.println("Clock count = "+timer+"---- Cook "+cook.id+" started using burger machine for diner "+cook.serving_diner.getId());
			int start = timer;
			while (start + grillMachineUsageTime > timer) {
				try {
					synchronized (lock_on_timer) {
						lock_on_timer.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Clock count = "+timer+"---- Cook "+cook.id+" finished using burger machine for diner "+cook.serving_diner.getId());
		}

	}

	private class DinerThread implements Runnable {

		private DinerProperties diner = new DinerProperties();

		public DinerThread(Diner diner) {
			this.diner.diner = diner;
		}

		public void run() {
			System.out.println("Clock count = "+timer+"---- Diner " + diner.diner.getId() + " entered");
			acquireTable();
			acquireFood();
			System.out.println("Clock count = "+timer+"---- Diner " + diner.diner.getId() + " started eating");
			while (diner.foodStartTime + 30 > timer) {
				try {
					synchronized (lock_on_timer) {
						lock_on_timer.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				tables.put(diner.table);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//diner leaving
			num_diners.countDown();
			System.out.println("Clock count = "+ timer+" ---- Diner " + diner.diner.getId() + " leaving ");
		}

		private void acquireTable() {
			try {
				diner.table = tables.take(); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Clock count = "+timer+" ---- Diner " + diner.diner.getId() + " acquired table "
					+ diner.table);
		}

		private void acquireFood() {
			try {
				Cook cook = cooks_queue.take();
				cook.prepareOrderFor(diner.diner);
				cook.waitForFood();
				cooks_queue.put(cook);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public Restaurant(Queue<Diner> queue_diners, int num_tables, int num_cooks) {
		timer = 0;
		this.diners = queue_diners;
		this.num_cooks = num_cooks;

		// create locks
		friesMachine = new Lock(true);
		burgerMachine = new Lock(true);
		cokeMachine = new Lock(true);
				
		//populating tables
		tables = new ArrayBlockingQueue<Integer>(num_tables, true);
		for (int i = 0; i < num_tables; i++) {
			tables.offer(i);
		}
		num_diners = new CountDownLatch(queue_diners.size());
	}

	public void beginEnv() {
		cooks_queue = new ArrayBlockingQueue<Cook>(num_cooks, true);
		for (int i=0;i<num_cooks;i++) {
			Cook c = new Cook(i+1);
			cooks_queue.offer(c);
			new Thread(c).start();
		}
		Thread clockThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!restaurantClosed) {
					try {
						Thread.sleep(clock_resolution);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					timer++;
					synchronized (lock_on_timer) {
						lock_on_timer.notifyAll();
					}
					// Start new thread
					startDinerThread();
				}
			}
		});
		clockThread.start();//start the timer which will increment the clock
		startDinerThread();//let the diners come
		try {
			num_diners.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Clock count = "+timer+ "---- Restaurant is closing");
		restaurantClosed = true;
	}

	private void startDinerThread() {
		Diner next = diners.peek();
		if (next != null) {
			if (next.getArrivalTime() == timer) {
				diners.remove();
				new Thread(new DinerThread(next)).start();
			}
		}
	}

	
	public static void main(String[] args) {
		System.out.println("Opening Restaurant ....");
		System.out.println("Reading environment properties ....");
		if (args.length != 1) {
			System.out.println("Input data required");
			return;
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader(args[0]));
			int num_diners = Integer.parseInt(in.readLine().trim());
			int num_tables = Integer.parseInt(in.readLine().trim());
			int num_cooks = Integer.parseInt(in.readLine().trim());

			Queue<Diner> diner_queue = new ArrayDeque<Diner>(num_diners);
			for (int i=0;i<num_diners;i++) {
				Scanner r = new Scanner(in.readLine());
				diner_queue.add(new Diner(r.nextInt(), r.nextInt(), r.nextInt(), Boolean.valueOf(r.nextInt() != 0)));
			}

			System.out.println("Creating environment ....");
			Restaurant env = new Restaurant(diner_queue, num_tables, num_cooks);
			env.beginEnv();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
