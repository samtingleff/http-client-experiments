package com.rubicon.httpclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rubicon.httpclient.impl.RPAsyncHttpClientImpl;

import junit.framework.TestCase;

public class RPAsyncHttpClientTestCase extends TestCase {

	private RPAsyncHttpClient client;

	public void setUp() {
		RPAsyncHttpClientImpl impl = new RPAsyncHttpClientImpl();
		impl.setThreadPool(Executors.newFixedThreadPool(20));
		impl.init();
		this.client = impl;
	}

	public void tearDown() {
		((RPAsyncHttpClientImpl) this.client).destroy();
	}

	public void testMultiRequest() throws IOException, InterruptedException, ExecutionException, TimeoutException {
		// total allowed time in millis
		int sla = 1000;

		int requestCount = 10;

		// submit each request
		List<Future<HttpClientResponse<Integer>>> futures = new ArrayList<Future<HttpClientResponse<Integer>>>(requestCount);
		for (int i = 0; i < requestCount; ++i) {
			HttpClientRequest<Integer> request = createRequest(i, HttpClientMethod.POST, "http://localhost:8124", sla-10);
			Future<HttpClientResponse<Integer>> f = this.client.request(request, null);
			futures.add(f);
		}

		// wait for responses
		List<HttpClientResponse<Integer>> responses = new ArrayList<HttpClientResponse<Integer>>(requestCount);
		long start = System.currentTimeMillis();
		for (Future<HttpClientResponse<Integer>> f : futures) {
			long remaining = sla - (System.currentTimeMillis() - start);
			HttpClientResponse<Integer> response = f.get(remaining, TimeUnit.MILLISECONDS);
			responses.add(response);
		}

		// validate responses
		assertEquals(requestCount, responses.size());
		for (HttpClientResponse<Integer> response : responses) {
			Integer requestId = response.getContext();
			int responseCode = response.getCode();
			String responseBody = response.getResponseBodyAsString();
			assertTrue(requestId.intValue() >= 0);
			assertTrue(requestId.intValue() < 10);
			assertEquals(responseCode, 200);
			assertEquals(responseBody, "{\"status\":10,\"hello\":\"world\"}");
		}
	}

	private HttpClientRequest<Integer> createRequest(int requestId, HttpClientMethod method, String url, int sla) {
		HttpClientRequest<Integer> request = new HttpClientRequest<Integer>(
				new Integer(requestId),
				method,
				url,
				null,
				null,
				null,
				"{ \"post\": \"body\" \"goes\": \"here\" }".getBytes(),
				sla - 10);
		return request;
	}
}
