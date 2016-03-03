package com.realnumworks.focustimer.view.settings;

import com.realnumworks.focustimer.utils.Logs;

public class Settings {
	private int startWith			= 0; // 일시작=1, 월시작=2
	private int alarm01				= 0; // 분단위
	private int alarm02				= 0; // 분단위
	public int alarmSelected 		= 0;
	private int isTutorial01Shown	= 0;
	private int isTutorial02Shown	= 0;
	private int isRedAlertShown		= 0;
	private int isVibrateOn			= 0;

	public Settings() {
	}
	
	public void set(int _startWith, int _alarm01, int _alarm02,
			int _alarmSelected, int _isTutorial01Shown, int _isTutorial02Shown,
			int _isRedAlertShown, int _isVibrateOn) {
		startWith = _startWith;
		alarm01 = _alarm01;
		alarm02 = _alarm02;
		alarmSelected = _alarmSelected;
		isTutorial01Shown = _isTutorial01Shown;
		isTutorial02Shown = _isTutorial02Shown;
		isRedAlertShown = _isRedAlertShown;
		isVibrateOn = _isVibrateOn;
	}

	public void setStartWith(int startWith) {
		this.startWith = startWith;
	}

	public void setAlarm01(int alarm01) {
		this.alarm01 = alarm01;
	}

	public void setAlarm02(int alarm02) {
		this.alarm02 = alarm02;
	}

	public void setAlarmSelected(int alarmSelected) {
		this.alarmSelected = alarmSelected;
	}

	public void setTutorial01Shown(boolean b) {
		isTutorial01Shown = (b == true) ? 0 : 1;
	}

	public void setTutorial02Shown(boolean b) {
		isTutorial02Shown = (b == true) ? 0 : 1;
	}

	public void setRedAlertShown(boolean b) {
		isRedAlertShown = (b == true) ? 0 : 1;
	}

	public void setVibrateOn(boolean b) {
		isVibrateOn = (b == true) ? 0 : 1;
	}

	public int getStartWith() {
		return startWith;
	}

	public int getAlarm01() {
		return alarm01;
	}

	public int getAlarm02() {
		return alarm02;
	}

	/** 선택된 알람의 인덱스 리턴 **/
	public int getAlarmSelected() {
		return alarmSelected;
	}

	public boolean isTutorial01Shown() {
		return (isTutorial01Shown == 0) ? true : false;
	}

	public boolean isTutorial02Shown() {
		return (isTutorial02Shown == 0) ? true : false;
	}

	public boolean isRedAlertShown() {
		return (isRedAlertShown == 0) ? true : false;
	}

	public boolean isVibrateOn() {
		Logs.d(Logs.VIBRATETEST, "진동 "+((isVibrateOn == 0)?"On":"Off"));
		return (isVibrateOn == 0) ? true : false;
	}

	public int getTutorial01Shown() {
		return isTutorial01Shown;
	}

	public int getTutorial02Shown() {
		return isTutorial02Shown;
	}

	public int getRedAlertShown() {
		return isRedAlertShown;
	}

	public int getIsVibrateOn() {
		return isVibrateOn;
	}

	/** 선택된 알람 값 리턴 **/
	public int getAlarmValueSelected() {
		switch (alarmSelected) {
		case 0:
			return 0;
		case 1:
			return alarm01;
		case 2:
			return alarm02;
		default:
			break;
		}
		return 0;
	}

	@Override
	public String toString() {
		String str = "";
		if (startWith == 2) {
			str += "월시작";
		} else if (startWith == 1) {
			str += "일시작";
		}
		str += ", 알람 : " + alarm01 + "/" + alarm02 + "분, 선택 : " + alarmSelected
				+ ", 튜토리얼 : " + isTutorial01Shown() + ", "
				+ isTutorial02Shown() + ", " + isRedAlertShown() + ", 진동 "
				+ ((isVibrateOn == 0) ? "On" : "Off");
		return str;
	}
}