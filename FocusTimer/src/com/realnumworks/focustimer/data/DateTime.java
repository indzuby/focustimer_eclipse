package com.realnumworks.focustimer.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.content.Context;

import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.history.Record;
import com.realnumworks.focustimer.view.history.Weekth;
import com.realnumworks.focustimer.view.main.MainFragment;

/**
 * 날짜와 시간 관련 처리해주는 클래스. 모든 함수는 static으로 선언되어 있어 객체생성이 필요없다.
 * 
 * @author Yedam
 * 
 */

public class DateTime {
	/**
	 * 현재의 년, 월, 일이 몇 년 몇 월의 몇 주차인지 Weekth객체를 리턴해준다.
	 * 
	 * @param year
	 * @param month
	 * @param date
	 * @return
	 */
	public static Weekth getWeekth(Record targetRecord) {
		
		// A. 해당 년, 월의 첫 수요일을 찾아 그것을 7로 나눈 몫을 리턴한다.
		int first_quot, current_quot; // 수요일의 일을 7로 나눈 나머지
		Weekth wkth = new Weekth();
//		Record currentRecord = new Record(targetRecord); // 현재 날짜
		Record first_wed_fd = new Record(targetRecord); // 이 달의 첫 번째 수요일
		Record current_wed_fd = new Record(targetRecord); // 현재와 가장 가까운 수요일

		int option = StateSingleton.getInstance().getDstate(); // 기본 옵션은 일시작으로 세팅

		/**
		 * A. 이번 주 수요일 찾는 로직 해당 년, 월, 일로부터 일정기간 안의 수요일을 찾아 그것을 7로 나눈 몫을 리턴한다.
		 */
		Logs.d(Logs.WEEKTHTEST, "오늘 : " + current_wed_fd.toString());
		if (current_wed_fd.getDay() != Calendar.WEDNESDAY) // 현재 요일이 수요일이 아닌 경우, 수요일 찾기 루프의 시작점을 set한다.
		{
			if (option == StateSingleton.STARTWITH_SUNDAY) // 일요일 시작이면 자신으로부터 -3~+3 안에 있는 수요일을 찾는다.
			{
				for (int i = 0; i < 3; i++) // 3일 전으로 당김
				{
					current_wed_fd.toPrevDate();
					Logs.d(Logs.WEEKTHTEST, "앞으로 땡겨서 : " + current_wed_fd.toString());
				}
			} else if (option == StateSingleton.STARTWITH_MONDAY) // 월요일 시작이면 자신으로부터 -4~+2 안에 있는 수요일을 찾는다.
			{
				for (int i = 0; i < 4; i++) // 4일 전으로 당김
				{
					current_wed_fd.toPrevDate();
					Logs.d(Logs.WEEKTHTEST, "앞으로 땡겨서 : " + current_wed_fd.toString());
				}
			}
		}
		while (current_wed_fd.getDay() != Calendar.WEDNESDAY) // 첫 요일이 수요일이 아닐 경우에 빙빙 돌며 다음 수요일을 찾는다. (수요일 찾기 루프)
		{
			if (current_wed_fd.getDay() == Calendar.WEDNESDAY)
				break;
			current_wed_fd.toNextDate();
			Logs.d(Logs.WEEKTHTEST, "뒤로 땡겨서 : " + current_wed_fd.toString());
		}

		Logs.d(Logs.WEEKTHTEST, "이번주 수요일 : " + current_wed_fd.toString());
		// 이 시점에서 current_wed_date는 현재 날짜가 속한 주의 수요일이다.

		current_quot = current_wed_fd.getDate() / 7;

		/**
		 * A. 첫 수요일 찾는 로직
		 */
		first_wed_fd = new Record(current_wed_fd); // 초기화. 이전 단계에서 전 달로 넘어갔으면 first_wed도 같이 넘어가게 된다.

		first_wed_fd.setDate(1); // 날짜를 1로 세팅

		while (first_wed_fd.getDay() != Calendar.WEDNESDAY) // 첫 요일이 수요일이 아닐 경우에 빙빙 돌며 다음 수요일을 찾는다.
		{
			first_wed_fd.toNextDate();
		}
		// 이 시점에서 first_wed_date은 그 달의 첫 수요일이 위치한 날짜(일)을 가리키고 있다.
//		Logs.d(Logs.WEEKTHTEST, "이번달 첫주 수요일 : " + first_wed_fd.toString());
		first_quot = first_wed_fd.getDate() / 7;

		// B-A+1 이 주차이다.
		wkth.setYear(current_wed_fd.getYear());
		wkth.setMonth(current_wed_fd.getMonth());
		wkth.setWeekDay(current_quot - first_quot + 1);

//		Logs.d(Logs.WEEKTHTEST, "주차 구하기 : " + wkth.toString());

		return wkth;
	}

	/**
	 * 해당 sec가 속한 주의 첫날 오전 4:00의 Record 객체를 리턴한다.
	 * 
	 * @param sec
	 * @return
	 */
	public static Record getWeekStartRecord(Record todayRecord) {
		int option = StateSingleton.getInstance().getDstate(); // 기본 옵션은 일시작으로 세팅

		Record startRecord = new Record(todayRecord);

		switch (option) {
			case StateSingleton.STARTWITH_SUNDAY:
				// 한 주의 시작 날짜 : 그 주 일요일
				while (startRecord.getDay() != Calendar.SUNDAY) // 일요일이 될 때까지
				{
					startRecord.toPrevDate(); // 하루씩 앞으로 ㄱㄱ
				}
				break;
			case StateSingleton.STARTWITH_MONDAY:
				// 한 주의 시작 날짜 : 그 주 일요일
				while (startRecord.getDay() != Calendar.MONDAY) // 월요일이 될 때까지
				{
					startRecord.toPrevDate(); // 하루씩 앞으로 ㄱㄱ
				}
				break;
		}
		Logs.d(Logs.DATETEST, "start Of this week = " + startRecord.toString());
		startRecord.setTime(4, 0, 0);
		return startRecord;
	}

	/**
	 * ArrayList에 속한 모든 Record의 focustime의 합을 리턴한다.
	 * 
	 * @param sec
	 * @return
	 */
	public static int getSumOfFocusTimesInSelectedDay(ArrayList<Record> recordsOfSelectedDay) {
		int sumOfFocusTimesOfSelectedDay = 0;
		if (recordsOfSelectedDay == null)
			return 0;
		for (int recordIndex = 0; recordIndex < recordsOfSelectedDay.size(); recordIndex++) {
			sumOfFocusTimesOfSelectedDay += recordsOfSelectedDay.get(recordIndex).getFocustime();
		}
		return sumOfFocusTimesOfSelectedDay;
	}

	/**
	 * Record Array에서 해당 TimeRange에 속하는 Record만을 리턴한다.
	 * 
	 * @param themeId
	 * @return
	 */
	public static ArrayList<Record> getRecordsInTimeRange(TimeRange timeRange, ArrayList<Record> allRecords) {
		if (allRecords == null) {
			return null;
		}
		ArrayList<Record> selectedRecords = new ArrayList<Record>();
		for (int allRecordsIndex = 0; allRecordsIndex < allRecords.size(); allRecordsIndex++) {
			int recordTimeSec = allRecords.get(allRecordsIndex).getTimeInSec();
			if (timeRange.getStartTimePoint() <= recordTimeSec && recordTimeSec < timeRange.getFinishTimePoint()) {
				selectedRecords.add(allRecords.get(allRecordsIndex));
			}
		}
		return selectedRecords;
	}

	/**
	 * 하루의 마지막 집중시간을 리턴한다.
	 * 
	 * @param currentSec
	 *            해당 currentSec가 속한 날짜의 새벽 4:00부터 다음날 새벽 4:00전까지 중 마지막 집중시간 구함
	 * @return
	 */
	public static int getLastFocusedTimeInSecForSelectedDay(int currentSec, Context context) {
		Record todayStartRecord = new Record(currentSec);
		Record nextDayStartRecord = new Record(currentSec);
		todayStartRecord.setTime(4, 0, 0); // 오늘 4시
		nextDayStartRecord = Record.getNextDate(nextDayStartRecord);
		nextDayStartRecord.setTime(4, 0, 0); // 내일 4시

		int todayStartSec = todayStartRecord.getTimeInSec();
		int nextDayStartSec = nextDayStartRecord.getTimeInSec();
		TimeRange todayRange = new TimeRange(todayStartSec, nextDayStartSec); // 이 안에 있는 범위

		Logs.d(Logs.TIMERTEST, todayRange.toString());
		Logs.d(Logs.TIMERTEST, todayRange.toDateString());

		DataBaseHelper dbh = new DataBaseHelper(context);
		ArrayList<Record> todayRecords = dbh.getRecordsInSecondRangeByTheme(todayRange, null); // 의 모든 레코드를 불러와서
		if (todayRecords == null) // 만약 오늘 측정기록이 없으면
		{
			return 0; // 0 리턴
		} else {
			Collections.sort(todayRecords, new MainFragment.StartTimeDescSort()); // 최근 기록이 맨 위에 오도록 정렬하고
			return todayRecords.get(0).getTimeInSec() + todayRecords.get(0).getFocustime(); // 맨 위의 레코드의 측정시작시간+집중시간을 더해서 리턴
		}
	}

	/**
	 * 집중시간을 초로 넣으면 HMS문자열로 리턴해준다.
	 * 
	 * @param _sec
	 * @return
	 */
	public static String getTimeStringFromSec(int _sec) {
		int hour = 0, min = 0, sec = _sec;
		String str = "";
		Logs.d("getTimeStringFromSec", "바꿀 sec = " + sec);
		if (sec > 60) {
			min = sec / 60;
			sec = sec % 60;
		}
		if (min > 60) {
			hour = min / 60;
			min = min % 60;
		}

		if (hour != 0)
			str += hour + "H";
		if (min != 0)
			str += min + "M";
		str += sec + "S";
		hour = min = sec = 0;
		Logs.d("getTimeStringFromSec", "바뀐 문자열 = " + str);
		return str;
	}

	/**
	 * sec을 넣으면 hour, min, sec을 리턴한다.
	 * 
	 * @return
	 */
	public static int[] getHourMinSecFromTimeSec(int currentSec) {
		int times[] = new int[]{0, 0, 0};

		if (0 <= currentSec && currentSec < 60) // 0초~59초
		{
			times[2] = currentSec;
			return times;
		} else if (60 <= currentSec) // 60초 이상이면 분단위로 끊는다.
		{
			times[1] = currentSec / 60;
			times[2] = currentSec % 60;
			if (times[1] >= 60) // 60분 이상이면 시간단위로 끊는다.
			{
				times[0] = times[1] / 60;
				times[1] = times[1] % 60;
			}
		}
		Logs.d(Logs.MAPTEST, currentSec + "초 -> " + times[0] + "시간" + times[1] + "분" + times[2] + "초");
		return times;
	}

	/**
	 * 시, 분, 초를 입력하면 AMPM 포함 Str로 바꿔 리턴
	 * 
	 * @param hour
	 * @param min
	 * @return 오전 9:07
	 */
	public static String getAMPMString(int hour, int min) {
		String ampmstr = "";
		int _hour = hour, _min = min;
		if (_hour <= 12) {
			ampmstr += "오전";
		} else {
			ampmstr += "오후";
			_hour -= 12;
		}
		ampmstr += " " + _hour + ":";
		if (min < 10) // 3분 등일 때, 03분으로 바꿔준다.
		{
			ampmstr += "0";
		}
		ampmstr += _min + "";

		return ampmstr;
	}

	/**
	 * '초' 상태의 날짜를 반환하면 "0월 0일, 0000년" 으로 바꿔 출력한다.
	 * 
	 * @param lastfocusSec
	 * @return
	 */
	public static String getDateString(long lastfocusSec) {
		String str = "";
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(lastfocusSec * 1000);
		// int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int date = calendar.get(Calendar.DATE);
		str = month + "월 " + date + "일";
		return str;
	}

	public static String getTimeString(long timeOfSec, int unit) {
		int times[] = DateTime.getHourMinSecFromTimeSec((int)timeOfSec);
		int hour = times[0], min = times[1], sec = times[2];
		switch (unit) {
			case 1:
				// 소수점 단위
				if (hour > 0)
					return hour + "." + (min * 60) / 100 + "시간";
				else if (min > 0)
					return min + "." + (sec * 60) / 100 + "분";
				else
					return sec + "초";
			case 2:
				// A시간 B분/A분 B초/A초
				if (hour > 0)
					return hour + "시간 " + min + "분";
				else if (min > 0)
					return min + "분 " + sec + "초";
				else
					return sec + "초";
			case 3:
				if (hour > 0)
					return hour + "시간 " + min + "분 " + sec + "초";
				else if (min > 0)
					return min + "분 " + sec + "초";
				else
					return sec + "초";
			default:
				return "";
		}
	}

	/**
	 * 시간, 분, 초를 받아 X시간 X분 또는 X분 X초로 바꿔준다.
	 * 
	 * @param hms
	 * @return
	 */
	public static String getTimeMinString(int[] times) {
		String str = "";
		int hour = times[0], min = times[1], sec = times[2];
		if (hour > 0) // 1시간 1분
		{
			str += hour + "시간";
		}
		if (min != 0) {
			str += " " + min + "분";
		}
		if (hour == 0)
			str += " " + sec + "초";
		return str;
	}
}
