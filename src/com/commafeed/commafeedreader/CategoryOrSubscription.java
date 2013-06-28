package com.commafeed.commafeedreader;

// OR type so I can make an ArrayList<CategoryOrSubscription> to pass to a ListView Adapter
public interface CategoryOrSubscription {
	public String getName();
	public long getUnread();
	public String getIconUrl();
	public boolean isFolder();
	public String getId();
}