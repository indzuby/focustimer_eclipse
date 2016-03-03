package com.realnumworks.focustimer.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.realnumworks.focustimer.data.PreferenceEnums.StartDay;
import com.realnumworks.focustimer.data.PreferenceEnums.StartTimeAlarm1;
import com.realnumworks.focustimer.data.PreferenceEnums.StartTimeAlarm2;
import com.realnumworks.focustimer.data.PreferenceEnums.StartVibrate;

public class PreferenceUtil {

	private static final String PREF_FOCUSTIMER = "PREF_FOCUSTIMER";
	//
	private static final String PREF_START_DAY = "PREF_START_DAY";
	private static final String PREF_START_VIBRATE = "PREF_START_VIBRATE";
	private static final String PREF_START_TIME_ALARM_1 = "PREF_START_TIME_ALARM_1";
	private static final String PREF_START_TIME_ALARM_2 = "PREF_START_TIME_ALARM_2";

	private static Context context;

	public static void setContext(Context context) {
		PreferenceUtil.context = context;
	}

	public static void setStartDay(StartDay startDay) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
			PREF_FOCUSTIMER, Activity.MODE_PRIVATE).edit();
		prefs.putInt(PREF_START_DAY, startDay.ordinal());
		prefs.commit();
	}

	public static StartDay getStartDay() {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FOCUSTIMER,
			Activity.MODE_PRIVATE);
		return StartDay.fromValue(prefs.getInt(PREF_START_DAY,
			Integer.MAX_VALUE));
	}

	public static void setStartVibrate(StartVibrate startVibrate) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
			PREF_FOCUSTIMER, Activity.MODE_PRIVATE).edit();
		prefs.putInt(PREF_START_VIBRATE, startVibrate.ordinal());
		prefs.commit();
	}

	public static int getStartVibrate() {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FOCUSTIMER,
			Activity.MODE_PRIVATE);
		return prefs.getInt(PREF_START_VIBRATE, Integer.MAX_VALUE);
	}

	public static void setStartTimeAlarm1(StartTimeAlarm1 startTimeAlarm1) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
			PREF_FOCUSTIMER, Activity.MODE_PRIVATE).edit();
		prefs.putInt(PREF_START_TIME_ALARM_1, startTimeAlarm1.ordinal());
		prefs.commit();
	}

	public static int getStartTimeAlarm1() {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FOCUSTIMER,
			Activity.MODE_PRIVATE);
		return prefs.getInt(PREF_START_TIME_ALARM_1, Integer.MAX_VALUE);
	}

	public static void setStartTimeAlarm2(StartTimeAlarm2 startTimeAlarm1) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
			PREF_FOCUSTIMER, Activity.MODE_PRIVATE).edit();
		prefs.putInt(PREF_START_TIME_ALARM_2, startTimeAlarm1.ordinal());
		prefs.commit();
	}

	public static int getStartTimeAlarm2() {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FOCUSTIMER,
			Activity.MODE_PRIVATE);
		return prefs.getInt(PREF_START_TIME_ALARM_2, Integer.MAX_VALUE);
	}
}
