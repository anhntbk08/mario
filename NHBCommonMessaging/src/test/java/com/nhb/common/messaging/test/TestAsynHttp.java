package com.nhb.common.messaging.test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;

import com.nhb.common.async.Callback;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuObject;
import com.nhb.common.utils.Initializer;
import com.nhb.messaging.http.HttpClientHelper;
import com.nhb.messaging.http.producer.HttpAsyncMessageProducer;
import com.nhb.messaging.http.producer.HttpSyncMessageProducer;

public class TestAsynHttp {

	static {
		Initializer.bootstrap(TestAsynHttp.class);
	}

	private static String url = "http://generalmgraph.desgame.com/account/GetProfile/";
	private static PuObject params = PuObject.fromObject(
			new MapTuple<>("access_token", "1a12c21e1453219984p0c1250104793.55d1fc0fe7d8182265ce906c02543282"));

	public static void main(String[] args) throws InterruptedException, IOException {
		asyncTest();
		syncTest();
	}

	private static void syncTest() throws InterruptedException, IOException {

		HttpSyncMessageProducer syncMessageProducer = new HttpSyncMessageProducer();
		syncMessageProducer.setMethod("get");
		syncMessageProducer.setFollowRedirect(false);
		syncMessageProducer.setEndpoint(url);

		final long startTime = System.currentTimeMillis();
		String str = syncMessageProducer.publish(params).toString().trim();
		long time = System.currentTimeMillis() - startTime;
		System.out.println(str.substring(0, Math.min(1000, str.length())));
		System.out.println("time: " + time + "ms");
		System.out.println("****************************************************************");
		syncMessageProducer.close();
	}

	private static void asyncTest() throws InterruptedException, IOException {
		final CountDownLatch doneSignal = new CountDownLatch(1);

		HttpAsyncMessageProducer asyncMessageProducer = new HttpAsyncMessageProducer();
		asyncMessageProducer.setMethod("get");
		asyncMessageProducer.setFollowRedirect(false);
		asyncMessageProducer.setEndpoint(url);

		final long startTime = System.currentTimeMillis();
		asyncMessageProducer.publish(params).setCallback(new Callback<HttpResponse>() {

			@Override
			public void apply(HttpResponse result) {
				long time = System.currentTimeMillis() - startTime;
				String str = HttpClientHelper.handleResponse(result).toString().trim();
				System.out.println(str.substring(0, Math.min(1000, str.length())));
				System.out.println("time: " + time + "ms");
				System.out.println("****************************************************************");
				doneSignal.countDown();
			}
		});

		doneSignal.await();
		asyncMessageProducer.close();
	}
}
