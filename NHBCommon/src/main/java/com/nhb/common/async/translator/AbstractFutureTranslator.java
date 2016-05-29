package com.nhb.common.async.translator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.nhb.common.BaseLoggable;
import com.nhb.common.annotations.ThreadSafe;
import com.nhb.common.async.RPCFuture;

public abstract class AbstractFutureTranslator<FromType, ToType> extends BaseLoggable
		implements FutureTranslator<FromType, ToType> {

	private final Future<FromType> sourceFuture;
	private volatile ToType lastResult;

	private Throwable failedCause;

	public AbstractFutureTranslator(RPCFuture<FromType> future) {
		assert future != null;
		this.sourceFuture = future;
	}

	protected Future<FromType> getSourceFuture() {
		return this.sourceFuture;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.sourceFuture.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return this.sourceFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.sourceFuture.isDone();
	}

	/**
	 * Attempt to parse and save the result returned by source future, using
	 * translate(result) method. <br/>
	 * Any <b>exception</b> thrown by translate() will be caught automatically
	 * and prevent the translate method to be re-executed
	 * 
	 * 
	 * @param result
	 * @return
	 */
	protected final ToType parseAndSaveResult(FromType result) {
		if (this.lastResult == null && this.getFailedCause() == null) {
			synchronized (this) {
				if (this.lastResult == null && this.getFailedCause() == null) {
					try {
						this.lastResult = this.translate(cloneBeforeTranslate(result));
					} catch (Exception e) {
						this.setFailedCause(e);
					}
				}
			}
		}
		return this.lastResult;
	}

	/**
	 * Translate response in FromType to ToType <br/>
	 * Any exception thrown will be caught by parseAndSaveResult() to prevent
	 * this method to be re-executed
	 * 
	 * @see AbstractFutureTranslator.parseAndSaveResponse
	 * @param sourceResult
	 * @return
	 */
	@ThreadSafe
	protected abstract ToType translate(FromType sourceResult) throws Exception;

	/**
	 * By default, return <b>sourceResult</b> parameter by itself. <br/>
	 * Override this if the translate method make any change on the sourceResult
	 * value
	 * 
	 * @param sourceResult
	 * @return cloned value
	 */
	protected FromType cloneBeforeTranslate(FromType sourceResult) {
		return sourceResult;
	}

	@Override
	public ToType get() throws InterruptedException, ExecutionException {
		return this.parseAndSaveResult(this.sourceFuture.get());
	}

	@Override
	public ToType get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return this.parseAndSaveResult(this.sourceFuture.get(timeout, unit));
	}

	@Override
	public Throwable getFailedCause() {
		return failedCause;
	}

	protected void setFailedCause(Throwable failedCause) {
		this.failedCause = failedCause;
	}

}
