package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {

	private T result;
	private boolean isDone;

	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		this.result = null;
		isDone = false;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public T get() {
		synchronized (this) { // use the lock this
			while (!isDone()) { // we should wait as long as the future is not resolved.
				try {
					System.out.println("get");
					wait(); // waiting for notifyAll() - when waking up, check if the future is resolved.
					System.out.println("got a");
				} catch (InterruptedException e) {
					System.out.println("Interrupted!");
				}
			}
			return result;
		}
	}
	
	/**
     * Resolves the result of this Future object.
	 * @pre isDone() == false
	 * @post isDone() == true
	 * @post this.result == result
     */
	public void resolve (T result) {
		synchronized (this) { // using the same lock as the get function.
			this.result = result;
			setDone(true);
			System.out.println("resolve");
			notifyAll();
		}
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		return isDone;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
	 *  @pre timeout >= 0
     */
	public T get(long timeout, TimeUnit unit) {
//		synchronized (this) {
			if (!isDone()) {
				// must wait for (timeout) units.
				// check if resolved, return result or null accordingly.
				try {
					unit.sleep(timeout);
				} catch (InterruptedException e) {
					System.out.println("Interrupted!");
				}
			}
			return result;
//		}
	}

	private void setDone(boolean done) {
		this.isDone = done;
	}

}
