package com.nhb.messaging.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.PuXmlHelper;

public class HttpClientHelper extends BaseLoggable implements Closeable {

	private boolean usingMultipath = true;
	private boolean followRedirect = true;

	private Set<CloseableHttpAsyncClient> asyncClients = new CopyOnWriteArraySet<>();

	private ThreadLocal<HttpAsyncClient> localHttpAsyncClients = new ThreadLocal<HttpAsyncClient>() {

		@Override
		protected HttpAsyncClient initialValue() {
			CloseableHttpAsyncClient result = null;
			if (isFollowRedirect()) {
				result = HttpAsyncClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
			} else {
				result = HttpAsyncClients.createDefault();
			}
			if (result != null) {
				result.start();
				asyncClients.add(result);
			}
			return result;
		};
	};

	private ThreadLocal<HttpClient> localHttpClients = new ThreadLocal<HttpClient>() {

		@Override
		protected HttpClient initialValue() {
			CloseableHttpClient result = null;
			if (isFollowRedirect()) {
				result = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
			} else {
				result = HttpClients.createDefault();
			}
			return result;
		};
	};

	private HttpClient getSyncClient() {
		return this.localHttpClients.get();
	}

	private HttpAsyncClient getAsyncClient() {
		return this.localHttpAsyncClients.get();
	}

	public HttpAsyncFuture executeAsync(RequestBuilder builder, PuObjectRO params) {
		if (params != null) {
			if (builder.getMethod().equalsIgnoreCase("get") || this.isUsingMultipath()) {
				for (Entry<String, PuValue> entry : params) {
					builder.addParameter(entry.getKey(), entry.getValue().getString());
				}
			} else {
				String json = params.toJSON();
				try {
					builder.setEntity(new StringEntity(json));
					builder.addHeader("Content-Type", "application/json");
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Unable to send data", e);
				}
			}
		}
		// getLogger().debug("\n------- REQUEST -------\nURI: {}\nPARAMS:
		// {}\n-----------------------",
		// builder.getUri().toString(), params);
		HttpAsyncFutureImpl future = new HttpAsyncFutureImpl();
		Future<HttpResponse> cancelFuture = getAsyncClient().execute(builder.build(), future);
		future.setCancelFuture(cancelFuture);
		return future;
	}

	public HttpResponse execute(RequestBuilder builder, PuObjectRO params) {
		try {
			if (params != null) {
				if (builder.getMethod().equalsIgnoreCase("get") || this.isUsingMultipath()) {
					for (Entry<String, PuValue> entry : params) {
						builder.addParameter(entry.getKey(), entry.getValue().getString());
					}
				} else {
					String json = params.toJSON();
					try {
						builder.setEntity(new StringEntity(json));
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException("Unable to send data", e);
					}
				}
			}
			getLogger().info("\n------- REQUEST -------\nURI: {}\nPARAMS: {}\n-----------------------",
					builder.getUri().toString(), params);
			return getSyncClient().execute(builder.build());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public HttpResponse executeGet(String uri, PuObjectRO params) {
		try {
			return execute(RequestBuilder.get().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	public HttpResponse executePost(String uri, PuObjectRO params) {
		try {
			return execute(RequestBuilder.post().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	public HttpAsyncFuture executeAsyncGet(String uri, PuObjectRO params) {
		try {
			return executeAsync(RequestBuilder.get().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	public HttpAsyncFuture executeAsyncPost(String uri, PuObjectRO params) {
		try {
			return executeAsync(RequestBuilder.post().setUri(new URI(uri)), params);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while creating URI instance", e);
		}
	}

	public static PuElement handleResponse(HttpResponse response) {
		PuElement result = null;
		if (response != null) {
			try {
				String responseText = EntityUtils.toString(response.getEntity());
				if (responseText != null) {
					responseText = responseText.trim();
					try {
						if (responseText.startsWith("[")) {
							result = PuArrayList.fromJSON(responseText);
						} else if (responseText.startsWith("{")) {
							result = PuObject.fromJSON(responseText);
						} else if (responseText.startsWith("<")) {
							result = PuXmlHelper.parseXml(responseText);
						} else {
							result = new PuValue(responseText, PuDataType.STRING);
						}
					} catch (Exception ex) {
						result = new PuValue(responseText, PuDataType.STRING);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Error while consuming response entity", e);
			}
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		for (CloseableHttpAsyncClient client : this.asyncClients) {
			if (client.isRunning()) {
				client.close();
			}
		}
	}

	public boolean isUsingMultipath() {
		return usingMultipath;
	}

	public void setUsingMultipath(boolean usingMultipath) {
		this.usingMultipath = usingMultipath;
	}

	public boolean isFollowRedirect() {
		return followRedirect;
	}

	public void setFollowRedirect(boolean followRedirect) {
		this.followRedirect = followRedirect;
	}
}
