package com.rubicon.httpclient.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.ning.http.client.Response;
import com.rubicon.httpclient.HttpClientResponse;

public class HttpClientResponseImpl<T> implements HttpClientResponse<T> {
	private final static String DEFAULT_CHARSET = "ISO-8859-1";

	private T context;

	private long duration;

	private Response response;

	public HttpClientResponseImpl(T context, long duration, Response response) {
		this.context = context;
		this.duration = duration;
		this.response = response;
	}

	public T getContext() {
		return context;
	}

	public long getDurationMillis() {
		return duration;
	}

	public Map<String, List<String>> getHeaders() {
		return response.getHeaders();
	}
	
	public String getHeader(String name) {
		return response.getHeader(name);
	}

	public String getResponseBodyAsString() throws IOException {
		return getResponseBody(DEFAULT_CHARSET);
	}

	public String getResponseBody(String charset) throws IOException {
		return response.getResponseBody(charset);
	}

	public int getCode() {
		return response.getStatusCode();
	}

	public InputStream getStream() throws IOException {
		return response.getResponseBodyAsStream();
	}
}
