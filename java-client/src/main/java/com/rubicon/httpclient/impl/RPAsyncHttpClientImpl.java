package com.rubicon.httpclient.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpProviderConfig;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.PerRequestConfig;
import com.ning.http.client.Response;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
import com.rubicon.httpclient.HttpClientRequest;
import com.rubicon.httpclient.HttpClientResponse;
import com.rubicon.httpclient.NameValuePair;
import com.rubicon.httpclient.RPAsyncCompletionHandler;
import com.rubicon.httpclient.RPAsyncHttpClient;

/**
 * https://github.com/sonatype/async-http-client
 * 
 * http://jfarcand.wordpress.com/2010/12/21/going-asynchronous-using-
 * asynchttpclient-the-basic/
 * 
 * http://jfarcand.wordpress.com/2011/01/04/going-asynchronous-using-
 * asynchttpclient-the-complex/
 * 
 * @author stingleff
 * 
 */
public class RPAsyncHttpClientImpl implements RPAsyncHttpClient {
	private static Log log = LogFactory.getLog(RPAsyncHttpClientImpl.class);

	private ExecutorService threadPool;

	private boolean compressionEnabled = false;

	private int connectionTimeout = 1000;

	private int requestTimeout = 1000;

	private int maxConnectionsPerHost = 10000;

	private int maxTotalConnections = 1000000000;

	private AsyncHttpClientConfig config;

	private AsyncHttpClient client;

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	public void setCompressionEnabled(boolean compressionEnabled) {
		this.compressionEnabled = compressionEnabled;
	}

	public void setConnectionTimeout(int timeout) {
		this.connectionTimeout = timeout;
	}

	public void setRequestTimeout(int timeout) {
		this.requestTimeout = timeout;
	}

	public void setMaximumConnectionsPerHost(int maxConnectionsPerHost) {
		this.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	public void setMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	public void init() {
		AsyncHttpProviderConfig asyncHttpProviderConfig = new NettyAsyncHttpProviderConfig();
		asyncHttpProviderConfig.addProperty(
				NettyAsyncHttpProviderConfig.EXECUTE_ASYNC_CONNECT, true);
		this.config = new AsyncHttpClientConfig.Builder()
				.setCompressionEnabled(compressionEnabled)
				.setConnectionTimeoutInMs(connectionTimeout)
				.setRequestTimeoutInMs(requestTimeout).setMaxRequestRetry(0)
				.setAsyncHttpClientProviderConfig(asyncHttpProviderConfig)
				.setMaximumConnectionsPerHost(maxConnectionsPerHost)
				.setMaximumConnectionsTotal(maxTotalConnections)
				.setFollowRedirects(false).setExecutorService(threadPool)
				.setUserAgent("be2/1.0").setIdleConnectionInPoolTimeoutInMs(
						1000 * 60).build();
		client = new AsyncHttpClient(this.config);
	}

	public void destroy() {
		this.client.close();
	}

	@Override
	public <T> Future<HttpClientResponse<T>> request(
			HttpClientRequest<T> request, RPAsyncCompletionHandler<T> handler)
			throws IOException {
		long start = System.currentTimeMillis();

		Handler<T> h = new Handler<T>(request.getContext(),
				System.currentTimeMillis(),
				handler,
				request.isDebug());
		BoundRequestBuilder builder = getBuilder(client, request);

		List<NameValuePair> headers = request.getHeaders();
		if ((headers != null) && (headers.size() > 0)) {
			for (NameValuePair nvp : headers) {
				builder.addHeader(nvp.getKey(), nvp.getValue());
			}
		}

		if (request.getAddress() != null)
			builder.setInetAddress(request.getAddress());
		ListenableFuture<Response> lf = builder.execute(h);
		return new FutureProxy<T>(lf, request.getContext(), System
				.currentTimeMillis());
	}

	private <T> BoundRequestBuilder getBuilder(AsyncHttpClient client,
			HttpClientRequest<T> request) {
		BoundRequestBuilder builder = null;
		switch (request.getMethod()) {
		case GET:
			builder = getGETBuilder(client, request);
			break;
		case POST:
			builder = getPOSTBuilder(client, request);
			break;
		default:
			throw new IllegalArgumentException();
		}

		PerRequestConfig requestConfig = new PerRequestConfig();
		requestConfig.setRequestTimeoutInMs(request.getTimeout());
		builder.setPerRequestConfig(requestConfig);
		return builder;
	}

	private <T> BoundRequestBuilder getGETBuilder(AsyncHttpClient client,
			HttpClientRequest<T> request) {
		BoundRequestBuilder builder = client.prepareGet(request.getUrl());
		List<NameValuePair> params = request.getParameters();
		if ((params != null) && (params.size() > 0)) {
			for (NameValuePair nvp : params) {
				builder.addQueryParameter(nvp.getKey(), nvp.getValue());
			}
		}
		return builder;
	}

	private <T> BoundRequestBuilder getPOSTBuilder(AsyncHttpClient client,
			HttpClientRequest<T> request) {
		BoundRequestBuilder builder = client.preparePost(request.getUrl());
		List<NameValuePair> params = request.getParameters();
		if ((params != null) && (params.size() > 0)) {
			for (NameValuePair nvp : params) {
				builder.addParameter(nvp.getKey(), nvp.getValue());
			}
		}

		if (request.getBody() != null)
			builder.setBody(request.getBody());
		return builder;
	}

	private static class FutureProxy<T> implements
			Future<HttpClientResponse<T>> {
		private ListenableFuture<Response> lf;

		private T context;

		private long start;

		public FutureProxy(ListenableFuture<Response> lf, T context, long start) {
			this.lf = lf;
			this.context = context;
			this.start = start;
		}

		public T getContext() {
			return context;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return lf.cancel(mayInterruptIfRunning);
		}

		@Override
		public HttpClientResponse<T> get() throws InterruptedException,
				ExecutionException {
			Response r = lf.get();
			return new HttpClientResponseImpl<T>(context, System
					.currentTimeMillis()
					- start, r);
		}

		@Override
		public HttpClientResponse<T> get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			Response r = lf.get(timeout, unit);
			return new HttpClientResponseImpl<T>(context, System
					.currentTimeMillis()
					- start, r);
		}

		@Override
		public boolean isCancelled() {
			return lf.isCancelled();
		}

		@Override
		public boolean isDone() {
			return lf.isDone();
		}

	}

	private static class Handler<T> implements AsyncHandler<Response> {
		private static final Log log = LogFactory.getLog(Handler.class);

		private final Response.ResponseBuilder builder = new Response.ResponseBuilder();

		private T context;

		private RPAsyncCompletionHandler<T> delegate;

		private boolean debug;

		private long start;

		public Handler(T context,
				long start,
				RPAsyncCompletionHandler<T> delegate,
				boolean debug) {
			this.context = context;
			this.delegate = delegate;
			this.start = start;
			this.debug = debug;
		}

		@Override
		public AsyncHandler.STATE onBodyPartReceived(
				HttpResponseBodyPart content) throws Exception {
			log.trace("onBodyPartReceived()");
			builder.accumulate(content);
			return STATE.CONTINUE;
		}

		@Override
		public AsyncHandler.STATE onStatusReceived(HttpResponseStatus status)
				throws Exception {
			log.trace("onStatusReceived()");
			builder.reset();
			builder.accumulate(status);
			return STATE.CONTINUE;
		}

		@Override
		public AsyncHandler.STATE onHeadersReceived(HttpResponseHeaders headers)
				throws Exception {
			log.trace("onHeadersReceived()");
			builder.accumulate(headers);
			return STATE.CONTINUE;
		}

		@Override
		public Response onCompleted() throws Exception {
			log.trace("onCompleted()");
			Response r = builder.build();
			HttpClientResponse<T> response = new HttpClientResponseImpl<T>(
					context, System.currentTimeMillis() - start, r);
			if (delegate != null) {
				delegate.onCompleted(response);
			}
			return r;
		}

		@Override
		public void onThrowable(Throwable t) {
			log.trace("onThrowable()");
			if (delegate != null)
				delegate.onThrowable(context, t);
		}

	}
}
