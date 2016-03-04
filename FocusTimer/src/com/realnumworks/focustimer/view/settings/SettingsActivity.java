package com.realnumworks.focustimer.view.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout.LayoutParams;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.service.StateCheckService;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.thread.StateCheckThread;
import com.realnumworks.focustimer.utils.DrawableHelper;
import com.realnumworks.focustimer.utils.Logs;

public class SettingsActivity extends ActionBarActivity {

	// private Tracker mTracker;

	ActionBar actionBar;
	DataBaseHelper dbm;
	Settings currentSettings;
	TypefaceTextView tv_weekday, tv_alarm01, tv_alarm02, tv_version, tv_vibrate;
	ImageView iv_weekday, iv_alarm01, iv_alarm02, iv_vibrate;
	RelativeLayout layout_weekday, layout_alarm01, layout_alarm02, layout_vibrate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_settings);

		setDB();
		setActionBar();
		setComponents();
		setListeners();
	}

	public void setDB() {
		dbm = new DataBaseHelper(this);
		currentSettings = dbm.getSettings();
	}

	public void setActionBar() {
		setTheme(R.style.NoActionBarShadowTheme); // 액션바 그림자 제거
		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(DrawableHelper.getDrawable(this, R.drawable.actionbar_gradient));

		View v = getLayoutInflater().inflate(R.layout.actionbar_settings, null);
		actionBar.setCustomView(v, new ActionBar.LayoutParams(new LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		Button btn_close = (Button) findViewById(R.id.btn_actionbarsettings_close);
		btn_close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				currentSettings = dbm.getSettings();
				finish();
			}
		});
	}

	public void setComponents() {
		tv_weekday = (TypefaceTextView) findViewById(R.id.tv_settings_weekday);
		tv_alarm01 = (TypefaceTextView) findViewById(R.id.tv_settings_alarm01);
		tv_alarm02 = (TypefaceTextView) findViewById(R.id.tv_settings_alarm02);
		tv_vibrate = (TypefaceTextView) findViewById(R.id.tv_settings_vibrate);

		iv_weekday = (ImageView) findViewById(R.id.iv_settings_weekday);
		iv_alarm01 = (ImageView) findViewById(R.id.iv_settings_alarm01);
		iv_alarm02 = (ImageView) findViewById(R.id.iv_settings_alarm02);
		iv_vibrate = (ImageView) findViewById(R.id.iv_settings_vibrate);

		layout_weekday = (RelativeLayout) findViewById(R.id.layout_settings_weekday);
		layout_alarm01 = (RelativeLayout) findViewById(R.id.layout_settings_alarm01);
		layout_alarm02 = (RelativeLayout) findViewById(R.id.layout_settings_alarm02);
		layout_vibrate = (RelativeLayout) findViewById(R.id.layout_settings_vibrate);

		tv_version = (TypefaceTextView) findViewById(R.id.tv_settings_version);

		tv_version.setText(getVersionName());
	}

	public String getVersionName() {
		PackageManager packageManager = getPackageManager();
		PackageInfo info;
		try {
			info = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			String versionName = info.versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setDatas() {
		// 일시작/월시작
		switch (currentSettings.getStartWith()) {
		case StateSingleton.STARTWITH_SUNDAY:
			tv_weekday.setText("일요일");
			break;
		case StateSingleton.STARTWITH_MONDAY:
			tv_weekday.setText("월요일");
			break;
		}

		tv_alarm01.setText(currentSettings.getAlarm01() + "분");
		tv_alarm02.setText(currentSettings.getAlarm02() + "분");
		tv_vibrate.setText((currentSettings.isVibrateOn() == true) ? "켬" : "끔");
	}

	public void setListeners() {
		View.OnClickListener daySettingListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SettingsDetailActivity.class);
				intent.putExtra("mode", "weekday");
				intent.putExtra("default", tv_weekday.getText().toString());
				startActivity(intent);
			}
		};
		tv_weekday.setOnClickListener(daySettingListener);
		iv_weekday.setOnClickListener(daySettingListener);
		layout_weekday.setOnClickListener(daySettingListener);

		View.OnClickListener alarmSettingListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 알람 설정
				Intent intent = new Intent(getApplicationContext(), SettingsDetailActivity.class);
				switch (v.getId()) {
				case R.id.tv_settings_alarm01:
				case R.id.iv_settings_alarm01:
				case R.id.layout_settings_alarm01:
					intent.putExtra("mode", "alarm01");
					intent.putExtra("default", tv_alarm01.getText().toString()); // "00분" 시간이 전달되겠지
					break;
				case R.id.tv_settings_alarm02:
				case R.id.iv_settings_alarm02:
				case R.id.layout_settings_alarm02:
					intent.putExtra("mode", "alarm02");
					intent.putExtra("default", tv_alarm02.getText().toString()); // "00분" 시간이 전달되겠지
					break;
				}
				startActivity(intent);
			}
		};

		tv_alarm01.setOnClickListener(alarmSettingListener);
		iv_alarm01.setOnClickListener(alarmSettingListener);
		layout_alarm01.setOnClickListener(alarmSettingListener);
		tv_alarm02.setOnClickListener(alarmSettingListener);
		iv_alarm02.setOnClickListener(alarmSettingListener);
		layout_alarm02.setOnClickListener(alarmSettingListener);

		View.OnClickListener vibrateSettingListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SettingsDetailActivity.class);
				intent.putExtra("mode", "vibrate");
				intent.putExtra("default", tv_vibrate.getText().toString()); // "켬", "끔" 중 하나가 전달되겠지
				startActivity(intent);
			}
		};

		tv_vibrate.setOnClickListener(vibrateSettingListener);
		iv_vibrate.setOnClickListener(vibrateSettingListener);
		layout_vibrate.setOnClickListener(vibrateSettingListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// String name = "설정 화면";
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
		setDB();
		setDatas();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			Logs.d(Logs.LIFELINE, "SettingsActivity.onKeyBack");
			finish();
		}
		return false;
	}
	
	@Override
	public void finish() {
		setResult(RESULT_OK, getIntent());
		super.finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		setDB();
		StateSingleton.getInstance().stateCheckThread.updateCurrentSettings(currentSettings);
		StateSingleton.getInstance().setPstate(StateSingleton.PSTATE_MODE_AWAY);
	}
}
