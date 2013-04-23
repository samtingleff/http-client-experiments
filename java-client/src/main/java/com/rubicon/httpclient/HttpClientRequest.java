package com.rubicon.httpclient;

import java.net.InetAddress;
import java.util.List;

public class HttpClientRequest<T> {

	private T context;

	private HttpClientMethod method;

	private String url;

	private InetAddress address;

	private List<NameValuePair> headers;

	private List<NameValuePair> parameters;

	private byte[] body;

	private int timeout;

	private boolean debug;

	public HttpClientRequest(T context,
			HttpClientMethod method,
			String url,
			InetAddress address,
			List<NameValuePair> headers,
			List<NameValuePair> parameters,
            byte[] body,
            int timeout) {
		this(context, method, url, address, headers, parameters, body, timeout, false);
	}

	public HttpClientRequest(T context,
			HttpClientMethod method,
			String url,
			InetAddress address,
			List<NameValuePair> headers,
			List<NameValuePair> parameters,
            byte[] body,
            int timeout,
            boolean debug) {
		this.context = context;
		this.method = method;
		this.url = url;
		this.address = address;
		this.headers = headers;
		this.parameters = parameters;
		this.body = body;
		this.timeout = timeout;
		this.debug = debug;
	}

	public T getContext() {
		return context;
	}

	public HttpClientMethod getMethod() {
		return method;
	}

	public String getUrl() {
		return url;
	}

	public InetAddress getAddress() {
		return address;
	}

	public List<NameValuePair> getHeaders() {
		return headers;
	}

	public List<NameValuePair> getParameters() {
		return parameters;
	}

    public byte[] getBody() {
		return body;
	}

	public int getTimeout() {
		return timeout;
	}

	public boolean isDebug() {
		return debug;
	}
}
