package com.realnumworks.focustimer.view.main;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.Codes;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.receiver.ScreenOnOffReceiver;
import com.realnumworks.focustimer.service.StateCheckService;
import com.realnumworks.focustimer.service.ThreadControlService;
import com.realnumworks.focustimer.singleton.DeviceControl;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.DrawableHelper;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.history.Record;
import com.realnumworks.focustimer.view.settings.Settings;
import com.realnumworks.focustimer.view.settings.SettingsActivity;
import com.realnumworks.focustimer.view.theme.Theme;
import com.realnumworks.focustimer.view.theme.ThemeListActivity;
import com.realnumworks.focustimer.view.timer.TimerActivity;
import com.realnumworks.focustimer.view.tutorial.TutorialHelper;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * 알람 메뉴, 파란 배경 부분(실제 메인화면), 하단 바 전부를 나타내는 부분. 앱 최초실행시 이 Class가 호출되므로 앱의 최초 실행에 필요한 부분도 같이 담고 있다. 이 부분은 이후 튜토리얼이나 Splash가 추가되면 그쪽으로 넘어갈 예정 단, 파란 배경 부분의 Component는 건들지 못한다.
 * 
 * @author Yedam
 * 
 */
public class MainActivity extends MainViewPagerActivity implements View.OnClickListener {

	// private Tracker mTracker;

	RelativeLayout layout_main_whole;
	RelativeLayout layout_alarmbottom, layout_pager, layout_main_indicator;
	int indicator_height;
	Fragment mainFragment;
	ArrayList<Theme> themeList;
	TypefaceTextView tv_lastFocusedTime;
	LinearLayout layout_alarmIconAndTextView;
	TypefaceTextView tv_alarm = null;
	DataBaseHelper dbm;
	Settings set;
	ImageButton btn_settings, btn_menu;
	BroadcastReceiver mScreenOnOffReceiver;
	TutorialHelper tutorialHelper = null;
	Intent stateCheckServiceIntent;

	//	//상단 슬라이드업 메뉴에 필요한 것들
	//	FrameLayout layout_black;
	//	LinearLayout layout_alarm;
	//	TextView[] tv_alarmbtns;

	// 스크롤뷰(알람 메뉴 상하 스크롤)에 필요한 것들
	//	int scroll_y;
	//	private ViewTreeObserver mObserver = null;
	//	private OnScrollChangedListener mScrollChangedListener = null;

	//스레드 시작 서비스 인텐트
	Intent threadServiceIntent;

	/**
	 * Class 호출시 자동실행된다. 화면이 Load될 때 호출되므로 기본적인 로직은 여기에 있음
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication) getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_main);
		Logs.d(Logs.LIFELINE, "MainActivity.onCreate");

		// 최초의 Setting을 DB에서 받아와서 변수에 삽입
		setSettingsFromDB();

		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_MAIN);

		registerScreenOnOffReceiver();

		// 등록된 모든 테마 리스트 불러오기
		loadThemeList();

		// LogTags.printLog(LogTags.DUMMYTEST, "더미 데이터 삽입");
//		insertDummyDatas();

		// WakeLock 초기세팅
		DeviceControl.getInstance().setScreenOnWakeLock();

		setLastFocusedTimeFromDB();

		// 일요일시작-월요일시작 설정(MainFragment에서 표에서 보여줄 예정)
		StateSingleton.getInstance().setDstate(set.getStartWith());

		// Components Setting
		setComponents();
		// setChangeAlphaHandler();

		//		initScrollView();
		//		setAlarmMenu();

		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

		// State Check Thread & Time Thread Start
//		startStateCheckService();
		startThreadService();

		// 하단 바 높이 구하기
		indicator_height = findViewById(R.id.layout_main_indicator).getLayoutParams().height;

		// States setting(최초의 TSTATE는 Main)
		// StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_MAIN);

		// Background Image Resize
		DisplayMetrics dm = getResources().getDisplayMetrics();
		Drawable backGround = DrawableHelper.getResizedDrawbleResource(getResources(), R.drawable.bg_main, dm.widthPixels, dm.heightPixels);
		layout_main_whole = (RelativeLayout)findViewById(R.id.layout_main_whole);
		layout_main_whole.setBackground(backGround);

		tutorialHelper = new TutorialHelper(this, dbm, set);
		popupTutorials();
		// if(!s.isTutorial02Shown()) setTutorial02();

		// Current index setting
		if (StateSingleton.getInstance().currentThemeId == null) {
			StateSingleton.getInstance().currentThemeId = themeList.get(0).getId();
			Logs.d(Logs.DEBUG, "1 current theme id = " + StateSingleton.getInstance().currentThemeId);
		}

		// mPager, mAdapter setting
		setPageAdapterIndicators();

		Logs.d("MainActivity", "onCreate");
	}
	
	public void startStateCheckService(){
		stateCheckServiceIntent  = new Intent(this, StateCheckService.class);
		startService(stateCheckServiceIntent);
	}

	public void stopStateCheckService(){
		stopService(stateCheckServiceIntent);
	}

	private void popupTutorials()
	{

		Logs.d("popset : " + set.toString());

		if (!set.isTutorial01Shown()) { //1, 2 모두 안보여졌을때
			TutorialHelper.popupTutorial01(this);
		}
		else if (!set.isTutorial02Shown()) { //2만 안보여졌을때
			TutorialHelper.popupTutorial02(this);
		}
		if (!set.isRedAlertShown()) {
			tutorialHelper.setTutorial03();
		}

	}

	private void loadThemeList() {
		themeList = dbm.getThemesListOfAll();
		if (themeList.size() == 0) {
			Theme theme01 = new Theme("0", "책 읽기", 0, 0); // 초기 3테마의 Id는 0, 1, 2로 고정된다.
			Theme theme02 = new Theme("1", "공부", 1, 1);
			Theme theme03 = new Theme("2", "명상", 2, 2);
			dbm.insertTheme(theme01);
			dbm.insertTheme(theme02);
			dbm.insertTheme(theme03);
			themeList = dbm.getThemesListOfAll();
		}
	}

	public void registerScreenOnOffReceiver() {
		IntentFilter screenStateFilter = new IntentFilter();
		screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
		mScreenOnOffReceiver = new ScreenOnOffReceiver();
		registerReceiver(mScreenOnOffReceiver, screenStateFilter);
	}

	private void setSettingsFromDB() {
		Logs.w("== setInitialSettingsFromDB");
		if (dbm == null) {
			dbm = new DataBaseHelper(this);
		}
		if (dbm.getSettings().getStartWith() == 0) {
			dbm.insertInitSettings();
		}
		set = dbm.getSettings();
//		StateSingleton.getInstance().stateCheckThread.updateCurrentSettings(set);
		
	}

	public void setLastFocusedTimeFromDB() {
		// Last Focused Time 불러오기
		int currentSec = (int)(Calendar.getInstance().getTimeInMillis() / 1000);
		int todaySec = currentSec;
		if (new Record(currentSec).getHour() < 4) // 오늘 4시 이전이면
		{
			// 전날로 편입
			todaySec = Record.getPrevDate(new Record(currentSec)).getTimeInSec();
			Logs.d(Logs.TICKTEST, "현재 시간(변경 후) : " + new Record(todaySec).toShortString());
		}
		StateSingleton.getInstance().lastFocusedTimeInSec = DateTime.getLastFocusedTimeInSecForSelectedDay(todaySec, getApplicationContext());
	}

	//	//스크롤뷰 초기 위치 설정
	//	public void initScrollView() {
	//		// 스크롤뷰 초기 위치 : 알람 메뉴 맨 아래
	//		sv_main.postDelayed(new Runnable() {
	//			@Override
	//			public void run() {
	//				Logs.d(Logs.DEBUG, "[before] scroll Y = "+sv_main.getScrollY());
	//				Logs.d(Logs.DEBUG, "alarm Height = "+layout_alarm.getLayoutParams().height);
	//				sv_main.setScrollY(layout_alarm.getLayoutParams().height);
	//				Logs.d(Logs.DEBUG, "[after] scroll Y = "+sv_main.getScrollY());
	//				sv_main.invalidate();
	//			}
	//		}, 100);
	//	}

	public void startThreadService()
	{
		setStateCheckThread();
		setTimerThreadToUpdateLastFocusedTime();

		//Service 실행
		threadServiceIntent = new Intent(this, ThreadControlService.class);
		startService(threadServiceIntent);
	}

	/**
	 * 스레드 핸들러 설정 ShowTimerActivityHandler : 측정화면 띄움
	 */
	public void setStateCheckThread() {
		if (!StateSingleton.getInstance().stateCheckThread.getState().equals(State.NEW)) {
			Logs.w("! already stateCheckThread :" + StateSingleton.getInstance().stateCheckThread.getState());
			return;
		}
		Logs.i("! Call stateCheckThread :" + StateSingleton.getInstance().timerThread.getState());
		StateSingleton.getInstance().stateCheckThread.setShowTimerActivityHandler(new Handler() {
			public void handleMessage(android.os.Message msg) {
				showTimerActivity();
			}
		});
		StateSingleton.getInstance().stateCheckThread.setDaemon(true); // 프로그램이 죽으면 스레드가 같이 죽게 함
		//		StateSingleton.getInstance().stateCheckThread.start();
	}

	/**
	 * 메인 타이머 스레드 설정 : Last Foused Time 갱신 기능
	 * Focusing 화면에서 Main 화면에 진입할 경우 Handler를 새로 Setting 해주어야 한다.
	 */
	public void setTimerThreadToUpdateLastFocusedTime() {
		Logs.i("! Call timerThread :" + StateSingleton.getInstance().timerThread.getState());
		StateSingleton.getInstance().timerThread.setHandler(new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == 1 || msg.what == -1) // msg가 1일 때(Main 모드일 때)
				{
					// 0.5초 간격으로 동작함
					Logs.d(Logs.THREADTEST, "Refresh CurrentTime Handler Called");
					int currentSec = (int)(Calendar.getInstance().getTimeInMillis() / 1000);
					int todaySec = currentSec;
					Logs.d(Logs.TICKTEST, "현재 시간 : " + new Record(todaySec).toShortString());
					if (new Record(currentSec).getHour() < 4) { // 오늘 4시 이전이면
						// 전날로 편입
						todaySec = Record.getPrevDate(new Record(currentSec)).getTimeInSec();
						Logs.d(Logs.TICKTEST, "현재 시간(변경 후) : " + new Record(todaySec).toShortString());
					}

					// 4:00 정각이 되면 Last Focused Time 갱신
					if (new Record(currentSec).getHour() == 4 && new Record(currentSec).getMin() == 0
						&& new Record(currentSec).getSec() == 0) {
						setLastFocusedTimeFromDB();
					}

					int lastfocusSec = StateSingleton.getInstance().lastFocusedTimeInSec;
					// ago 설정
					String agohms = "";
					if (currentSec == 0) {
						agohms = "Error";
					} else if (lastfocusSec == 0) {
						agohms = "No Record Today";
					} else {
						int agosec = currentSec - lastfocusSec; // 최종 측정시간으로부터 지난 시간
						int times[] = DateTime.getHourMinSecFromTimeSec(agosec); // times[0] : hour, times[1] : min, times[2] : sec
						agohms = "";
						if (times[0] > 0) {
							agohms += times[0] + "시간 ";
							if (times[1] > 0) {
								agohms += times[1] + "분 ";
							}
						} else {
							if (times[1] > 0) {
								agohms += times[1] + "분 ";
							}
							agohms += times[2] + "초 ";
						}
						agohms += "지남";
					}
					tv_lastFocusedTime.setText(agohms);
				}
			}
		});

		if (!StateSingleton.getInstance().timerThread.getState().equals(State.NEW)) {
			Logs.w("! already timerThread :" + StateSingleton.getInstance().timerThread.getState());
			return;
		}
		else
			StateSingleton.getInstance().timerThread.setDaemon(true);
	}

	public void setPageAdapterIndicators() {
		// Adapter Setting
		loadThemeList();
		if(mAdapter!=null && mAdapter.getCount() != themeList.size()) {
			mAdapter.update(themeList);
			
		}else {
			mAdapter = new MainFragmentAdapter(getSupportFragmentManager());
			// ViewPager & Indicator Setting
			mPager = (ViewPager)findViewById(R.id.pager);
			mPager.setAdapter(mAdapter);
		}
		Logs.d(Logs.DEBUG, "2 current theme id = " + StateSingleton.getInstance().currentThemeId);

		mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);

		mIndicator.setViewPager(mPager);

		// 시작 시 페이지 인덱스
		int currentIndex = dbm.getThemeById(StateSingleton.getInstance().currentThemeId).getOrder();
		mPager.setCurrentItem(currentIndex);
		mIndicator.setCurrentItem(currentIndex);
		Logs.d(Logs.DEBUG, "current theme id = " + StateSingleton.getInstance().currentThemeId + ", index = "
			+ currentIndex);
		mPager.setOffscreenPageLimit(7);
		mAdapter.setContent();
	}

	/**
	 * Components Setting
	 */
	@SuppressLint("ResourceAsColor")
	public void setComponents() {

		// Button setting
		btn_settings = (ImageButton)findViewById(R.id.btn_settings);
		btn_menu = (ImageButton)findViewById(R.id.btn_menu);
		Logs.d("btnsettings", "가로 : " + btn_settings.getDrawable().getMinimumWidth());
		btn_settings.setOnClickListener(this);
		btn_menu.setOnClickListener(this);

		layout_alarmbottom = (RelativeLayout)findViewById(R.id.layout_main_alarmbottom);
		layout_main_indicator = (RelativeLayout)findViewById(R.id.layout_main_indicator);
		tv_lastFocusedTime = (TypefaceTextView)findViewById(R.id.tv_lastfocusedtime);

		layout_alarmIconAndTextView = (LinearLayout)findViewById(R.id.layout_main_alarmIconAndTextView);
		tv_alarm = (TypefaceTextView)findViewById(R.id.tv_alarm);
		setCurrentAlarmValue();
		setAlarmIconOnClickListener();

		//		//상단 메뉴 관련 컴포넌트
		//		layout_alarm = (LinearLayout)findViewById(R.id.layout_main_alarm);
		//		layout_black = (FrameLayout)findViewById(R.id.layout_black);
		//		layout_black.bringToFront();
		//		layout_black.setVisibility(View.GONE);
		//		layout_black.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View arg0) {
		//				if (layout_black.getAlpha() > 0) // 투명도가 0이상일때 : 알람메뉴가 올라간 상태에서
		//				{
		//					doSmoothScrollTo(sv_main, layout_alarm.getLayoutParams().height);
		//				}
		//			}
		//		});
		//		layout_black.invalidate();
		//
	}

	private void setCurrentAlarmValue()
	{
		String alarmValue = "";
		switch (set.alarmSelected)
		{
			case 0:
				alarmValue = "없음";
				break;
			case 1:
				alarmValue = set.getAlarm01() + "분";
				break;
			case 2:
				alarmValue = set.getAlarm02() + "분";
				break;
		}
		tv_alarm.setText(alarmValue);
	}

	public void setAlarmIconOnClickListener()
	{
		layout_alarmIconAndTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (StateSingleton.getInstance().getTstate() == StateSingleton.TSTATE_MODE_MAIN) //알람메뉴가 내려간 상태에서
				{
					//					doSmoothScrollTo(sv_main, 0); // 올린다
					Intent alarmSettingMenuIntent = new Intent(v.getContext(), AlarmActivity.class);
					StateSingleton.getInstance().currentThemeId = getThemeId();
					startActivityForResult(alarmSettingMenuIntent, Codes.RequestCode.ALARM_SETTING_MENU);
					overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				}
			}
		});
	}

	/**
	 * 설정, 메뉴버튼을 누르면 해당 화면으로 가게 함
	 */
	@SuppressLint("ResourceAsColor")
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		int requestCode = -1;
		switch (v.getId()) {
			case R.id.btn_settings:
				intent = new Intent(this, SettingsActivity.class);
				requestCode = Codes.RequestCode.SETTINGS;
				break;
			case R.id.btn_menu:
				intent = new Intent(this, ThemeListActivity.class);
				requestCode = Codes.RequestCode.THEMES;
				break;
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		StateSingleton.getInstance().currentThemeId = getThemeId();
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_OTHER); // TSTATE를 OTHER로 Setting하고 이동
		startActivityForResult(intent, requestCode);
	}

	public void showTimerActivity() {
		Intent intent = new Intent(this, TimerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("themeId", getThemeId());
		StateSingleton.getInstance().currentThemeId = getThemeId();
		intent.putExtra("alarm", set.getAlarmValueSelected()); // 선택된 알람 값(15분, 30분 등) 전달
		startActivityForResult(intent, Codes.RequestCode.TIMER);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK)
		{
			Logs.d(Logs.LIFELINE, "MainActivity.onActivityResult : requestCode " + requestCode);
			setSettingsFromDB();
			switch (requestCode) {
				case (Codes.RequestCode.SETTINGS):
				case (Codes.RequestCode.ALARM_SETTING_MENU):
					setCurrentAlarmValue();
					break;
				case (Codes.RequestCode.THEMES):
				case (Codes.RequestCode.RECORD):
				case (Codes.RequestCode.TIMER):
					setLastFocusedTimeFromDB();
					setTimerThreadToUpdateLastFocusedTime();
					break;
				default:
					break;
			}
		}
	}

	/**
	 * 백그라운드로 나갔다가 다시 돌아올 때 초기 설정 부분
	 */
	@SuppressLint("ResourceAsColor")
	@Override
	protected void onResume() {
		super.onResume();
		// String name = getClass().getSimpleName();
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
		Logs.d(Logs.LIFELINE, "MainActivity.onResume");

		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_MAIN);
		setPageAdapterIndicators();
		mIndicator.notifyDataSetChanged();
		setTimerThreadToUpdateLastFocusedTime();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Logs.d(Logs.LIFELINE, "MainActivity.onStart");
	}

	@Override
	protected void onStop() {
		Logs.d(Logs.LIFELINE, "MainActivity.onStops");
		StateSingleton.getInstance().currentThemeId = getThemeId();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mScreenOnOffReceiver);
		dbm.close();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) // 뒤로가기 키를 눌렀을 때, 알림창 띄우고 종료
	{
		Log.d("KEYCODE", keyCode+"");

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);

			alertDlg.setTitle("종료하시겠습니까?");
			alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() { // 확인 버튼
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					moveTaskToBack(true);
					stopService(threadServiceIntent); //스레드 관리 서비스 종료
					System.exit(0); // 시스템 종료
				}
			});
			alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() { // 취소 버튼
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {

					dialog.cancel();
				}
			});
			AlertDialog alert = alertDlg.create();
			alert.show();
		}
		return false;
	}

	/**
	 * 백그라운드로 나갈 때, 프로세스 종료 또는 백그라운드로 나가기 전에 호출된다.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Logs.d("startTest", "MainActivity.onPause");
		Logs.d(Logs.LIFELINE, "onPause");
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_OTHER);
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		Logs.d("startTest", "MainActivity.onUserLeaveHint");
		Logs.d(Logs.LIFELINE, "onUserLeaveHint");
	}

	public String getThemeId() {
		int currentFragmentPosition = mPager.getCurrentItem();
		String themeId = MainFragmentAdapter.content.get(currentFragmentPosition);
		Logs.d("MainActivity.showTimerActivity", "themetest id 전달 : " + themeId);
		return themeId;
	}

	/** input Dummy Datas **/
	public void insertDummyDatas() {
		//모든 테마(7개)에
		//하루 3개씩, 각 8시간 미만의 데이터를 넣는다.
		//더미 데이터 들어가는 날짜 : 1년 전

		Calendar cal = Calendar.getInstance();

		for (int themeSize = 0; themeSize < themeList.size(); themeSize++) {
			int year = 2010, month = Calendar.OCTOBER, day = 8, hour = 1;
//			for (int index = 0; index < 770; index++)
			for (int index = 0; index < 300; index++)
			{
				cal.set(year, month, day, hour, 0, 0);//시작 : 1년 전
				Record record = new Record(year, month + 1, day, hour, 0, 0);
				Random rand = new Random();
				int focustime = rand.nextInt(28799) + 1;
				record.setFocustime(focustime);
				Logs.d(Logs.DUMMYTEST, "●더미 : " + themeList.get(themeSize).getName() + ", " + year + "/" + (month + 1)
					+ "/" + day + " " + hour + "시, " + focustime + "초 집중");
				dbm.insertRecord(record.getTimeInSec(), focustime, themeList.get(themeSize).getId());
				switch (hour) {
					case 1:
						hour = 9;
						break;
					case 9:
						hour = 17;
						break;
					case 17:
						hour = 1;
						//이 달의 마지막 날일 경우 다음 달 1일로 ㄱㄱ
						if (day == cal.getActualMaximum(Calendar.DATE)) {
							//12월일 경우 1월 1일로 ㄱㄱ
							if (month == Calendar.DECEMBER) {
								month = Calendar.JANUARY;
								year++;
							}
							else
								month++;
							day = 1;
						}
						else
							day++;
						break;
				}
			}
		}
	}

	//	/**
	//	 * 알람메뉴의 버튼별 행동을 설정
	//	 */
	//	public void setAlarmMenu() {
	//		Logs.d(Logs.LIFELINE, "setAlarmMenu()");
	//		tv_alarmbtns = new TextView[3];
	//		tv_alarmbtns[0] = (TextView)findViewById(R.id.tv_alarmbtn_none);
	//		tv_alarmbtns[1] = (TextView)findViewById(R.id.tv_alarmbtn_01);
	//		tv_alarmbtns[2] = (TextView)findViewById(R.id.tv_alarmbtn_02);
	//
	//		changeAlarmButtonValue();
	//
	//		View.OnClickListener alarmListener = new View.OnClickListener() {
	//			@Override
	//			public void onClick(View v) {
	//				switch (v.getId()) {
	//					case R.id.tv_alarmbtn_none:
	//						set.setAlarmSelected(0);
	//						break;
	//					case R.id.tv_alarmbtn_01:
	//						set.setAlarmSelected(1);
	//						break;
	//					case R.id.tv_alarmbtn_02:
	//						set.setAlarmSelected(2);
	//						break;
	//				}
	//				setAlarmButtonOn(set.getAlarmSelected());
	//				dbm.modifySettings(set);
	//			}
	//		};
	//		for (int i = 0; i < tv_alarmbtns.length; i++) {
	//			tv_alarmbtns[i].setOnClickListener(alarmListener);
	//		}
	//
	//		setOnScrollChangedListener();
	//		setScrollbarTouchListener(true);
	//		// 스크롤 변경 리스너 추가
	//		if (mObserver == null) {
	//			mObserver = sv_main.getViewTreeObserver();
	//			mObserver.addOnScrollChangedListener(mScrollChangedListener);
	//		} else if (!mObserver.isAlive()) {
	//			mObserver.removeOnScrollChangedListener(mScrollChangedListener);
	//			mObserver = sv_main.getViewTreeObserver();
	//			mObserver.addOnScrollChangedListener(mScrollChangedListener);
	//		}
	//	}
	//	
	//	public void changeAlarmButtonValue()
	//	{
	//		tv_alarmbtns[1].setText(set.getAlarm01() + "분");
	//		tv_alarmbtns[2].setText(set.getAlarm02() + "분");
	//
	//		setAlarmButtonOn(set.getAlarmSelected());
	//	}
	//
	//	/**
	//	 * 알람버튼 중 하나만 켠다.
	//	 * 
	//	 * @param index
	//	 */
	//	public void setAlarmButtonOn(int index) {
	//		// 알람버튼 모두의 배경을 하얀색으로
	//		tv_alarmbtns[0].setBackgroundResource(R.drawable.ic_circle_lightgrey_3x);
	//		tv_alarmbtns[1].setBackgroundResource(R.drawable.ic_circle_lightgrey_3x);
	//		tv_alarmbtns[2].setBackgroundResource(R.drawable.ic_circle_lightgrey_3x);
	//
	//		// 알람버튼 모두의 글자색을 jadeblue로
	//		for (int i = 0; i < tv_alarmbtns.length; i++) {
	//			tv_alarmbtns[i].setTextColor(getResources().getColor(R.color.ft_jadeblue));
	//		}
	//
	//		// 선택된 알람버튼의 배경을 하얀색으로, 글자색을 jadeblue로
	//		switch (index) {
	//			case 0:
	//				tv_alarmbtns[index].setBackgroundResource(R.drawable.ic_circle_jadeblue_3x);
	//				tv_alarmbtns[index].setTextColor(getResources().getColor(R.color.ft_white));
	//				break;
	//			case 1:
	//				tv_alarmbtns[index].setBackgroundResource(R.drawable.ic_circle_jadeblue_3x);
	//				tv_alarmbtns[index].setTextColor(getResources().getColor(R.color.ft_white));
	//				break;
	//			case 2:
	//				tv_alarmbtns[index].setBackgroundResource(R.drawable.ic_circle_jadeblue_3x);
	//				tv_alarmbtns[index].setTextColor(getResources().getColor(R.color.ft_white));
	//				break;
	//		}
	//
	//		// 하단 알람 아이콘 우측 텍스트 바꾸기
	//		TypefaceTextView tv_alarm = (TypefaceTextView)findViewById(R.id.tv_alarm);
	//		tv_alarm.setText(tv_alarmbtns[index].getText());
	//	}

	//	/**
	//	 * 스크롤 메뉴 리스너 설정
	//	 */
	//	public void setScrollbarTouchListener(boolean b) {
	//		Logs.d(Logs.LIFELINE, "setScrollbarTouchListener()");
	//		if (b) {
	//			sv_main.setOnTouchListener(new View.OnTouchListener() {
	//				@Override
	//				public boolean onTouch(View v, MotionEvent event) {
	//
	//					final int alarmHeight = layout_alarm.getLayoutParams().height;
	//					final int scrollto;
	//
	//					Logs.d(Logs.TUTORIAL, "터치 리스너 설정");
	//
	//					boolean isScrollUp = true;
	//
	//					switch (event.getAction()) {
	//						case MotionEvent.ACTION_DOWN:
	//							break;
	//						case MotionEvent.ACTION_UP:
	//
	//							if (StateSingleton.getInstance().getTstate() == StateSingleton.TSTATE_MODE_MAIN) // 현재 상태가 메인이라면
	//							{
	//								// OTHER로 바꿔주고
	//								StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_OTHER);
	//								isScrollUp = true; // 스크롤을 올려야함을 알려줌
	//							} else if (StateSingleton.getInstance().getTstate() == StateSingleton.TSTATE_MODE_OTHER) // 현재상태가 아더라면(알람올라온상태)
	//							{
	//								isScrollUp = false; // 스크롤을 내려야함을 알려줌
	//							}
	//							Logs.d(Logs.SCROLLTEST, "isScrollup : " + isScrollUp);
	//
	//							scroll_y = sv_main.getScrollY();
	//							Logs.d("mo", "svontouch " + scroll_y);
	//							if (scroll_y < alarmHeight) // 스크롤이 알람 메뉴 내부에 있을 경우
	//							{
	//
	//								// 0.9.2 수정
	//								if (scroll_y > alarmHeight - 50) // 맨 아래부터 위로 30까지일 때는
	//								{
	//									isScrollUp = false; // 스크롤 내리고
	//								}
	//
	//								if (isScrollUp) // 스크롤을 올려야할때
	//								{
	//									scrollto = 0; // 욜려주고
	//								} else // 내려야할때
	//								{
	//									scrollto = alarmHeight; // 내려준다.
	//								}
	//							} else // 메인화면까지 갔을 경우
	//							{
	//								scrollto = layout_alarm.getLayoutParams().height;
	//								StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_MAIN);
	//							}
	//							sv_main.post(new Runnable() {
	//								@Override
	//								public void run() {
	//									doSmoothScrollTo(sv_main, scrollto);
	//									return;
	//								}
	//							});
	//
	//							break;
	//					}
	//					return false;
	//				}
	//			});
	//		} else {
	//			sv_main.setOnTouchListener(null);
	//		}
	//	}

	//	/**
	//	 * 느리게 스크롤 시켜주는 함수
	//	 */
	//	public void doSmoothScrollTo(final ScrollView sv, final int targetScrollY) {
	//		layout_black.setVisibility(View.VISIBLE);
	//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	//			ValueAnimator realSmoothScrollAnimation = ValueAnimator.ofInt(sv.getScrollY(), targetScrollY);
	//			realSmoothScrollAnimation.setDuration(300);
	//			realSmoothScrollAnimation.addUpdateListener(new AnimatorUpdateListener() {
	//				@Override
	//				public void onAnimationUpdate(ValueAnimator animation) {
	//					int scrollTo = (Integer)animation.getAnimatedValue();
	//					sv.smoothScrollTo(0, scrollTo);
	//
	//				}
	//			});
	//			realSmoothScrollAnimation.start();
	//		} else {
	//			sv.smoothScrollTo(0, targetScrollY);
	//		}
	//		if (targetScrollY == layout_alarm.getLayoutParams().height) {
	//			StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_MAIN);
	//		}
	//	}

	//	/**
	//	 * 스크롤 시 스크롤 위치가 변경되었을 때 호출되는 리스너를 정의한다.
	//	 */
	//	public void setOnScrollChangedListener() {
	//		Logs.d(Logs.LIFELINE, "setOnScrollChangedListener()");
	//		mScrollChangedListener = new OnScrollChangedListener() {
	//
	//			@Override
	//			public void onScrollChanged() {
	//				int mScrollY = sv_main.getScrollY();
	//				Logs.d(Logs.DEBUG, "mScrollY = " + mScrollY);
	//				if (mScrollY > layout_alarm.getLayoutParams().height) {
	//					mScrollY = layout_alarm.getLayoutParams().height;
	//				}
	//
	//				if (mScrollY == layout_alarm.getLayoutParams().height) {
	//					StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_MAIN);
	//					layout_black.setVisibility(View.GONE);
	//					Logs.d(Logs.DEBUG, "layout_black GONE");
	//				} else {
	//					layout_black.setVisibility(View.VISIBLE);
	//					Logs.d(Logs.DEBUG, "layout_black VISIBLE");
	//				}
	//
	//				int percent = 200 - 200 * mScrollY / layout_alarm.getLayoutParams().height; // what에는 현재 스크롤 좌표를 넘겨준다.
	//
	//				Logs.d(Logs.DEBUG, "scrollbar test : 현재 스크롤 위치 " + mScrollY + ", 퍼센트 " + percent);
	//
	//				// Background Alpha값 조정
	//				Drawable dr = DrawableHelper.getDrawable(MainActivity.this, R.color.ft_black);
	//				dr.setAlpha(percent);
	//				layout_alarmbottom.setBackground(dr);
	//				layout_black.setBackground(dr);
	//			}
	//		};
	//	}
}
