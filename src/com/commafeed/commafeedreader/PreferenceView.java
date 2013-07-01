package com.commafeed.commafeedreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.webkit.URLUtil;
import android.widget.Toast;

public class PreferenceView extends PreferenceActivity {
	
	private SharedPreferences prefs = null;
	
	private SharedPreferences.OnSharedPreferenceChangeListener listener = 
		new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences p, String key) {
				Tools.debug("API URL Changed");
		        if(key.equals("apiUrl")) {
		        	String url = p.getString("apiUrl", null);
		    		if (url == null || !URLUtil.isValidUrl(url)) { // Bad URL, revert back to commafeed URL
		    			url = getString(R.string.defaultApiUrl); // Reset the URL to default
		    			SharedPreferences.Editor editor = p.edit();
		    			editor.putString("apiUrl", url); // overwrite to default
		    			editor.commit(); // save
		    			toast("Invalid API URL; falling back to default");
		    		}
		    		// Now update the client's URL
		    		CommaFeedClient client = RestProxy.getInstance();
		    		client.setRootUrl(url); // Done!
		        }
		    }
		};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_view);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Validate user's URL input
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}
		
	public void toast(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}
}