package com.realnumworks.focustimer.test.activity.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.realnumworks.focustimer.R;

public class MessageListAdapter extends BaseAdapter {

	public static final int MAX_ITEM = 100;

	private LayoutInflater inflater;
	private Context context;
	private ArrayList<MessageListItem> items;

	public MessageListAdapter(Context context, ArrayList<MessageListItem> list) {
		this.context = context;
		this.items = list;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.listitem_message, null);
		}

		TextView tv_title = (TextView)convertView.findViewById(R.id.tv_message_title);
		TextView tv_help = (TextView)convertView.findViewById(R.id.tv_message_body);

		if (items != null && items.size() > position) {
			try {
				final MessageListItem item = items.get(position);
				tv_title.setText(String.valueOf(item.mTitle));
				tv_help.setText(String.valueOf(item.mHelpMesage));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return convertView;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void add(MessageListItem item) {
		if (items.size() >= MAX_ITEM)
			items.remove(0);
		items.add(item);
	}

	public void clear() {
		items.clear();
	}
}
