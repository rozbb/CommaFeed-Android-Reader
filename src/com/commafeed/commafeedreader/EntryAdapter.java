package com.commafeed.commafeedreader;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EntryAdapter extends ArrayAdapter<Entry> {

	private Context context;
	private Entries entries;
	
	public EntryAdapter(Context c, Entries e) {
	    super(c, R.layout.entry_row, e.entries.toArray(new Entry[e.entries.size()]));
	    context = c;
	    entries = e;
	}
	
	// Bold feeds/folders with unread feeds; hide unread count (which is 0)
	// for feeds/folders without unread feeds. Also load proper icon
	// for feeds and folders
	private void style(EntryMetadata holder, boolean read) {
		if (!read)
			holder.name.setTypeface(null, Typeface.BOLD);
		else
			holder.name.setTypeface(null, Typeface.NORMAL);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Entry entry = entries.entries.get(position);
		View rowView = convertView;
		// Make a brand-new View
		if(rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.entry_row, parent, false);
			EntryMetadata holder = new EntryMetadata();

			holder.name = (TextView)  rowView.findViewById(R.id.feedName);
			holder.html = entry.content;
			
			holder.name.setText(entry.title);
			
			style(holder, entry.read);
			rowView.setTag(holder);
		}
		else {
			// Re-purpose used View 
			EntryMetadata holder = (EntryMetadata) rowView.getTag();
			holder.name.setText(entry.title);
			style(holder, entry.read);
		}
		
		return rowView;
	}
}