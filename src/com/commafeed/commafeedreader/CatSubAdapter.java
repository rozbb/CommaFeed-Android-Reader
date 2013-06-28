package com.commafeed.commafeedreader;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

// ListView Adapter for CategoryOrSubscription types
public class CatSubAdapter extends ArrayAdapter<CategoryOrSubscription> {

	private Context context;
	private CategoryOrSubscription[] items;
	
	public CatSubAdapter(Context c, CategoryOrSubscription[] arr) {
	    super(c, R.layout.cat_sub_row, arr);
	    context = c;
	    items = arr;
	}
	
	// Bold feeds/folders with unread items; hide unread count (which is 0)
	// for feeds/folders without unread items. Also load proper icon
	// for feeds and folders
	private void style(CatSubMetadata holder, long unread) {
		if (unread > 0) {
			holder.name.setTypeface(null, Typeface.BOLD);
			holder.unread.setVisibility(View.VISIBLE);
		}
		else {
			holder.name.setTypeface(null, Typeface.NORMAL);
			holder.unread.setVisibility(View.GONE); // When the unread count is 0, hide the unread number
		}
		if (holder.folder)
			holder.icon.setImageResource(R.drawable.folder);
		else
			holder.icon.setImageResource(R.drawable.feed_placeholder);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategoryOrSubscription item = items[position];
		View rowView = convertView;
		// Make a brand-new View
		if(rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.cat_sub_row, parent, false);
			CatSubMetadata holder = new CatSubMetadata();
			
			// Save all this stuff in the tag for ease of use later
			holder.icon 	= (ImageView) rowView.findViewById(R.id.catSubIcon);
			holder.name 	= (TextView)  rowView.findViewById(R.id.catSubName);
			holder.unread 	= (TextView)  rowView.findViewById(R.id.catSubUnreadCount);
			holder.folder	= item.isFolder();
			holder.id		= item.getId();
			
			holder.name.setText(item.getName());
			holder.unread.setText(String.valueOf(item.getUnread()));
			
			style(holder, item.getUnread());
			rowView.setTag(holder);
		}
		else {
			// Re-purpose used View 
			CatSubMetadata holder = (CatSubMetadata) rowView.getTag();
			holder.name.setText(item.getName());
			holder.unread.setText(String.valueOf(item.getUnread()));
			style(holder, item.getUnread());
		}
		
		return rowView;
	}
}
