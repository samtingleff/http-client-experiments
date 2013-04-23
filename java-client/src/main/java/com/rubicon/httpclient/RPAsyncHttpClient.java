package com.rubicon.httpclient;

import java.io.IOException;
import java.util.concurrent.Future;

public interface RPAsyncHttpClient {
	public <T> Future<HttpClientResponse<T>> request(HttpClientRequest<T> request,
			RPAsyncCompletionHandler<T> handler) throws IOException;
}
