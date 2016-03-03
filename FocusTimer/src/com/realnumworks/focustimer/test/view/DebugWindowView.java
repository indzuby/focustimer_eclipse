package com.realnumworks.focustimer.test.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.utils.Timestamp;

public class DebugWindowView extends LinearLayout {

	private ListView lv_debug_message;
	private MessageListAdapter adapter;
	private TextView tv_debug_info;
	private StringBuilder sb = new StringBuilder();

	public DebugWindowView(Context context) {
		super(context);
		findView(context);
		setContantStatusValues();
	}

	public void init() {

	}

	public void destroy() {

	}

	private void findView(Context context) {

		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.view_debug_window, this, true);

		tv_debug_info = (TextView)findViewById(R.id.tv_debug_info);
		lv_debug_message = (ListView)findViewById(R.id.lv_debug_message);
		lv_debug_message.setDivider(null);
		adapter = new MessageListAdapter(context, R.layout.listitem_message, true);
		lv_debug_message.setAdapter(adapter);
	}

	private void setContantStatusValues() {
		if (tv_debug_info != null) {
			int size = sb.length();
			if (size > 0) {
				sb.delete(0, size);
			}
			sb.append(String.format("NT-Heap.Total %,d Kb", Debug.getNativeHeapSize() / 1024));
			sb.append(String.format("NT-Heap.Alloc %,d Kb", Debug.getNativeHeapAllocatedSize() / 1024));
			sb.append(String.format("NT-Heap.Free %,d Kb", Debug.getNativeHeapFreeSize() / 1024));
			sb.append(String.format("VM-Heap.Total %,d Kb", Runtime.getRuntime().totalMemory() / 1024));
			sb.append(String.format("VM-Heap.Alloc %,d Kb", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024));
			sb.append(String.format("VM-Heap.Free %,d Kb", Runtime.getRuntime().freeMemory() / 1024));

			tv_debug_info.setText(String.valueOf(sb.toString()));
		}
	}

	@SuppressWarnings("deprecation")
	public void update(String status, int state) {
		long curTime = System.currentTimeMillis();
		if (null != adapter) {
			if (adapter.isRun() == true) {
				adapter.add(Timestamp.ToTimeString(curTime, false) + "\t" + status);
				adapter.notifyDataSetChanged();
			} else
				adapter.addBuffer(Timestamp.ToTimeString(curTime, false) + "\t" + status);
		}
		if (null != lv_debug_message) {
			if (adapter.isRun() == true) {
				int cp = lv_debug_message.getCount();

				lv_debug_message.setSelectionFromTop(cp - 1, MessageListAdapter.MAX_ITEM);

				if (Integer.parseInt(Build.VERSION.SDK) < 8)
					lv_debug_message.setSelection(cp - 1);
				else
					lv_debug_message.smoothScrollToPosition(cp - 1);
			}
		}
	}

	public class MessageListAdapter extends BaseAdapter {

		public static final int MAX_ITEM = 100;

		private LayoutInflater inflater;
		private Context context;
		private ArrayList<String> list;
		private Queue<String> mMessageBuffer = null;
		private int resource;
		private boolean isRun = true;

		public MessageListAdapter(Context context, int resource, boolean run) {
			this.context = context;
			this.resource = resource;
			this.list = new ArrayList<String>();
			mMessageBuffer = new LinkedList<String>();
			isRun = run;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(resource, null);
			}

			TextView tvHistory = (TextView)convertView.findViewById(R.id.tv_message_body);
			TextView tvTimeStamp = (TextView)convertView.findViewById(R.id.tv_message_title);

			if (list != null && list.size() > position) {
				try {
					String[] tok = list.get(position).split("\t");
					tvTimeStamp.setText(tok[0]);
					tvHistory.setText(tok[1]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return convertView;
		}

		public void setIsRun(boolean run) {
			isRun = run;
		}

		public boolean isRun() {
			return isRun;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public void add(String str) {
			if (list.size() >= MAX_ITEM)
				list.remove(0);

			list.add(str);
		}

		public void addBuffer(String str) {
			if (mMessageBuffer.size() >= MAX_ITEM)
				mMessageBuffer.poll();

			mMessageBuffer.offer(str);
		}

		public synchronized void copyHistory() {
			if (mMessageBuffer != null && mMessageBuffer.isEmpty() == true && list != null)
				return;

			int listSize = list.size();
			int bufferSize = mMessageBuffer.size();

			if (bufferSize + listSize >= MAX_ITEM) {
				if (bufferSize == 100 && list != null && list.isEmpty() == false)
					list.clear();
				else {
					while (bufferSize + listSize >= MAX_ITEM) {
						if (list != null && list.isEmpty() == false)
							list.remove(0);

						listSize = list.size();
						bufferSize = mMessageBuffer.size();
					}
				}
			}

			for (int i = 0; i < bufferSize; i++)
				list.add(mMessageBuffer.poll());
		}

		public void clear() {
			list.clear();
			mMessageBuffer.clear();
		}
	}

}
