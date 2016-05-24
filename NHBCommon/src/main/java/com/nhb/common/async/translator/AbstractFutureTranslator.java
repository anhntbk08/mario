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

	private final Future<FromType> future;
	private volatile ToType response;

	public AbstractFutureTranslator(RPCFuture<FromType> future) {
		assert future != null;
		this.future = future;
	}

	protected Future<FromType> getFuture() {
		return this.future;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return this.future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.future.isDone();
	}

	protected final ToType parseAndSaveResponse(FromType response) {
		if (this.response == null) {
			synchronized (this) {
				if (this.response == null) {
					this.response = this.translate(cloneBeforeTranslate(response));
				}
			}
		}
		return this.response;
	}

	@ThreadSafe
	protected abstract ToType translate(FromType response);

	/**
	 * By default, return <b>response</b> parameter itself. <br/>
	 * Override this if the translate method make any change on the response
	 * value
	 * 
	 * @param response
	 * @return cloned value
	 */
	protected FromType cloneBeforeTranslate(FromType response) {
		return response;
	}

	@Override
	public ToType get() throws InterruptedException, ExecutionException {
		return this.parseAndSaveResponse(this.future.get());
	}

	@Override
	public ToType get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return this.parseAndSaveResponse(this.future.get(timeout, unit));
	}

}
