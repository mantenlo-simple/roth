package com.roth.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class RothThread<T> {
	private ExecutorService executor;
	private Future<T> future;
	
	/**
	 * Check to see if the thread is still running.
	 * @return
	 */
	public boolean isRunning() { return !(future.isCancelled() || future.isDone()); }
	
	/**
	 * Attempt to cancel the thread execution.  Whether this works depends on how the 
	 * thread is coded.
	 * @return
	 */
	public boolean cancel() { return future.cancel(true); }
	
	/**
	 * Get the result of the thread execution.<br/>
	 * IMPORTANT: Check to make sure that the thread is not still running before running this, 
	 * as this is a blocking call, and will wait for a result.
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public T getResult() throws InterruptedException, ExecutionException { return future.get(); }
	
	/**
	 * Start the execution of the thread.
	 */
	public void run() {
		executor = Executors.newSingleThreadExecutor();
		Callable<T> callable = getCallable();
		if (callable != null)
			future = executor.submit(callable);
	}
	
	/**
	 * Get the callable used to execute.  This is where the functional code of the thread exists.
	 * @return
	 */
	protected abstract Callable<T> getCallable();
}
