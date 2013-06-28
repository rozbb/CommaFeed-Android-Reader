package com.commafeed.commafeedreader;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Subscription implements CategoryOrSubscription {
	public Long id;
	public String name;
	public String message;
	public int errorCount;
	public Date lastRefresh;
	public Date nextRefresh;
	public String feedUrl;
	public String feedLink;
	public String iconUrl;
	public long unread;
	public String categoryId;
	public long position;
	
	public String getName() {
		return name;
	}
	
	public long getUnread() {
		return unread;
	}
	
	public String getIconUrl() {
		return iconUrl;
	}
	
	@JsonIgnore
	public boolean isFolder() {
		return false;
	}
	
	public String getId() {
		return String.valueOf(id);
	}
}