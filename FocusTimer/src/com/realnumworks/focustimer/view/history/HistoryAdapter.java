package com.realnumworks.focustimer.view.history;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.realnumworks.focustimer.R;

//어댑터 클래스
public class HistoryAdapter extends BaseAdapter {

	Context context;
	LayoutInflater inflater;
	ArrayList<ItemString> itemStringList;
	ViewGroup parent;

	public HistoryAdapter(Context context, ArrayList<ItemString> itemStringList) {
		this.context = context;
		this.itemStringList = itemStringList;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		int type = BaseItem.ITYPE_DATA;
		if (itemStringList.get(position).getHeader() != null) {
			type = BaseItem.ITYPE_HEADER;
		}
		return type;
	}

	@Override
	public int getCount() {
		return itemStringList.size();
	}

	@Override
	public ItemString getItem(int position) {
		return itemStringList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		this.parent = parent;
		int type = getItemViewType(position);
		switch (type)
		{
			case BaseItem.ITYPE_HEADER:
				convertView = getHeaderView(position, convertView, parent);
				break;
			case BaseItem.ITYPE_DATA:
				convertView = getItemView(position, convertView, parent);
		}
		return convertView;
	}

	private View getHeaderView(int position, View convertView, ViewGroup parent) {

		View headerView = convertView;
		HeaderViewHolder viewHolder;
		ItemString itemString;

		if (headerView == null) { //신규 생성된 View
			headerView = inflater.inflate(R.layout.listheader_history, parent, false);
			viewHolder = new HeaderViewHolder(headerView);
			headerView.setTag(viewHolder); //Tag로 Viewholder를 걸어 재사용 할 수 있게 한다.
		}
		else { //재사용 View
			viewHolder = (HeaderViewHolder)headerView.getTag();
		}

		itemString = getItem(position);
		viewHolder.setTextInHeader(itemString);
		return headerView;
	}

	private View getItemView(int position, View convertView, ViewGroup parent) {

		View itemView = convertView;
		ItemViewHolder viewHolder;
		ItemString itemString;

		if (itemView == null) {
			itemView = inflater.inflate(R.layout.listitem_history, parent, false);
			viewHolder = new ItemViewHolder(itemView);
			itemView.setTag(viewHolder); //Tag로 Viewholder를 걸어 재사용 할 수 있게 한다.
		}
		else { //재사용 View
			viewHolder = (ItemViewHolder)itemView.getTag();
		}

		itemString = getItem(position);
		viewHolder.setTextInItem(itemString);
		return itemView;
	}
}
