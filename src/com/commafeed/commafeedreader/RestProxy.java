package com.commafeed.commafeedreader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import android.app.Activity;

// This class exists for the sole purpose of handling edge cases (including
// HTTP exceptions) that are triggered by the REST API class, CommaFeedClient_
public class RestProxy implements InvocationHandler {
	CommaFeedClient_ client;
	static Object instance;
	static CanToast toaster;  // We want to be able to toast from here; specifically need an activity that has a toast
							  // method that runs in the UI thread. Otherwise I would just use the given context
							  // to pass to Toast.makeText()
	
	static Activity activity; // I am not clever enough to figure out how to have the same object
							  // as an activity and toaster without casting every time
	
	// Create a new proxy class wrapping the CommaFeedClient_ class
	// (the underscore is the suffix added to the name of the interface CommaFeedClient)
	public static Object getInstance(Activity a) {
		if (instance == null)
			instance = Proxy.newProxyInstance(CommaFeedClient_.class.getClassLoader(), // Class loader
											  new Class[] {CommaFeedClient.class},	 // Interfaces to implement
											  new RestProxy(new CommaFeedClient_()));  // InvocationHandler, pass it a new class
		activity = a;
		toaster = (CanToast) a;
		return instance;
	}
	
	public RestProxy(CommaFeedClient_ c) {
		if (c != null)
			client = c;
	}

	@Override
	public Object invoke(Object target, Method method, Object[] args)
			throws Throwable {
		String name = method.getName();
		// If wee need connectivity for this request, give up
		if (!name.equals("getRestTemplate") && !name.equals("setRestTemplate") && !Tools.isOnline(activity)) {
			toaster.toast("Error: No Internet Connection");
			throw new ExpectedException();
		}
		Object ret = null;
		Tools.info("Calling method "+method.getName());
		Tools.info("client == "+client.getClass().toString());
		try {
			// I am not a smart man
			switch(args.length){
			case 0:
				ret = method.invoke(client);
				break;
			case 1:
				ret = method.invoke(client, args[0]);
				break;
			case 2:
				ret = method.invoke(client, args[0], args[1]);
				break;
			case 3:
				ret = method.invoke(client, args[0], args[1], args[2]);
				break;
			case 4:
				ret = method.invoke(client, args[0], args[1], args[2], args[3]);
				break;
			case 5:
				ret = method.invoke(client, args[0], args[1], args[2], args[3], args[4]);
				break;
			case 6:
				ret = method.invoke(client, args[0], args[1], args[2], args[3], args[4], args[5]);
				break;
			}
		}
		catch(InvocationTargetException e) {
			Throwable cause = e.getCause(); // Can be null...don't really know what to do then
			if (cause instanceof HttpClientErrorException) {
				// Must cast cause otherwise we cannot use the getStatusCode and getStatusText methods
				HttpClientErrorException httpEx = (HttpClientErrorException) cause;
				toaster.toast("HTTP Error "+httpEx.getStatusCode()+" - "+httpEx.getStatusText());
			}
			else if (cause instanceof HttpServerErrorException) {
				HttpServerErrorException httpEx = (HttpServerErrorException) cause;
				toaster.toast("HTTP Error "+httpEx.getStatusCode()+" - "+httpEx.getStatusText());
				httpEx.printStackTrace();
			}
			else if (cause instanceof ResourceAccessException)
				toaster.toast("Network Error: No Connection");
			else
				throw cause; // Throw the unexpected error and crash the program. I refuse to catch 'em all
			throw new ExpectedException(); // Still throw so that the caller knows that an error occurred. The reason we bother with
					 					   // the proxy in the first place is so that it can handle the errors while the caller need only
					 					   // acknowledge that one occurred and simply stop what it was doing
		}
		return ret; // No failures, but ret may be null
	}
}
