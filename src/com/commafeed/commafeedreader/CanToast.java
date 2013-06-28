package com.commafeed.commafeedreader;


// Any Activity that can have network errors should probably implement CanToast
// This is used so that all network-error-related toasting is deferred to the RestProxy
// so that I don't have to wrap every single REST call in a try/catch block
public interface CanToast {
	void toast(String s);
}