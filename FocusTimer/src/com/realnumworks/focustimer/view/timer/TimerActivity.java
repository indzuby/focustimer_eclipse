package com.realnumworks.focustimer.view.timer;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.receiver.PowerOffEventReceiver;
import com.realnumworks.focustimer.singleton.DeviceControl;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.singleton.TimerSingleton;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.history.Record;
import com.realnumworks.focustimer.view.settings.Settings;

public class TimerActivity extends Activity {

	// private Tracker mTracker;

	private TypefaceTextView tv_min, tv_sec, tv_fifteen, tv_lap, tv_nowfocusing, tv_3sec;
	private LinearLayout btn_confirm;
	private LinearLayout layout_timer_main, layout_timer_refocus, layout_timer_focusingcircle;
	private Handler changeScreenTypeHandler;
	private String themeId;
	private Intent intent;
	private int alarmValue;
	long lappingTime = 0 ;
	private boolean alarmOn = false; // alarmOn이 true상태인 동안은 alarm을 울리지 않는다. (1분 sleep의 대안)
	private PowerOffEventReceiver saveIfPowerOffReceiver;
	Settings currentSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_timer);
		
		setCurrentSettingsFromDB();

		if (currentSettings.isVibrateOn()) {
			DeviceControl.getInstance().vibrate(400);// 0.4s 진동
		}

		// Get current theme id & alarm Value
		intent = getIntent();
		themeId = intent.getStringExtra("themeId");
		alarmValue = intent.getIntExtra("alarm", 0);

		// tstate Setting(to FOCUSING mode)
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_FOCUSING);

		setPowerOffReceiver();
		// Components Setup
		setupComponents();
		// tstate에 따라 screen의 종류를 변경해 보여줄 수 있도록 세팅한다.
		setChangeScreenTypeHandler();
		// Main에서 사용하던 Timer Thread Handler에 알람기능 추가
		setTimerThreadToAlarmable();
		// Screen Setup
		setupScreen();
	}

	private void setCurrentSettingsFromDB()
	{
		DataBaseHelper dbhelper = new DataBaseHelper(this);
		currentSettings = dbhelper.getSettings();
		dbhelper.close();
	}

	public void setPowerOffReceiver() {
		// Power Off Receiver Setting
		saveIfPowerOffReceiver = new PowerOffEventReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SHUTDOWN);
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
		registerReceiver(saveIfPowerOffReceiver, filter);
	}

	public void setupScreen() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		DeviceControl.getInstance().disableLockScreen();
		DeviceControl.getInstance().dimScreen(TimerActivity.this);
	}

	public void setTimerThreadToAlarmable() {
		// Main에서 사용하던 Timer Thread를 Alarm용으로 바꾼다.
		StateSingleton.getInstance().timerThread.setHandler(new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == 0) // msg가 0일 때(Focusing 모드일 때)
				{
					long currentTimeInSec = Calendar.getInstance().getTimeInMillis() / 1000;
					// 15초 타이머가 아예 실행된 적이 없거나, 적어도 실행되고 있지 않을 때
					// 측정 중인 시간 = 현재 시간 - 시작 시간
					long focusingTimeInSec = currentTimeInSec 
							- TimerSingleton.getInstance().getStartTime() 
							- TimerSingleton.getInstance().getTotalLappingTime() ;
					Logs.d(Logs.FOCUSINGTEST, "★측정 시작 후 " + DateTime.getTimeString(focusingTimeInSec, 2) + " 경과");
					long focusingTimeInMin = focusingTimeInSec / 60;
					if (alarmValue > 0) {
						if (focusingTimeInMin == alarmValue || focusingTimeInMin == alarmValue + 3 || focusingTimeInMin == alarmValue + 5) // 알람 시간, 알람시간 3분 후, 알람시간 5분 후에 도달하면
						{
							if (alarmOn == false) {
								DeviceControl.getInstance().vibrate(400);// 0.4s 진동
								alarmOn = true; // 알람이 울린적이 없으면, 알람을 울려준다.
							}
							Logs.d(Logs.VIBRATETEST, "여기서 호출됨 : 알람값 " + alarmValue + ", 측정초 " + focusingTimeInSec);
						} else {
							alarmOn = false;
						}
					}
				} else if (msg.what == 2) // Lapping 모드일 때, 15초 타이머의 역할을 대신한다.
				{
					boolean isLapping = TimerSingleton.getInstance().getIsLapping();
					long currentTimeInSec = Calendar.getInstance().getTimeInMillis() / 1000;
					TimerSingleton.getInstance();
					int fifteenTime = (int) (15 - (currentTimeInSec - TimerSingleton.getFinishTime()));
					// 큰 원 안의 집중 시간 갱신
					TimerSingleton.getInstance().refreshTextViewsTexts();

					Logs.d(Logs.FOCUSINGTEST, "★15초타이머 : " + DateTime.getTimeString(fifteenTime, 2));

					if (isLapping && fifteenTime > 0) // 15초 시간이 남아 있으면
					{
						if (StateSingleton.getInstance().getTstate() == StateSingleton.TSTATE_MODE_LAPPINGRESULT) {
							tv_fifteen.setText(fifteenTime + "");// 텍스트뷰에 갱신
						}
					} else if (fifteenTime == 0) {
						// 큰 원 안의 집중 시간 갱신
						TimerSingleton.getInstance().refreshTextViewsTexts();

						tv_lap.setText("이어 집중하기가 가능하지 않습니다.");
						layout_timer_refocus.setVisibility(View.GONE);
						TimerSingleton.getInstance().finishFifteenTimer();
						StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_RESULT);
					}
				}
			}
		});
	}

	/**
	 * tstate값에 따라 어떤 화면인지(Focusing/Lapping/Result) 판단하여 변경하는 Handler. msg.what값을 통해 받아온 current tstate값을 이용해 화면을 변경하여 보여준다.
	 */
	public void setChangeScreenTypeHandler() {
		changeScreenTypeHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case StateSingleton.TSTATE_MODE_FOCUSING: {
					// WakeLock 해제
					DeviceControl.getInstance().releaseScreenOnWakeLock();

					setResultVisibility(false); // 결과화면 지움
					DeviceControl.getInstance().dimScreen(TimerActivity.this);
					break;
				}
				case StateSingleton.TSTATE_MODE_LAPPINGRESULT: // LAPPING 화면이라면
				{
					Logs.d(Logs.MAIN_THREAD_CONFIRM, "TimerActivity.setChangeHandler()");
					// WakeLock 설정
					DeviceControl.getInstance().acquireScreenOnWakeLock();
					TimerSingleton.getInstance().updateLapCountTextView(); // 이어 집중 횟수 텍스트뷰 갱신
					TimerSingleton.getInstance().setFinishTime(); // 현재시간을 종료시간으로 정하고
					DeviceControl.getInstance().undimScreen(TimerActivity.this);
					setResultVisibility(true); // 결과화면 보여줌
					break;
				}
				case StateSingleton.TSTATE_MODE_RESULT: // 완전한 측정 완료 화면이라면
				{
					// WakeLock 설정
					DeviceControl.getInstance().acquireScreenOnWakeLock();
					TimerSingleton.getInstance().updateLapCountTextView(); // 이어 집중 횟수 텍스트뷰 갱신
					DeviceControl.getInstance().undimScreen(TimerActivity.this);
					setResultVisibility(true); // 결과화면 보여줌
					TimerSingleton.getInstance().setFinishTime(); // 현재시간을 종료시간으로 정한다
					TimerSingleton.getInstance().refreshTextViewsTexts();
					break;
				}
				}
			}
		};
		StateSingleton.getInstance().stateCheckThread.setChangeScreenTypeHandler(changeScreenTypeHandler);
	}

	/**
	 * 결과 화면과 집중시작 텍스트뷰 중 어떤 것을 보여줄까?
	 * 
	 * @param b
	 */
	public void setResultVisibility(boolean b) {
		Logs.d(Logs.MAIN_THREAD_CONFIRM, "TimerActivity.setResultVisibility()");
		if (b) {
			layout_timer_main.setVisibility(View.VISIBLE);
			for (int i = 0; i < layout_timer_main.getChildCount(); i++) {
				layout_timer_main.getChildAt(i).setVisibility(View.VISIBLE);
			}
			TimerSingleton.getInstance();
			if (TimerSingleton.getFinishTime() - TimerSingleton.getInstance().getStartTime() < 3) // 집중시간이 3초 미만일 경우
			{
				show3SecTextView(true);
			} else {
				show3SecTextView(false);
			}

			tv_nowfocusing.setText("");
			tv_nowfocusing.setVisibility(View.GONE);
		} else {
			layout_timer_main.setVisibility(View.GONE);
			for (int i = 0; i < layout_timer_main.getChildCount(); i++) {
				layout_timer_main.getChildAt(i).setVisibility(View.GONE);
			}
			tv_nowfocusing.setText("Focusing");
			tv_nowfocusing.setVisibility(View.VISIBLE);
			Logs.d("test", "visibility test : tv_now = " + tv_nowfocusing.getVisibility() + " result = " + layout_timer_main.getVisibility());
		}
	}

	/**
	 * 동그라미 영역을 없앨지 결정
	 */
	public void show3SecTextView(boolean b) {
		Logs.d(Logs.MAIN_THREAD_CONFIRM, "TimerActivity.show3secTextView()");
		if (b) {
			layout_timer_focusingcircle.setVisibility(View.GONE);
			tv_3sec.setVisibility(View.VISIBLE);
		} else {
			layout_timer_focusingcircle.setVisibility(View.VISIBLE);
			tv_3sec.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("KEYCODE", keyCode+"");
		if(keyCode != KeyEvent.KEYCODE_HOME && keyCode != KeyEvent.KEYCODE_MOVE_HOME &&keyCode!=KeyEvent.KEYCODE_POWER) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	/** 
	 * FIXME find more effective way
	 * - To Image Sampling cuz Out Of Memory Error
	 **/
	public Drawable getResizedDrawbleResource(int resId, int width, int height) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 8; // can be changed
		Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.bg_main, options);
		Bitmap resized = Bitmap.createScaledBitmap(src, width, height, true);
		return new BitmapDrawable(getResources(), resized);
	}

	public void setupComponents() {
		tv_min = (TypefaceTextView) findViewById(R.id.tv_timer_min);
		tv_sec = (TypefaceTextView) findViewById(R.id.tv_timer_sec);
		tv_fifteen = (TypefaceTextView) findViewById(R.id.tv_timer_fifteen);
		tv_lap = (TypefaceTextView) findViewById(R.id.tv_timer_lap);
		tv_3sec = (TypefaceTextView) findViewById(R.id.tv_timer_3sec);

		tv_nowfocusing = (TypefaceTextView) findViewById(R.id.tv_timer_nowfocusing);
		setViewFadeInAndOut(tv_nowfocusing);

		layout_timer_main = (LinearLayout) findViewById(R.id.layout_timer_main);
		layout_timer_refocus = (LinearLayout) findViewById(R.id.layout_timer_refocus);
		layout_timer_focusingcircle = (LinearLayout) findViewById(R.id.layout_timer_focusingcircle);
		setViewFadeInAndOut(layout_timer_main);

		// Confirm Button Setting
		btn_confirm = (LinearLayout) findViewById(R.id.btn_timer_confirm);
		btn_confirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				confirm();
			}
		});

		TimerSingleton.getInstance().setTextViews(tv_min, tv_sec, tv_fifteen, tv_lap, tv_3sec);
		TimerSingleton.getInstance().setTimerMainLayout(layout_timer_main);
		TimerSingleton.getInstance().setRefocusLayout(layout_timer_refocus);
		TimerSingleton.getInstance().setFocusingCircleLayout(layout_timer_focusingcircle);
		// TimerSingleton.getInstance().setNowFocusingTextView(tv_nowfocusing);

		// Background Image Resize
		DisplayMetrics dm = getResources().getDisplayMetrics();
		Drawable bg = getResizedDrawbleResource(R.drawable.bg_main, dm.widthPixels, dm.heightPixels);
		RelativeLayout layout_whole = (RelativeLayout) findViewById(R.id.layout_timer_whole);
		layout_whole.setBackground(bg);
	}

	/**
	 * confirm 버튼을 눌렀을 때 수행되는 작업.
	 * 
	 */
	public void confirm() {

		// 스레드가 돌고 있으면 꺼준다.
		if (TimerSingleton.getInstance().getIsLapping())
			TimerSingleton.getInstance().finishFifteenTimer();
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_MAIN);
		Logs.d(Logs.LIFELINE, "TimerActivity.confirm");

		// wakelock도 해제해준다.
		DeviceControl.getInstance().releaseScreenOnWakeLock();
		DeviceControl.getInstance().enableLockScreen();

		int focusTimeSec = TimerSingleton.getInstance().getFocusTimeInSec();
		long startTime = TimerSingleton.getInstance().getStartTime();

		new Record((int) startTime);

		DataBaseHelper dbm = new DataBaseHelper(this);

		if (focusTimeSec >= 3) {
			dbm.insertRecord((int) startTime, focusTimeSec, themeId);
			StateSingleton.getInstance().lastFocusedTimeInSec = (int) (startTime + focusTimeSec);
		}

		Logs.d(Logs.DEBUG, "4 current theme id = " + StateSingleton.getInstance().currentThemeId);
		Logs.d(Logs.DEBUG, "44 current theme id = " + StateSingleton.getInstance().currentThemeId);

		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setViewFadeInAndOut(View v) {
		AlphaAnimation fadeIn = new AlphaAnimation(1.0f, 0.0f);
		AlphaAnimation fadeOut = new AlphaAnimation(0.0f, 1.0f);
		v.startAnimation(fadeIn);
		v.startAnimation(fadeOut);
		fadeIn.setDuration(100);
		fadeIn.setFillAfter(true);
		fadeOut.setDuration(100);
		fadeOut.setFillAfter(true);
		// fadeOut.setStartOffset(4200+fadeIn.getStartOffset());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Logs.d(Logs.LIFELINE, "TimerActivity.onDestroy");
		Logs.d(Logs.DEBUG, "5 current theme id = " + StateSingleton.getInstance().currentThemeId);
		unregisterReceiver(saveIfPowerOffReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// String name = getClass().getSimpleName();
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
	
	@Override
	public void finish() {
		setResult(RESULT_OK, getIntent());
		super.finish();
	}
}