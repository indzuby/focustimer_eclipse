package com.realnumworks.focustimer.singleton;

import android.widget.LinearLayout;

/**
 * MainFragment에서 다른 액티비티로 넘어갈 때 맵 뷰를 저장하고, 다시 돌아올 때 맵 뷰를 받아온다. 호출되는 경우 : 1. 날짜가
 * 바뀔 때(현재 날짜(4시 이후) != 마지막 측정 날짜일 때) -> 전부 reDraw 2. Theme 페이지에서 테마 수정이 이루어졌을 때
 * 3. Timer 측정을 완료했을 때(해당 테마의, FocusTime Layout만)
 * 
 * TODO 설계를 다시 할 것. 각 Page별 Layout을 가져와야 함. 즉, themeId와 Layout을 동시에 가지고 있어야함.
 * 배열로 저장 필요.
 * 
 * @author Yedam
 * 
 */
public class CustomBundleSingleton {
	private CustomBundleSingleton() {
	}

	private static CustomBundleSingleton instance = new CustomBundleSingleton();

	public static CustomBundleSingleton getInstance() {
		return instance;
	}

	private LinearLayout layout_days, layout_dates, layout_focusTimes, layout_bigtimes;
	private boolean mapNeedsRedraw = true;

	public void saveMapDaysLayout(LinearLayout daysLayout) {
		layout_days = daysLayout;
	}

	public void saveMapDatesLayout(LinearLayout datesLayout) {
		layout_dates = datesLayout;
	}

	public void saveMapFocusTimesLayout(LinearLayout focusTimesLayout) {
		layout_focusTimes = focusTimesLayout;
	}

	public void saveBigTimesLayout(LinearLayout bigTimesLayout) {
		layout_bigtimes = bigTimesLayout;
	}

	public LinearLayout getMapDaysLayout() {
		return layout_days;
	}

	public LinearLayout getMapDatesLayout() {
		return layout_dates;
	}

	public LinearLayout getMapFocusTimesLayout() {
		return layout_focusTimes;
	}

	public LinearLayout getBigTimesLayout() {
		return layout_bigtimes;
	}

	public boolean isMapNeedsRedraw() {
		return mapNeedsRedraw;
	}

	public void setRedraw(boolean reDraw) {
		mapNeedsRedraw = reDraw;
	}

	public void setRedrawThemeId(String themeId) {
	}

	// 모두 초기화
	public void unsetRedraw() {
		mapNeedsRedraw = false;
	}
}
