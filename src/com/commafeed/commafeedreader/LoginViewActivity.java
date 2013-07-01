package com.commafeed.commafeedreader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;


@EActivity(R.layout.login_view)
public class LoginViewActivity extends Activity implements CanToast { // No Sherlock for this. Just log in
	
	CommaFeedClient client = RestProxy.getInstance(this);
	
	@ViewById
	EditText usernameField;
	
	@ViewById
	EditText passwordField;
	
	ProgressDialog progress;
	
	@AfterViews
	void setup() {
		RestTemplate template = client.getRestTemplate();
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		AuthInterceptor ai = AuthInterceptor.getInstance();
		interceptors.add(ai);
		template.setInterceptors(interceptors);
		
		Tools.setContext(this);
		client.setRootUrl(Tools.getApiUrl());
	}
	
	@Click(R.id.loginButton)
	@Background
	void login() {
		AuthInterceptor ai = AuthInterceptor.getInstance();
		String username = usernameField.getText().toString();
		String password = passwordField.getText().toString();
		ai.setUsername(username);
		ai.setPassword(password);
		progressStart("Please Wait");
		try {
			client.serverGet();
		} catch (ExpectedException e) { // An expected network error; do not catch unexpected ones
			progressStop();
			return;
		}
		Tools.setUsername(username);
		Tools.setPassword(password);
		Tools.saveLogin();
		Intent i = new Intent(LoginViewActivity.this, MainViewActivity_.class);
		startActivity(i);
		finish();
		progressStop();
	}
	
	@Override
	@UiThread
	public void toast(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();		
	}
	
	@UiThread
	void progressStart(String s) {
		progress = new ProgressDialog(LoginViewActivity.this);
		progress.setMessage(s);
		progress.show();
	}
	
	@UiThread
	void progressStop() {
		if (progress != null)
			progress.dismiss();
	}
}