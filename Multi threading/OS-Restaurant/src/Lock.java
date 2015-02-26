
/**
 * @author Bhilhanan A Jeyaram
 */
import java.util.concurrent.locks.ReentrantLock;


public class Lock {
	public ReentrantLock lock;
	
	public Lock(boolean status){
		lock=new ReentrantLock(status);
	}

	public void unlock() {
		lock.unlock();
		
	}

	public boolean tryLock() {
		return lock.tryLock();
	}

	public void lock() {
		lock.lock();
		
	}
	public boolean isLockAvailable(){
		return lock.tryLock();
	}
}
