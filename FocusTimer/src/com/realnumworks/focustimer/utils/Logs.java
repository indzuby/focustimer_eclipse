package com.realnumworks.focustimer.utils;

import android.util.Log;

import com.realnumworks.focustimer.AppConfig;

public class Logs {

	public static final String DATETEST = "DateTimeTest";
	public static final String DUMMYTEST = "DummyDataTest";
	public static final String MAPTEST = "MapTest";
	public static final String BACKGROUND = "BackgroundTest";
	public static final String LIFELINE = "LifeLine";
	public static final String THEMETEST = "Theme Test";
	public static final String VIBRATETEST = "Vibration Test";
	public static final String SENSORTEST = "Sensor Test";
	public static final String THREADTEST = "스레드테스트";
	public static final String CONTINUOUS = "Continuous";
	public static final String TIMERTEST = "Timer Test";
	public static final String DEBUG = "Debug";
	public static final String FOCUSINGTEST = "측정시간테스트";
	public static final String SHUTDOWNTEST = "Shutdown Test";
	public static final String TUTORIAL = "Tutorial";
	public static final String TICKTEST = "Tick Test";
	public static final String SCREENONOFFTEST = "Screen Test";
	public static final String SCROLLTEST = "Scroll Test";
	public static final String ANALYTICS = "Analytics";
	public static final String WEEKTHTEST = "Weekth Test";
	public static final String SETTINGSTEST = "Settings Test";
	public static final String MAIN_THREAD_CONFIRM = "Main Thread Confirm";
	public static final String LAPCOUNT = "lapcount";
	public static final String SERVICE = "Service";
	public static final String HISTORY_EACH = "History_Each";
	public static final String HISTORY_DAY = "History_Day";
	public static final String HISTORY_WEEK = "History_Week";
	public static final String HISTORY_MONTH = "History_Month";

	//
	private static final String PUBLIC = "FocusTimer";

	public static void d(String message) {
		d(PUBLIC, message);
	}

	public static void i(String message) {
		i(PUBLIC, message);
	}

	public static void w(String message) {
		w(PUBLIC, message);
	}

	public static void e(String message) {
		e(PUBLIC, message);
	}

	public static void d(String tag, String message) {
		if (AppConfig.isDebug()) {
			Log.d(tag, message);
		}
	}

	public static void i(String tag, String message) {
		if (AppConfig.isDebug()) {
			Log.i(tag, message);
		}
	}

	public static void w(String tag, String message) {
		if (AppConfig.isDebug()) {
			Log.w(tag, message);
		}
	}

	public static void e(String tag, String message) {
		if (AppConfig.isDebug()) {
			Log.e(tag, message);
		}
	}
}