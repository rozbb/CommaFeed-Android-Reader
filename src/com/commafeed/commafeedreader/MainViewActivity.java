package com.commafeed.commafeedreader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.UiThread;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

// CanToast allows this to be compatible with the REST proxy that handles
// all of the API errors
@EActivity
@OptionsMenu(R.menu.main_view_menu)
public class MainViewActivity extends SherlockActivity implements CanToast {

	private ActionBar actionBar;
	private ListView listView;
	private ProgressDialog progress;
	private Category currentCategory = null;
	private String currentSubscriptionId = null;
	private EntryMetadata currentEntryMetadata = null;
	private boolean viewingEntry = false;
	private Stack<Category> parentCategoryStack = new Stack<Category>(); // no parents at root view
	private CatSubAdapter adapter;
	private HashMap<String, Entries> entriesMap = new HashMap<String, Entries>(); // Maps subscription ids to entry groups
	
	CommaFeedClient client = RestProxy.getInstance(this);
	
	@OptionsItem
	void settings() {
		Intent i = new Intent(this, PreferenceView.class);
		startActivity(i);
	}
	
	@OptionsItem
	void refresh() {
		entriesMap = new HashMap<String, Entries>(); // clear the entry cache
		populateAndShowCategory(); // clear the category cache then show
	}
	
	// Clear login info and launch us into LoginView
	@OptionsItem
	void logout() {
		Tools.clearLogin();
		assertLogin();
	}
	
	@UiThread
	void progressStart(String s) {
		progress = new ProgressDialog(MainViewActivity.this);
		progress.setMessage(s);
		progress.show();
	}
	
	@UiThread
	void progressStop() {
		if (progress != null)
			progress.dismiss();
	}
	
	@Override
	@UiThread
	public void toast(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

	// Must figure out what do do when one of the exceptions below is thrown
	public void saveState() {
		Tools.debug("Saving state...");
		ViewState state = new ViewState();
		state.parents = parentCategoryStack;
		state.category = currentCategory;
		state.subId = currentSubscriptionId;
		state.entriesMap = entriesMap;
		state.entry = currentEntryMetadata;
		try {
			FileOutputStream viewStateFile = openFileOutput("viewState", Context.MODE_PRIVATE);
			// Map everything back to JSON
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonMapper.writeValue(viewStateFile, state);			
			viewStateFile.close();
		}
		// Not sure how this would be thrown; MODE_PRIVATE creates
		// the file if it doesn't already exist
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Whatever
		catch (IOException e) { 
			e.printStackTrace();
		}
	}

	// Must figure out what do do when one of the exceptions below is thrown
	public boolean restoreState() {
		Tools.debug("Restoring state...");
		try {
			FileInputStream viewStateFile = openFileInput("viewState");
			ObjectMapper jsonMapper = new ObjectMapper();
			ViewState viewState = jsonMapper.readValue(viewStateFile, ViewState.class);
			boolean showCategory=false, showEntries=false, showEntry=false; // We'll see what to do once we parse the state
			
			// Drill down view in order of specificity
			if (viewState.entry != null)
				showEntry = true;
			else if (viewState.subId != null)
				showEntries = true;
			else if (viewState.category != null)
				showCategory = true;
			else
				throw (new RuntimeException("State was not category, entries, or entry!")); // Very very weird if it happened
			
			parentCategoryStack = viewState.parents;
			currentCategory = viewState.category;
			currentSubscriptionId = viewState.subId;
			entriesMap = viewState.entriesMap;
			currentEntryMetadata = viewState.entry;
			// Go in order just like before
			if (showEntry) {
				// Must set title for entry view; assume entries are in entriesMap (fair assumption...I think)
				showEntry(currentEntryMetadata);
				actionBar.setTitle(entriesMap.get(currentSubscriptionId).name);
			}
			else if (showEntries)
				getAndShowEntries(currentSubscriptionId);
			else if (showCategory)
				showCurrentCategory();
			Tools.debug("Finished restoration");
			return true;
		}
		
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Tools.setContext(this); // Allows me to load stuff from preferences
		if (!assertLogin()) // Make sure we're logged in; assertLogin switches to LoginView and finishes()
			return;			// if we're not logged in
		setup();
		if (!restoreState())
			populateAndShowCategory(); // if the state restoration didn't work, download
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (!assertLogin()) // Must check if we're still logged in onResume(); settings may have changed
			return;
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		saveState();
	}
	
	boolean assertLogin() {
		try {
			Tools.loadLogin();
		} catch(ExpectedException e) {
			Intent i = new Intent(MainViewActivity.this, LoginViewActivity_.class); // Make the user log in before anything else
			startActivity(i);
			finish();
			return false;
		}
		return true;
	}

	void setup() {
		RestTemplate template = client.getRestTemplate();
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		AuthInterceptor ai = AuthInterceptor.getInstance();
		ai.setUsername(Tools.getUsername());
		ai.setPassword(Tools.getPassword());
		interceptors.add(ai);
		template.setInterceptors(interceptors);
		client.setRootUrl(Tools.getApiUrl());
		
		Tools.debug("Set rootUrl to "+Tools.getApiUrl());

		actionBar = getSupportActionBar();
		listView = (ListView) getLayoutInflater().inflate(R.layout.feed_view, null);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int pos, long id) {
				// If the clicked item is not an "entry"
				if (view.getTag() instanceof CatSubMetadata) {
					CatSubMetadata holder = (CatSubMetadata) view.getTag();
					if (holder.folder) { // View a sub-Category
						Category target = null;
						for (Category c : currentCategory.children) { // Find the target folder
							if (c.name.equals(holder.name.getText()))
								target = c;
						}
						if (target == null) // Something weird happened
							return;
						// Drill down one level
						MainViewActivity.this.parentCategoryStack.push(MainViewActivity.this.currentCategory);
						MainViewActivity.this.currentCategory = target;
						MainViewActivity.this.showCurrentCategory();
					}
					else {
						Tools.debug("holder.id == "+holder.id);
						// this function deals with caching
						try {
							getAndShowEntries(holder.id);
							// still wanna be able to go back to categories
							MainViewActivity.this.parentCategoryStack.push(currentCategory);
						}
						catch (ExpectedException e) {} // Do not push if getAndShowEntries() fails
					}
				}
				else { // it is an entry
					EntryMetadata holder = (EntryMetadata) view.getTag();
					showEntry(holder);
				}
			}
		});
	}
	
	@Background
	void populateAndShowCategory() {
		progressStart("Fetching Categories...");
		try {
			currentCategory = client.categoryGet(); // The first category is "all"
			parentCategoryStack = new Stack<Category>(); // So old parents are cleared
		}
		catch(ExpectedException e) { // Some connection or HTTP error
			progressStop();
			return;
		}
		showCurrentCategory();
	}
	
	@UiThread
	void showCurrentCategory() {
		ArrayList<CategoryOrSubscription> list = new ArrayList<CategoryOrSubscription>();
		// Populate the list with folders then feeds
		for (Category c : currentCategory.children) {
			list.add(c);
		}
		for (Subscription s : currentCategory.feeds) {
			list.add(s);
		}
		adapter = new CatSubAdapter(this, list.toArray(new CategoryOrSubscription[list.size()]));
		listView.setAdapter(adapter);
		progressStop();
		actionBar.setTitle(currentCategory.name);
		setContentView_(listView);
	}

	// Download entries if not present
	@Background
	void getAndShowEntries(String subId) {
		Entries entries = entriesMap.get(subId);
		if (entries == null) { // entries were not cached
			try {
				progressStart("Downloading Entries...");
				// see description in CommaFeedClient.java
				entries = client.feedEntries(String.valueOf(subId), "all", 0, 0, 10, "desc");
				progressStop();
			}
			catch (ExpectedException e) {
				progressStop();
				throw e; // Propagate the error down
			}
		}
		// We change the view state only after the network actions have succeeded
		currentSubscriptionId = subId; // so we can pop back if we look at an entry
		entriesMap.put(subId, entries);
		showEntries(entries);
	}
	
	@UiThread
	void showEntries(Entries entries) {
		EntryAdapter adapter = new EntryAdapter(MainViewActivity.this, entries);
		listView.setAdapter(adapter);
		actionBar.setTitle(entries.name);
		setContentView_(listView);
	}
	
	@UiThread
	void showEntry(EntryMetadata holder) {
		WebView webView = new WebView(MainViewActivity.this);
		webView.loadData(holder.html, "text/html", null);
		WebSettings settings = webView.getSettings();
		settings.setBuiltInZoomControls(true); // allow for pinch-zoom
		settings.setSupportZoom(true);
		settings.setUseWideViewPort(true); // scale (poorly) to screen
		settings.setLightTouchEnabled(true);
		settings.setLoadWithOverviewMode(true);
		
		// Very important variables for maintaining state
		currentEntryMetadata = holder;
		viewingEntry = true;
		
		setContentView_(webView);
	}

	@Override
	public void onBackPressed() {
		Tools.debug("Parent Stack:\n" + Tools.stackToString(parentCategoryStack)+"\n");
		// If we're at the topmost and back is pressed, die
		if (parentCategoryStack.empty()) {
			finish();
			return;
		}
		else if (viewingEntry) { // if we're looking at an entry, go back to the entry list
			Tools.debug("Back pressed, going back to subscription");
			getAndShowEntries(currentSubscriptionId);
			currentEntryMetadata = null; // Pop state back
			viewingEntry = false;
		}
		else { // Otherwise pop out to last category
			Tools.debug("Back pressed, going back to parent");
			currentCategory = parentCategoryStack.pop();
			currentSubscriptionId = null;
			showCurrentCategory();
		}
	}
	
	// Wrap so this always runs in UiThread
	@UiThread
	void setContentView_(View v) {
		setContentView(v);
	}
}