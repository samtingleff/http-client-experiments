package com.rubicon.httpclient;

public interface RPAsyncCompletionHandler<T> {
	public void onCompleted(HttpClientResponse<T> response);

	public void onThrowable(T context, Throwable t);
}
