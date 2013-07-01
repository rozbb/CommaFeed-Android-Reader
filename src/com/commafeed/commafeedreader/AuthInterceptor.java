package com.commafeed.commafeedreader;

import java.io.IOException;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

// Authentication Interceptor Singleton
class AuthInterceptor implements ClientHttpRequestInterceptor {
	
	private static AuthInterceptor instance = new AuthInterceptor();
	
	public static AuthInterceptor getInstance() {
		return instance;
	}
	
	String username = new String("");
	String password = new String("");
	
	public void setUsername(String u) {
		if (u != null)
			username = u;
	}
	
	public void setPassword(String p) {
		if (p != null)
			password = p;
	}

	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		HttpHeaders headers = request.getHeaders();
		HttpAuthentication auth = new HttpBasicAuthentication(username, password);
		headers.setAuthorization(auth);
		//Tools.debug("Headers.auth == "+headers.getAuthorization());
		return execution.execute(request, body);
	}
}