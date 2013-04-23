package com.rubicon.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface HttpClientResponse<T> {

	public T getContext();

	public long getDurationMillis();

	public String getHeader(String name);

	public Map<String, List<String>> getHeaders();

	public String getResponseBodyAsString() throws IOException;

	public String getResponseBody(String charset) throws IOException;

	public int getCode();

	public InputStream getStream() throws IOException;
}
