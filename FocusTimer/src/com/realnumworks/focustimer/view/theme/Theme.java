package com.realnumworks.focustimer.view.theme;

import java.util.ArrayList;
import java.util.Calendar;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.singleton.DeviceControl;

public class Theme {
	public static final int MAX_THEME_COUNT = 7;
	private String id; // 생성 시간을 기준으로 결정되고, 바뀌지 않음
	private String name;
	private int color = 0;
	private int order; // 0부터
	private int lastFocusTime = 0; // 초단위
	private int totalFocusTime = 0; // 초단위
	private int numOfFocus = 0;

	public Theme() {
		setId();
		name = "제목 없음";
		color = 0;
		DataBaseHelper dbm = new DataBaseHelper(DeviceControl.getInstance()
				.getApplicationContext());
		ArrayList<Theme> themeList = dbm.getThemesListOfAll();
		order = themeList.size();
	}

	public Theme(String _name, int _color, int _order) {
		setId();
		name = _name;
		color = _color;
		order = _order;
	}

	public Theme(String _id, String _name, int _color, int _order) {
		id = _id;
		name = _name;
		color = _color;
		order = _order;
	}

	public Theme(String _id, String _name, int _color, int _order,
			int _lastFocusTime, int _totalFocusTime, int _numOfFocus) {
		id = _id;
		name = _name;
		color = _color;
		order = _order;
		lastFocusTime = _lastFocusTime;
		totalFocusTime = _totalFocusTime;
		numOfFocus = _numOfFocus;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getColor() {
		return color;
	}

	public int getOrder() {
		return order;
	}

	public int getLastFocusTime() {
		return lastFocusTime;
	}

	public int getTotalFocusTime() {
		return totalFocusTime;
	}

	public int getNumOfFocus() {
		return numOfFocus;
	}

	private void setId() {
		id = Calendar.getInstance().getTimeInMillis() + ""; // 테마 생성 시간을 id로
															// 잡는다.
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setLastFocusTime(int lastFocus) {
		this.lastFocusTime = lastFocus;
	}

	public void setTotalFocusTime(int totalFocustime) {
		this.totalFocusTime = totalFocustime;
	}

	public void setNumOfFocus(int numOfFocus) {
		this.numOfFocus = numOfFocus;
	}

	@Override
	public String toString() {
		String str = "";
		String colorStr = "";
		switch (color) {
		case 0:
			colorStr = "GREEN";
			break;
		case 1:
			colorStr = "YELLOW";
			break;
		case 2:
			colorStr = "ORANGE";
			break;
		case 3:
			colorStr = "RED";
			break;
		case 4:
			colorStr = "PURPLE";
			break;
		case 5:
			colorStr = "LIGHTBLUE";
			break;
		case 6:
			colorStr = "DARKBLUE";
			break;
		}
		int times[] = DateTime.getHourMinSecFromTimeSec(lastFocusTime);
		String timeStr = DateTime.getAMPMString(times[0], times[1]);
		str = "테마 [id]" + id + ", [이름]" + name + ", [색상]" + colorStr + ", [순서]"
				+ order + ", [마지막]" + timeStr + ", [총시간]"
				+ DateTime.getTimeStringFromSec(totalFocusTime)
				+ ", [총횟수]" + numOfFocus + ", [평균]" + getAvgFocusTime();
		return str;
	}

	public int getAvgFocusTime() {
		if (numOfFocus == 0)
			return 0;
		else
			return totalFocusTime / numOfFocus;
	}

	public int getRealColor() {
		int realColor = 0;
		switch (color) {
		case 0:
			realColor = DeviceControl.getInstance().getApplicationContext()
					.getResources().getColor(R.color.ft_lightgreen);
			break;
		case 1:
			realColor = DeviceControl.getInstance().getApplicationContext()
					.getResources().getColor(R.color.ft_yellow);
			break;
		case 2:
			realColor = DeviceControl.getInstance().getApplicationContext()
					.getResources().getColor(R.color.ft_orange);
			break;
		case 3:
			realColor = DeviceControl.getInstance().getApplicationContext()
					.getResources().getColor(R.color.ft_red);
			break;
		case 4:
			realColor = DeviceControl.getInstance().getApplicationContext()
					.getResources().getColor(R.color.ft_purple);
			break;
		case 5:
			realColor = DeviceControl.getInstance().getApplicationContext()
					.getResources().getColor(R.color.ft_lightblue);
			break;
		case 6:
			realColor = DeviceControl.getInstance().getApplicationContext()
					.getResources().getColor(R.color.ft_darkblue);
			break;
		}
		return realColor;
	}
}
