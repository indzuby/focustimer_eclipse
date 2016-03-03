package com.realnumworks.focustimer.view.settings;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout.LayoutParams;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.DrawableHelper;
import com.realnumworks.focustimer.utils.Logs;

public class SettingsDetailActivity extends ActionBarActivity {
	// private Tracker mTracker;

	ActionBar actionBar;
	ListView lv;
	Settings s;
	ArrayList<String> datas;
	DataBaseHelper dbm;
	Intent intent;
	String mode = ""; // Weekday/Alarm01/Alarm02
	String selected = ""; // 현재 선택된 값

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_settings_detail);

		intent = getIntent();
		mode = intent.getStringExtra("mode");
		selected = intent.getStringExtra("default");

		setActionBar();
		setDatas();

		dbm = new DataBaseHelper(this);
		s = dbm.getSettings();

		final SettingsDetailAdapter adapter = new SettingsDetailAdapter(this,
				R.layout.listitem_settings_detail, datas, selected);

		lv = (ListView) findViewById(R.id.lv_settingsdetail_main);
		lv.setAdapter(adapter);

		//Item Click Listener 설정
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@SuppressLint("ResourceAsColor")
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String text = parent.getAdapter().getItem(position).toString();
				//클릭한 Item의 색상 회색처리
				//왜 setBackgroundColor로 처리했을 때는 제대로 색상 표시가 안 되지??????? 알쏭달쏭
				view.setBackgroundResource(R.color.ft_selectedgray);
				if (mode.equals("weekday")) {
					s.setStartWith(strToInt(text));
					StateSingleton.getInstance().setDstate(strToInt(text));
				} else if (mode.equals("alarm01")) {
					for (int i = 1; i < 10; i++)
						s.setAlarm01(strToInt(text));
				} else if (mode.equals("alarm02")) {
					s.setAlarm02(strToInt(text));
				} else if (mode.equals("vibrate")) {
					selected = datas.get(position);
					if (selected.equals("켬")) {
						s.setVibrateOn(true);
					} else if (selected.equals("끔")) {
						s.setVibrateOn(false);
					}
					Logs.d(Logs.SETTINGSTEST, s.toString());
					
				}
				dbm.modifySettings(s);
				selected = datas.get(position);
				Logs.d(Logs.SETTINGSTEST, "selected (after changed) = "+selected);
				adapter.changeSelectedItem(selected);
				adapter.notifyDataSetChanged();
				
				finish();
			}
		});
	}

	public void setActionBar() {
		setTheme(R.style.NoActionBarShadowTheme); // 액션바 그림자 제거
		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(DrawableHelper.getDrawable(this,
				R.drawable.actionbar_gradient));

		View v = getLayoutInflater().inflate(
				R.layout.actionbar_settings_detail, null);
		actionBar.setCustomView(v, new ActionBar.LayoutParams(new LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT)));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		TypefaceTextView tv_actionbar_title = (TypefaceTextView) v
				.findViewById(R.id.tv_actionbarsdetail_title);
		Logs.d("mmmm", "mode : " + mode);
		if (mode.equals("weekday")) {
			tv_actionbar_title.setText("시작 요일");
		} else if (mode.equals("alarm01")) {
			tv_actionbar_title.setText("시간 알림 1");
		} else if (mode.equals("alarm02")) {
			tv_actionbar_title.setText("시간 알림 2");
		} else if (mode.equals("vibrate")) {
			tv_actionbar_title.setText("집중 시작 진동");
		}
	}

	public void setDatas() {
		datas = new ArrayList<String>();
		if (mode.equals("weekday")) {
			datas.add("일요일");
			datas.add("월요일");
		} else if (mode.equals("alarm01")) {
			for (int i = 1; i < 10; i++) {
				datas.add((i * 5) + "분");
			}
		} else if (mode.equals("alarm02")) {
			for (int i = 1; i < 10; i++) {
				datas.add((i * 10) + "분");
			}
		} else if (mode.equals("vibrate")) {
			datas.add("켬");
			datas.add("끔");
		}
	}

	/**
	 * '분'을 int형으로, "요일" 을 2와 1로 바꿔준다.
	 * 
	 * @param str
	 * @return
	 */
	public int strToInt(String str) {
		if (str.contains("분")) {
			str = str.substring(0, str.length() - 1);
			return Integer.parseInt(str);
		} else if (str.contains("요일")) {
			if (str.equals("일요일"))
				return StateSingleton.STARTWITH_SUNDAY;
			else if (str.equals("월요일"))
				return StateSingleton.STARTWITH_MONDAY;
		}
		return -1;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// String name = getClass().getSimpleName();
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
}

class SettingsDetailAdapter extends BaseAdapter {
	Context context;
	ArrayList<String> items;
	LayoutInflater inflater;
	String selected;
	int layout;

	public SettingsDetailAdapter(Context context, int itemLayout,
			ArrayList<String> items, String selected) {
		this.context = context;
		this.items = items;
		this.selected = selected;
		layout = itemLayout;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void changeSelectedItem(String selected)
	{
		this.selected = selected;		 
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int index) {
		return items.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(layout, parent, false);
		}
		TypefaceTextView tv_item = (TypefaceTextView) convertView
				.findViewById(R.id.tv_settingsdetail_title);
		ImageView iv_checkmark = (ImageView) convertView
				.findViewById(R.id.iv_settingsdetail_checkmark);

		tv_item.setText(items.get(position).toString());
		Logs.d("mmmm", "selected = " + selected + ", "
				+ items.get(position).toString());
		if (selected.equals(items.get(position).toString())) // 선택된 값일 경우 체크박스를
																// 넣어준다
		{
			iv_checkmark.setImageResource(R.drawable.ic_check_mark_3x);
		} else {
			iv_checkmark.setImageDrawable(null);
		}

		return convertView;
	}
}
