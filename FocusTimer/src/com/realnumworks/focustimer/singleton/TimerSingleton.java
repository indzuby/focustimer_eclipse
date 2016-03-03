package com.realnumworks.focustimer.singleton;

import java.util.Calendar;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.history.Record;

/**
 * 현재 날짜, 시간에서 시작 날짜, 시간을 빼서 스톱워치를 만드는 싱글톤 클래스
 * 
 * @author Yedam
 * 
 */

public class TimerSingleton {

	private static TimerSingleton appInstance;

	public static final int MAX_LAPCOUNT = 2;

	// 측정 시작 시간 = 메인화면에서 기기를 뒤집었을 때의 현재시간
	// 측정 종료 시간 = 측정화면에서 기기를 바로 했을 때의 현재시간(단, 랩카운트가 남아 있을 때)
	// 랩카운트 = 측정화면에서 기기를 바로 했을 때 하나씩 추가, 최대 3
	// 15카운트 = 1초마다의 현재시간 빼기 랩타임(1초 스레드 돌려야 함, 15카운트가 0이 되면 스레드 해제)
	// 측정 종료 시간 > 15카운트가 0이 되어 종료되는 경우에는 랩타임이 그대로 측정 종료 시간이 되고, 랩카운트를 더 늘리지 못해
	// 종료하는 경우 역시 기기를 바로 했을 때의 시간이 종료 시간이 됨

	private static long startTime; // ms단위
	private static long finishTime;
	private static int lapCount;
	private static long startLappingTime;
	private static int totalLappingTime;
	private static boolean isLapping; // 현재 15초 카운트가 돌아가는 중인가?
	private int focusTime; // 집중 시간(초단위)

	private TypefaceTextView tv_min, tv_sec, tv_lapcount;
	LinearLayout layout_timer_main, layout_timer_refocus, layout_timer_focusingcircle;

	public static TimerSingleton getInstance() {
		synchronized (TimerSingleton.class) {
			if (appInstance == null) {
				appInstance = new TimerSingleton();
				init();
			}
		}
		return appInstance;
	}

	public static void init() {
		lapCount = 3;
		isLapping = false;
		startTime = finishTime = 0;
		startLappingTime = 0;
		totalLappingTime = 0;
	}

	public int getFocusTimeInSec() {
		return focusTime;
	}

	public long getStartTime() {
		return startTime;
	}
	
	public long getLappingTime() {
		return startLappingTime;
	}

	public void setLappingTime(long lappingTime) {
		this.startLappingTime = lappingTime;
	}
	public void addLappingTime() {
		totalLappingTime += getSecond(Calendar.getInstance().getTimeInMillis())
				- startLappingTime;
		startLappingTime = 0;
	}
	public int getTotalLappingTime() {
		return totalLappingTime;
	}
	/**
	 * 랩카운트 값을 하나 올려준다.
	 */
	public void plusLapCount() {
		lapCount--;
		Logs.d(Logs.LAPCOUNT, "lapcount added (lapcount : " + lapCount + ")");
	}

	/**
	 * 이어 집중 횟수 텍스트뷰 갱신
	 */
	public void updateLapCountTextView() {
		Logs.d(Logs.LAPCOUNT, "updated textview (lapcount : " + lapCount + ")");
		if (lapCount > 0 ) {
			tv_lapcount.setText(Html.fromHtml("<u>" + "이어 집중하기 가능(" + lapCount + "/" + (MAX_LAPCOUNT) + ")" + "</u>"));
		} else {
			tv_lapcount.setText(Html.fromHtml("이어 집중하기가 가능하지 않습니다."));
		}
	}

	public int getLapCount() {
		return lapCount;
	}

	public boolean getIsLapping() {
		return isLapping;
	}

	//
	/**
	 * 결과화면의 TextViews를 Timer에 세팅
	 * 
	 * @param tv_hour
	 * @param tv_min
	 * @param tv_sec
	 * @param tv_fifteen
	 * @param tv_lapcount
	 */
	public void setTextViews(TypefaceTextView tv_min, TypefaceTextView tv_sec, TypefaceTextView tv_fifteen,
			TypefaceTextView tv_lapcount, TypefaceTextView tv_3sec) {
		this.tv_min = tv_min;
		this.tv_sec = tv_sec;
		this.tv_lapcount = tv_lapcount;
	}

	/**
	 * 집중 시작 시간을 측정하는 함수
	 */
	public void setStartTime() {
		startTime = getSecond(Calendar.getInstance().getTimeInMillis());
		Record startTimeRecord = new Record((int)startTime);
		Log.d("setStartTime()", startTimeRecord.toString());
	}

	/**
	 * 현재시간을 측정 종료 시간으로 세팅하는 함수
	 */
	public void setFinishTime() {
		finishTime = getSecond(Calendar.getInstance().getTimeInMillis()); // 종료시간을 현재 시간으로 정하고
	}

	/**
	 * 15초 타이머 시작하는 함수
	 */
	public void startFifteenTimer() {
		isLapping = true;
		startLappingTime = getSecond(Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * 15초 타이머 종료하는 함수
	 */
	public void finishFifteenTimer() {
		isLapping = false;
		addLappingTime();
		// refreshTextViewsTexts();
	}

	/**
	 * 집중 시간 텍스트뷰의 텍스트들을 시, 분, 초별로 나누어 갱신해주는 함수.
	 */
	public void refreshTextViewsTexts() {
		focusTime = (int)(finishTime - startTime) - totalLappingTime; // 집중 시간(현재 초단위)
		int focusTimeSec = focusTime;
		int focusTimeMin = 0;

		if (focusTimeSec > 60) // 60초를 넘어갈 경우, 분단위로 빼 준다
		{
			focusTimeMin = focusTimeSec / 60;
			focusTimeSec = focusTimeSec % 60;
		}

		tv_min.setText(focusTimeMin + "");
		tv_sec.setText(focusTimeSec + "");
		if (StateSingleton.getInstance().getTstate() == StateSingleton.TSTATE_MODE_RESULT) {
			tv_lapcount.setText("이어 집중하기가 가능하지 않습니다.");
			layout_timer_refocus.setVisibility(View.GONE);
		}
	}

	/**
	 * millisecond단위를 초단위로 바꿔주는 함수
	 * 
	 * @param millis
	 * @return
	 */
	public long getSecond(long millis) {
		return millis / 1000;
	}

	/**
	 * 맨 처음 집중 시작할 때, 첫 집중 시작 시간을 기록하는 함수
	 */
	public void startFocusing() {
		TimerSingleton.init();// 모든 시간, 변수 초기화
		TimerSingleton.getInstance().setStartTime(); // 뒤집히자마자 시간을 측정하고 그걸 시작시간으로 잡음
	}

	public void setTimerMainLayout(LinearLayout layout) {
		layout_timer_main = layout;
	}

	public void setRefocusLayout(LinearLayout layout) {
		layout_timer_refocus = layout;
	}

	public void setFocusingCircleLayout(LinearLayout layout) {
		layout_timer_focusingcircle = layout;
	}

	/**
	 * 최초 시작 시 Focusing 글자 출력하는 TextView
	 */
	// public void setNowFocusingTextView(TypefaceTextView tv)
	// {
	// tv_nowfocusing = tv;
	// }

	public static long getFinishTime() {
		return finishTime;
	}
}
