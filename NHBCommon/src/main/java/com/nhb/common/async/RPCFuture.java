package com.nhb.common.async;
import java.util.concurrent.Future;

public interface RPCFuture<V> extends Future<V> {

	void setCallback(Callback<V> callable);

	Callback<V> getCallback();
}
