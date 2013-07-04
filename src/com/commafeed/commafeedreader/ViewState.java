package com.commafeed.commafeedreader;

import java.util.HashMap;
import java.util.Stack;

public class ViewState {
	public Stack<Category> parents;
	public Category category;
	public String subId;
	public HashMap<String, Entries> entriesMap;
	public EntryMetadata entry;
}
