package com.realnumworks.focustimer.view.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.data.TimeRange;
import com.realnumworks.focustimer.data.TimeRangeManager;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.chart.ChartActivity;
import com.realnumworks.focustimer.view.history.HistoryActivity;
import com.realnumworks.focustimer.view.history.Record;
import com.realnumworks.focustimer.view.theme.Theme;

/**
 * 메인 화면에서 Swipe되는 파란 배경 부분.
 * 
 * @author Yedam
 * 
 */
public final class MainFragment extends Fragment implements View.OnClickListener {

	private static final String KEY_CONTENT = "MainFragment:Content";
	private static final int MAP_SIZE = 7;

	View mainView;
	ImageButton btn_record, btn_chart;
	TypefaceTextView tv_theme, tv_day, tv_numHour, tv_numMin, tv_thisWeek, tv_weekFocusTime, tv_hour, tv_min;
	TypefaceTextView maptv_days[];
	TypefaceTextView maptv_dates[];
	TypefaceTextView maptv_focustimes[];
	LinearLayout layout_days, layout_dates, layout_focusTimes;
	int sumOfFocusTimes = 0; // 일주일의 focustime을 총합한 것(sec단위)
	Record todayRecord;
	int todayIndex = -1;
	Theme currentTheme;
	DataBaseHelper dbm;
	ArrayList<Record> mapRecordsByTheme[]; // 각 날짜마다의 Record
	ArrayList<Record> allRecordsInThisWeek; // 이번 주의 모든 Record
	TimeRange thisWeekSecondRange;
	boolean isRedraw; // 맵을 다시 그릴지, 기존걸 가져올지?

	public static MainFragment newInstance(String content) {
		MainFragment fragment = new MainFragment();
		fragment.mContent = content;

		return fragment;
	}

	private String mContent = "null"; // mContent = Theme Name

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Logs.d("startTest", "MainFragment.onCreate");

		// Set Current Theme
		dbm = new DataBaseHelper(getActivity());
		Logs.d(Logs.BACKGROUND, "mC = " + mContent);
		currentTheme = dbm.getThemeById(mContent);
		Logs.d("startTest", "현재 테마 : " + currentTheme.toString());
		setTodayRecord();
		setTodayIndex();
		bringMapRecordsFromDB();

		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getString(KEY_CONTENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logs.d("startTest", "MainFragment.onCreateView");

		mainView = inflater.inflate(R.layout.fragment_main, container, false);
		setComponents(mainView);
		setMapComponents(mainView);
		setButtonOnClickListener(this);

		drawMap();
		setMapComponentsToGrayColor();
		setTextViewTextsTheme();
		setTextViewTextsToday();
		setTextViewTextsThisWeek();

		dbm.close();

		return mainView;
	}

	public void setTodayRecord() {
		todayRecord = new Record((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		if (todayRecord.getHour() < 4) // 오전 4시 이전이면
		{
			todayRecord = Record.getPrevDate(todayRecord); // 전날로 편입
		}
	}

	public void setTodayIndex() {
		int dayOfToday = todayRecord.getDay(); // 오늘 요일
		// MON~SUN 추가
		int option = StateSingleton.getInstance().getDstate();
		switch (option) {
		case StateSingleton.STARTWITH_SUNDAY:
			todayIndex = dayOfToday - option;
			break;
		case StateSingleton.STARTWITH_MONDAY:
			if (dayOfToday == Calendar.SUNDAY) {
				todayIndex = MAP_SIZE - 1;
			} else {
				todayIndex = dayOfToday - option;
			}
			break;
		}
	}

	/**
	 * 버튼, 텍스트뷰 등 레이아웃의 findviewbyid 함수 집합
	 * 
	 * @param mainView
	 */
	public void setComponents(View mainView) {
		btn_record = (ImageButton) mainView.findViewById(R.id.btn_record);
		btn_chart = (ImageButton) mainView.findViewById(R.id.btn_chart);
		tv_theme = (TypefaceTextView) mainView.findViewById(R.id.tv_theme);
		tv_day = (TypefaceTextView) mainView.findViewById(R.id.tv_day);
		tv_numHour = (TypefaceTextView) mainView.findViewById(R.id.tv_numhour);
		tv_numMin = (TypefaceTextView) mainView.findViewById(R.id.tv_numMin);
		tv_hour = (TypefaceTextView) mainView.findViewById(R.id.tv_hour);
		tv_min = (TypefaceTextView) mainView.findViewById(R.id.tv_min);
		tv_thisWeek = (TypefaceTextView) mainView.findViewById(R.id.tv_thisweek);
		tv_weekFocusTime = (TypefaceTextView) mainView.findViewById(R.id.tv_weekfocustime);
		tv_thisWeek = (TypefaceTextView) mainView.findViewById(R.id.tv_thisweek); // This Week
	}

	public void setMapComponents(View mainView) {
		maptv_days = new TypefaceTextView[MAP_SIZE];
		maptv_days[0] = (TypefaceTextView) mainView.findViewById(R.id.tv_day0);
		maptv_days[1] = (TypefaceTextView) mainView.findViewById(R.id.tv_day1);
		maptv_days[2] = (TypefaceTextView) mainView.findViewById(R.id.tv_day2);
		maptv_days[3] = (TypefaceTextView) mainView.findViewById(R.id.tv_day3);
		maptv_days[4] = (TypefaceTextView) mainView.findViewById(R.id.tv_day4);
		maptv_days[5] = (TypefaceTextView) mainView.findViewById(R.id.tv_day5);
		maptv_days[6] = (TypefaceTextView) mainView.findViewById(R.id.tv_day6);

		maptv_dates = new TypefaceTextView[MAP_SIZE];
		maptv_dates[0] = (TypefaceTextView) mainView.findViewById(R.id.tv_date0);
		maptv_dates[1] = (TypefaceTextView) mainView.findViewById(R.id.tv_date1);
		maptv_dates[2] = (TypefaceTextView) mainView.findViewById(R.id.tv_date2);
		maptv_dates[3] = (TypefaceTextView) mainView.findViewById(R.id.tv_date3);
		maptv_dates[4] = (TypefaceTextView) mainView.findViewById(R.id.tv_date4);
		maptv_dates[5] = (TypefaceTextView) mainView.findViewById(R.id.tv_date5);
		maptv_dates[6] = (TypefaceTextView) mainView.findViewById(R.id.tv_date6);

		maptv_focustimes = new TypefaceTextView[MAP_SIZE];
		maptv_focustimes[0] = (TypefaceTextView) mainView.findViewById(R.id.tv_ft0);
		maptv_focustimes[1] = (TypefaceTextView) mainView.findViewById(R.id.tv_ft1);
		maptv_focustimes[2] = (TypefaceTextView) mainView.findViewById(R.id.tv_ft2);
		maptv_focustimes[3] = (TypefaceTextView) mainView.findViewById(R.id.tv_ft3);
		maptv_focustimes[4] = (TypefaceTextView) mainView.findViewById(R.id.tv_ft4);
		maptv_focustimes[5] = (TypefaceTextView) mainView.findViewById(R.id.tv_ft5);
		maptv_focustimes[6] = (TypefaceTextView) mainView.findViewById(R.id.tv_ft6);
	}

	public void setMapComponentsToGrayColor() {
		for (int numOfDay = todayIndex + 1; numOfDay < MAP_SIZE; numOfDay++) {
			int grayColor = getResources().getColor(R.color.ft_mapGray);
			maptv_days[numOfDay].setTextColor(grayColor);
			maptv_dates[numOfDay].setTextColor(grayColor);
			maptv_focustimes[numOfDay].setTextColor(grayColor);
		}
	}

	/** Set the Listeners of MainFragment Buttons(Record, Chart) **/
	public void setButtonOnClickListener(OnClickListener listener) {
		btn_record.setOnClickListener(listener);
		btn_chart.setOnClickListener(listener);
	}

	public void bringMapRecordsFromDB() {
		DataBaseHelper dbHelper = new DataBaseHelper(getActivity().getApplicationContext());
		mapRecordsByTheme = new ArrayList[MAP_SIZE];
		allRecordsInThisWeek = new ArrayList<Record>();
		// 그 주의 Second Range를 가져온다.
		Logs.d(Logs.DATETEST, "todayRecord : " + todayRecord.toString());
		thisWeekSecondRange = TimeRangeManager.getTimeRangeOfWeekByRecord(todayRecord);
		// 그 주의 Second Range에 해당하는 DB 전부를 가져온다.
		allRecordsInThisWeek = dbHelper.getRecordsInSecondRangeByTheme(thisWeekSecondRange.clone(), currentTheme.getId());
		ArrayList<TimeRange> daySecondRanges = TimeRangeManager.getSpiltedSecondRangeByDays(thisWeekSecondRange, MAP_SIZE);
		if (allRecordsInThisWeek != null) {
			Collections.sort(allRecordsInThisWeek, new StartTimeAscSort()); // 시작 시간 순서대로 Sort한다.

			// 전체 DB를 가져온 ArrayList를 각각의 날짜별 ArrayList로 나누어 mapRecords에 담는다.
			// 즉 MapRecord[0]은 0번째 날의 모든 Record들을 의미함
			for (int numsOfDay = 0; numsOfDay < MAP_SIZE; numsOfDay++) {
				TimeRange thisDaySecondRange = daySecondRanges.get(numsOfDay);
				ArrayList<Record> thisDayAllRecords = DateTime.getRecordsInTimeRange(thisDaySecondRange, allRecordsInThisWeek);
				mapRecordsByTheme[numsOfDay] = thisDayAllRecords;
			}
		} else {

			for (int numsOfDay = 0; numsOfDay < MAP_SIZE; numsOfDay++) {
				mapRecordsByTheme[numsOfDay] = null;
			}
		}
		printMapArray();
	}

	public void printMapArray() {
		Logs.d(Logs.MAPTEST, "Print the Map");
		for (int column = 0; column < MAP_SIZE; column++) {
			int thisDayFocusTimes = DateTime.getSumOfFocusTimesInSelectedDay(mapRecordsByTheme[column]);
			Logs.d(Logs.MAPTEST, (column + 1) + "일째의 집중시간 = " + thisDayFocusTimes + "초");
		}
	}

	public void drawMap() {
		ArrayList<String> dayArrayList = getMapDayStringArray();
		ArrayList<String> dateArrayList = getMapDateStringArray(thisWeekSecondRange);
		ArrayList<String> focusTimeArrayList = getFocusTimeStringArray();
		for (int mapColumnIndex = 0; mapColumnIndex < MAP_SIZE; mapColumnIndex++) {
			maptv_days[mapColumnIndex].setText(dayArrayList.get(mapColumnIndex));
			maptv_dates[mapColumnIndex].setText(dateArrayList.get(mapColumnIndex));
			maptv_focustimes[mapColumnIndex].setText(focusTimeArrayList.get(mapColumnIndex));
		}
	}

	public ArrayList<String> getFocusTimeStringArray() {
		ArrayList<String> focusTimeStringArrayList = new ArrayList<String>();
		// 각 날짜별로 mapRecords에서 FocusTime들을 받아와서 합치고,
		int sumOfFocusTimeOfOneDay = 0;
		for (int numOfDay = 0; numOfDay < MAP_SIZE; numOfDay++) {
			if (mapRecordsByTheme[numOfDay] == null)
				focusTimeStringArrayList.add(numOfDay, "-");
			else {
				for (int recordIndex = 0; recordIndex < mapRecordsByTheme[numOfDay].size(); recordIndex++) {
					sumOfFocusTimeOfOneDay += mapRecordsByTheme[numOfDay].get(recordIndex).getFocustime();
				}
				focusTimeStringArrayList.add(numOfDay, Record.getFocusTimeInString(sumOfFocusTimeOfOneDay));
				sumOfFocusTimeOfOneDay = 0;
			}
		}
		// 그것들을 String으로 표현
		return focusTimeStringArrayList;
	}

	public String getSumOfFocusTimeInString() {
		String sumOfFocusTimeInString = "-";

		return sumOfFocusTimeInString;
	}

	class DrawMapBundle {
		int numOfDay;
		Typeface tf;
		int color;
		LayoutParams position;

		public DrawMapBundle(int numOfDay, Typeface tf, int color, LayoutParams position) {
			this.numOfDay = numOfDay;
			this.tf = tf;
			this.color = color;
			this.position = position;
		}
	}

	public TypefaceTextView getDayMapLine(int numOfDay, Typeface tf, int color, LayoutParams position) {
		// 요일
		TypefaceTextView tv_mapdays = getMapTextView(mainView, getMapDayStringArray().get(numOfDay));
		tv_mapdays.setTypeface(tf);
		tv_mapdays.setTextColor(color);
		return tv_mapdays;
	}

	public TypefaceTextView getDateMapLine(int numOfDay, Typeface tf, int color, LayoutParams position) {
		// 날짜
		TypefaceTextView tv_mapdates = getMapTextView(mainView, getMapDateStringArray(thisWeekSecondRange).get(numOfDay));
		tv_mapdates.setTypeface(tf);
		tv_mapdates.setTextColor(color);
		return tv_mapdates;
	}

	public LinearLayout getFocusTimeMapLine(int numOfDay, int color, LayoutParams position) {
		// 집중시간
		LinearLayout layout_maphourmin = getMapHourMinLayout(mapRecordsByTheme[numOfDay], color);
		return layout_maphourmin;
	}

	/**
	 * text라는 텍스트를 가진 Map TextView 하나를 리턴
	 * 
	 * @param parentView
	 * @param text
	 * @return
	 */
	public TypefaceTextView getMapTextView(View parentView, String text) {
		TypefaceTextView maptv = new TypefaceTextView(parentView.getContext());
		maptv.setText(text);
		maptv.setGravity(Gravity.CENTER);
		return maptv;
	}

	/**
	 * 이번 주의 Second Range를 매개변수로 받아, 요일을 의미하는 String형 ArrayList로 리턴한다.
	 * 
	 * @return 요일(월, 일)이 들어있는 ArrayList
	 */
	public ArrayList<String> getMapDayStringArray() {
		ArrayList<String> mapDayStringArray = new ArrayList<String>();

		// MON~SUN 추가
		int option = StateSingleton.getInstance().getDstate();
		if (option == StateSingleton.STARTWITH_SUNDAY) {
			mapDayStringArray.addAll(Arrays.asList(getResources().getStringArray(R.array.days_startwithSunday)));
		} else if (option == StateSingleton.STARTWITH_MONDAY) {
			mapDayStringArray.addAll(Arrays.asList(getResources().getStringArray(R.array.days_startwithMonday)));
		}
		return mapDayStringArray;
	}

	/**
	 * 이번 주의 Second Range를 매개변수로 받아, 날짜를 의미하는 String형 ArrayList로 리턴한다.
	 * 
	 * @return MONTH/DATE
	 */
	public ArrayList<String> getMapDateStringArray(TimeRange thisWeekSecondRange) {
		ArrayList<String> mapDateStringArray = new ArrayList<String>();

		Record startDateOfThisWeekRecord = new Record(thisWeekSecondRange.getStartTimePoint());

		for (int numOfDays = 0; numOfDays < MAP_SIZE; numOfDays++) // 7일 뺑이 돌리면서 적음
		{
			mapDateStringArray.add(startDateOfThisWeekRecord.getDateString("/", ""));
			startDateOfThisWeekRecord = Record.getNextDate(startDateOfThisWeekRecord);
		}
		return mapDateStringArray;
	}

	/**
	 * 해당 날짜의 모든 Records와 날짜를 표시할 Color를 받아, 그것을 표 한 칸에 들어갈 해당 날짜의 도합 집중시간으로
	 * 바꿔준다.
	 * 
	 * @param recordsOfSelectedDay
	 * @param color
	 * @return
	 */
	public LinearLayout getMapHourMinLayout(ArrayList<Record> recordsOfSelectedDay, int color) {
		// Initial setting
		LinearLayout llayout = new LinearLayout(getActivity());
		llayout.setOrientation(LinearLayout.HORIZONTAL);
		llayout.setGravity(Gravity.BOTTOM);

		int sumOfFocusTimesInTimeInSec = 0; // 선택된 날짜의 모든 Record의 focustime을 총합하여 TimeInSec로 나타낸 변수

		// null이면 "-" 리턴
		if (recordsOfSelectedDay == null) {
			TypefaceTextView tv_noRecord = new TypefaceTextView(getActivity());
			tv_noRecord.setTextSize(16);
			tv_noRecord.setText("-");
			tv_noRecord.setTextColor(color);
			llayout.addView(tv_noRecord);
			return llayout;
		}

		// 선택된 날짜의 focustime을 모두 더한다.
		sumOfFocusTimesInTimeInSec = DateTime.getSumOfFocusTimesInSelectedDay(recordsOfSelectedDay);

		int times[] = DateTime.getHourMinSecFromTimeSec(sumOfFocusTimesInTimeInSec);
		int hour = times[0], min = times[1], sec = times[2];
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/NanumBarunGothic.ttf");

		if (!(hour == 0 && min == 0 && sec == 0)) // 기록이 있을 때
		{
			if (hour >= 10)// h가 10 이상일 때, 10.5h로 표시
			{
				float alpha = ((float) min / 6) * 10;
				int a = (int) alpha / 10;
				TypefaceTextView tv_numHour = getMapTextViewOfFocusTimesPanels(14, color, tf, hour + "." + a);
				TypefaceTextView tv_hour = getMapTextViewOfFocusTimesPanels(10, color, tf, "h");
				llayout.addView(tv_numHour);
				llayout.addView(tv_hour);
			} else if (0 < hour && hour < 10)// h가 0과 10 사이일때, 9h 39m으로 표시
			{
				TypefaceTextView tv_numHour = getMapTextViewOfFocusTimesPanels(14, color, tf, hour + "");
				TypefaceTextView tv_hour = getMapTextViewOfFocusTimesPanels(10, color, tf, "h");
				TypefaceTextView tv_numMin = getMapTextViewOfFocusTimesPanels(14, color, tf, min + "");
				TypefaceTextView tv_min = getMapTextViewOfFocusTimesPanels(10, color, tf, "m");
				llayout.addView(tv_numHour);
				llayout.addView(tv_hour);
				llayout.addView(tv_numMin);
				llayout.addView(tv_min);

			} else if (hour == 0 && 0 < min)// 0분과 1시간 사이일 때, 8m으로 표시하고 초 단위는 버림
			{
				TypefaceTextView tv_numMin = getMapTextViewOfFocusTimesPanels(14, color, tf, min + "");
				TypefaceTextView tv_min = getMapTextViewOfFocusTimesPanels(10, color, tf, "m");
				llayout.addView(tv_numMin);
				llayout.addView(tv_min);
			} else // 초단위. 8s로 표시
			{
				TypefaceTextView tv_numSec = getMapTextViewOfFocusTimesPanels(14, color, tf, sec + "");
				TypefaceTextView tv_sec = getMapTextViewOfFocusTimesPanels(10, color, tf, "s");
				llayout.addView(tv_numSec);
				llayout.addView(tv_sec);
			}
		} else // 기록이 없을 때
		{
			TypefaceTextView tv_noRecord = getMapTextViewOfFocusTimesPanels(14, color, tf, "-");
			llayout.addView(tv_noRecord);
		}
		llayout.setGravity(Gravity.CENTER_HORIZONTAL);
		return llayout;
	}

	public TypefaceTextView getMapTextViewOfFocusTimesPanels(int textSize, int textColor, Typeface tf, String text) {
		TypefaceTextView tv = new TypefaceTextView(getActivity());
		tv.setTextSize(textSize);
		tv.setTextColor(textColor);
		tv.setTypeface(tf);
		tv.setText(text);
		return tv;
	}

	@Override
	public void onClick(View v) {

		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.btn_record:
			intent = new Intent(getActivity(), HistoryActivity.class);
			break;
		case R.id.btn_chart:
			intent = new Intent(getActivity(), ChartActivity.class);
			break;
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra("themeId", mContent);
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_OTHER);
		startActivity(intent);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Logs.d(Logs.LIFELINE, "MainFragment.onSaveInstanceState");
		outState.putString(KEY_CONTENT, mContent);
	}

	@Override
	public void onResume() {
		super.onResume();
		Logs.d(Logs.LIFELINE, "MainFragment.onResume");
	}

	public void setTextViewTextsTheme() {
		// Theme Name
		tv_theme.setText(currentTheme.getName());
		tv_theme.setTextColor(currentTheme.getRealColor());
	}

	public void setTextViewTextsToday() {

		ArrayList<Record> todayRecords;
		if (mapRecordsByTheme == null)
			todayRecords = null;
		else
			todayRecords = mapRecordsByTheme[todayIndex];

		int focusdatesOfToday, hour, min, sec;
		if (todayRecords == null) {
			focusdatesOfToday = hour = min = sec = 0;
		} else {
			focusdatesOfToday = DateTime.getSumOfFocusTimesInSelectedDay(todayRecords);
			int times[] = DateTime.getHourMinSecFromTimeSec(focusdatesOfToday);
			hour = times[0];
			min = times[1];
			sec = times[2];
		}

		if (hour == 0) {
			tv_numHour.setText(min + "");
			tv_hour.setText("분");
			tv_numMin.setText(sec + "");
			tv_min.setText("초");
		} else {
			tv_numHour.setText(hour + "");
			tv_hour.setText("시간");
			tv_numMin.setText(min + "");
			tv_min.setText("분");
		}

	}

	public void setTextViewTextsThisWeek() {
		int wkhour = 0, wkmin = 0, wksec = 0;
		if (allRecordsInThisWeek != null) {
			int sumOfFocusTimes = DateTime.getSumOfFocusTimesInSelectedDay(allRecordsInThisWeek);
			int times[] = DateTime.getHourMinSecFromTimeSec(sumOfFocusTimes);
			wkhour = times[0];
			wkmin = times[1];
			wksec = times[2];
		}

		if (wkhour == 0 && wkmin == 0 && wksec == 0) {
			tv_weekFocusTime.setText("-");
		} else {
			if (wkhour == 0) {
				tv_weekFocusTime.setText(wkmin + "분 " + wksec + "초");
			} else {
				tv_weekFocusTime.setText(wkhour + "시간 " + wkmin + "분");
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	/**
	 * 레코드 시작 시간(측정 시간) 낮은 = 오래된 것부터 정렬
	 * 
	 * @author Yedam
	 * 
	 */
	static class StartTimeAscSort implements Comparator<Record> {

		@Override
		public int compare(Record arg0, Record arg1) {
			return arg0.getTimeInSec() < arg1.getTimeInSec() ? -1 : arg0.getTimeInSec() > arg1.getTimeInSec() ? 1 : 0;
		}
	}

	/**
	 * 측정 시간이 큰(최근인) 거부터 정렬
	 * 
	 * @author Yedam
	 * 
	 */
	public static class StartTimeDescSort implements Comparator<Record> {

		@Override
		public int compare(Record arg0, Record arg1) {
			return arg0.getTimeInSec() > arg1.getTimeInSec() ? -1 : arg0.getTimeInSec() < arg1.getTimeInSec() ? 1 : 0;
		}
	}
}
