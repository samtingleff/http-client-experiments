package com.rubicon.httpclient;

public class NameValuePair {
	private String key;

	private String value;

	public NameValuePair() {
	}

	public NameValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
