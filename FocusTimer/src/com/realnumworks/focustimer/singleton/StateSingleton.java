package com.realnumworks.focustimer.singleton;

import com.realnumworks.focustimer.thread.StateCheckThread;
import com.realnumworks.focustimer.thread.TimeThread;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.settings.Settings;

public class StateSingleton {
	// tstate : 현재 진행 모드 (메인화면 0, 집중시간 측정 중 1, 결과 화면 2, 기타 화면 3)
	public static final int TSTATE_MODE_MAIN = 0;
	public static final int TSTATE_MODE_FOCUSING = 1;
	public static final int TSTATE_MODE_LAPPINGRESULT = 2;
	public static final int TSTATE_MODE_RESULT = 3;
	public static final int TSTATE_MODE_OTHER = 4;

	// zstate : 기울어짐 상태
	public static final int ZSTATE_MODE_FACEUP = 0;
	public static final int ZSTATE_MODE_TURNING = 1;
	public static final int ZSTATE_MODE_FACEDOWN = 2;

	// pstate : 근접센서가 덮어진 상태
	public static final int PSTATE_MODE_AWAY = 0;
	public static final int PSTATE_MODE_CLOSE = 1;

	//달력의 월요일 시작 또는 일요일 시작(기본 일요일)
	public static final int STARTWITH_MONDAY = 2;
	public static final int STARTWITH_SUNDAY = 1;

	// theme_mode : 테마가 현재 list 모드인지, edit 모드인지
	public static final int THEME_MODE_LIST = 0;
	public static final int THEME_MODE_EDIT = 1;

	private int pstate = -1, zstate = -1, tstate = -1, dstate = STARTWITH_SUNDAY;
	private int theme_mode = THEME_MODE_LIST;
	private float zRate = 0;
	public String currentThemeId = null;

	public StateCheckThread stateCheckThread = new StateCheckThread();
	public TimeThread timerThread = new TimeThread();

	public int lastFocusedTimeInSec = 0;

	//	public static Settings currentSettings = null; // Main화면의 onStart에서 변경된다.

	private StateSingleton() {
	}

	private static StateSingleton instance = new StateSingleton();

	public static StateSingleton getInstance() {
		return instance;
	}

	public void setPstate(int pstate) {
		this.pstate = pstate;
	}

	public void setZstate(int zstate) {
		this.zstate = zstate;
	}

	public void setTstate(int tstate) {
		this.tstate = tstate;
	}

	public void setZRate(float zRate) {
		this.zRate = zRate;
	}

	public void setDstate(int dstate) {
		this.dstate = dstate;
	}

	public void setThemeMode(int theme_mode) {
		this.theme_mode = theme_mode;
	}

	public int getPstate() {
		return pstate;
	}

	public int getZstate() {
		return zstate;
	}

	public int getTstate() {
		return tstate;
	}

	public float getZRate() {
		return zRate;
	}

	public int getDstate() {
		return dstate;
	}

	public int getThemeMode() {
		return theme_mode;
	}

	public void logStates() {
		String tstring = null;

		switch (tstate) {
			case TSTATE_MODE_MAIN:
				tstring = "MAIN";
				break;
			case TSTATE_MODE_FOCUSING:
				tstring = "FOCUSING";
				break;
			case TSTATE_MODE_LAPPINGRESULT:
				tstring = "LAPPING";
				break;
			case TSTATE_MODE_RESULT:
				tstring = "RESULT";
				break;
			case TSTATE_MODE_OTHER:
				tstring = "OTHER";
				break;
			default:
				tstring = "null";
				break;
		}
		Logs.d(getClass().getSimpleName().toString(), "status log : (z)" + getZString() + " (p)" + getPString()
			+ " (t)" + tstring);
	}

	public String getPString() {
		String pstring = "";
		switch (pstate) {
			case PSTATE_MODE_AWAY:
				pstring = "AWAY";
				break;
			case PSTATE_MODE_CLOSE:
				pstring = "CLOSE";
				break;
			default:
				pstring = "null";
				break;
		}
		return pstring;
	}

	public String getZString() {
		String zstring = "";
		switch (zstate) {
			case ZSTATE_MODE_FACEUP:
				zstring = "FACEUP";
				break;
			case ZSTATE_MODE_TURNING:
				zstring = "TURNING";
				break;
			case ZSTATE_MODE_FACEDOWN:
				zstring = "FACEDOWN";
				break;
			default:
				zstring = "null";
				break;
		}
		return zstring;
	}

}
