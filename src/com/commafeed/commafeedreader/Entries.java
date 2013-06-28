package com.commafeed.commafeedreader;

import java.util.ArrayList;

public class Entries {
	public String name;
	public String message;
	public int errorCount;
	public long timestamp;
	public boolean hasMore;
	public ArrayList<Entry> entries = new ArrayList<Entry>();
}
