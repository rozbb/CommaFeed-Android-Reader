package com.commafeed.commafeedreader;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Category implements CategoryOrSubscription {
	private long unreadCache = -1;
	
	public String id;
	public String parentId;
	public String name;
	public ArrayList<Category> children;
	public ArrayList<Subscription> feeds;
	public boolean expanded;
	public long position;
	public long unread = 0;
	
	public String getName() {
		return name;
	}
	
	@JsonIgnore
	public long getUnread() {
		if (unreadCache > -1) // Don't bother again until I've refreshed
			return unreadCache;
		long total = 0;
		for (Subscription s : feeds) {
			total += s.getUnread(); // Subscription::getUnread() just returns s.unread
		}
		for (Category c : children) { // Recursively sum all unread from children
			total += c.getUnread();
		}
		return total;
	}
	
	@JsonIgnore
	public String getIconUrl() { // Only Subscriptions have icons, silly
		return null; // when applying an icon, if this returns null it gets a folder icon
	}
	
	@JsonIgnore
	public boolean isFolder() {
		return true;
	}
	
	public String getId() {
		return id;
	}
}