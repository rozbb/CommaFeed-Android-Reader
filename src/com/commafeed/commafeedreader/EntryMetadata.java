package com.commafeed.commafeedreader;

import org.codehaus.jackson.annotate.JsonIgnore;

import android.widget.TextView;

public class EntryMetadata {
	// Must JSON ignore when this is saved to a file
	@JsonIgnore
	public TextView name;
	
	public boolean unread;
	public String id;
	public String html;
}
