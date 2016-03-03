package com.realnumworks.focustimer.test;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.service.ThreadControlService;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.test.view.DebugWindowView;
import com.realnumworks.focustimer.view.chart.ChartActivity;
import com.realnumworks.focustimer.view.history.HistoryActivity;
import com.realnumworks.focustimer.view.main.MainActivity;
import com.realnumworks.focustimer.view.settings.SettingsActivity;

public class MainTestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_test);

		findView();
	}

	private void findView() {

	}

	public void onClickMainBtn(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		/**
		 * set sth on intent if necessary
		 */
		startActivity(intent);
	}

	public void onClickServiceBtn(View view) {
		int tag = getTag(view);
		TextView tv = (TextView)view;
		if (tag == 0) {
			tv.setTag(1);
			tv.setText("Service OFF");
			Intent service = new Intent(this, ThreadControlService.class);
			startService(service);
		} else {
			tv.setTag(0);
			tv.setText("Service ON");
			Intent service = new Intent(this, ThreadControlService.class);
			stopService(service);
		}
	}

	/**
	 * 
	 * @param view
	 * @return int value / default = 0;
	 */
	public int getTag(View view) {
		int out = 0;
		Object tag = view.getTag();
		if (tag instanceof String || tag instanceof Integer) {
			out = Integer.valueOf("" + tag);
		} else {
			out = -1;
		}
		return out;
	}

	public void onClickRecordBtn(View view) {
		Intent intent = new Intent(this, HistoryActivity.class);
		/**
		 * set sth on intent if necessary
		 */
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra("themeId", "0");
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_OTHER);

		startActivity(intent);
	}

	public void onClickChartBtn(View view) {
		Intent intent = new Intent(this, ChartActivity.class);
		/**
		 * set sth on intent if necessary
		 */
		startActivity(intent);
	}

	public void onClickSettingBtn(View view) {
		Intent intent = new Intent(this, SettingsActivity.class);
		/**
		 * set sth on intent if necessary
		 */
		startActivity(intent);
	}

	public enum TestClass {
		DebugWindow(DebugWindowView.class, "", "");

		Class<?> iclass;
		String name, desc;

		TestClass(Class<?> iclass, String name, String desc) {
			this.iclass = iclass;
			this.name = name;
			this.desc = desc;
		}

		public static List<TestClass> getList() {
			ArrayList<TestClass> list = new ArrayList<TestClass>();
			for (TestClass testClass : getList()) {
				list.add(testClass);
			}
			return list;
		}
	}
}
