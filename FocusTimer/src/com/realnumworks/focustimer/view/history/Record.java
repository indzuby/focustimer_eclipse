package com.realnumworks.focustimer.view.history;

import java.util.Calendar;
import java.util.TimeZone;

import com.realnumworks.focustimer.utils.Logs;

/**
 * 기본 Record 날짜 클래스.
 * 
 * @author Yedam
 *
 */
public class Record {

	private int year; // 년
	private int month; // 월
	private int date; // 일
	private int day; // 요일
	private int hour; // 24시계, 시
	private int min; // 분
	private int sec; // 초
	private Calendar cal; // 현재 fd의 정보가 set 되어 있는 캘린더객체
	public Weekth weekth;
	private int focustime;

	/**
	 * 넣을 땐 4시컷, 날짜 이월 등등 신경안쓰고 넣는다. 안에서 처리해줌. Weekth와 Focustime은 처리되지 않는다.
	 * 
	 * @param _year
	 * @param _month
	 * @param _date
	 * @param _hour
	 *            24시계 기준
	 * @param _min
	 * @param _sec
	 */
	public Record(int _year, int _month, int _date, int _hour, int _min,
			int _sec) {
		year = _year;
		month = _month;
		date = _date;
		hour = _hour;
		min = _min;
		sec = _sec;
		cal = Calendar.getInstance(); // Locale을 넣을거면 나중에 싱글톤에서 불러와서 넣던지.
		resetCal();
		day = cal.get(Calendar.DAY_OF_WEEK);
		weekth = new Weekth();
		setFocustime(0);
	}

	/**
	 * Hard Copy 파라미터가 null일 경우, 현재 시간의 FocusTime으로 반환한다.
	 * 
	 * @param _focusDate
	 */
	public Record(Record _focusDate) {
		//오늘자 데이터로.
		if (_focusDate == null) {
			cal = Calendar.getInstance();
			_focusDate = new Record(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH), cal.get(Calendar.DATE),
				cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND));
		}
		year = _focusDate.year;
		month = _focusDate.month;
		date = _focusDate.date;
		day = _focusDate.day;
		hour = _focusDate.hour;
		min = _focusDate.min;
		sec = _focusDate.sec;
		weekth = new Weekth(_focusDate.weekth.getYear(),
			_focusDate.weekth.getMonth(), _focusDate.weekth.getWeekDay());
		setFocustime(_focusDate.getFocustime());
		cal = Calendar.getInstance(); // Locale을 넣을거면 나중에 싱글톤에서 불러와서 넣던지.
		resetCal();
	}

	public Record(int timeInSec) {
		cal = Calendar.getInstance();
		long millis = (long)timeInSec * 1000;
		cal.setTimeInMillis(millis);

		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		date = cal.get(Calendar.DATE);
		day = cal.get(Calendar.DAY_OF_WEEK);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		min = cal.get(Calendar.MINUTE);
		this.sec = cal.get(Calendar.SECOND);
		setFocustime(0);
		weekth = new Weekth();
		Logs.d("DateTimeRecord", this.toString());
	}

	public static Record getTodayRecord() {
		return new Record((int)(Calendar.getInstance().getTimeInMillis() / 1000));
	}

	/**
	 * 2015-10-21 6시 53분 44초, 총 집중 시간 56sec, 2015년 10월 4주차
	 */
	@Override
	public String toString() {
		return year + "-" + (month + 1) + "-" + date + " " + getDayString()
			+ " " + hour + "시 " + min + "분 " + sec + "초, 총 집중 시간 "
			+ getFocustime() + "sec, " + weekth.toString();
	}

	/**
	 * 10/21 5:09 55초 집중
	 * @return
	 */
	public String toShortString() {
		return (month + 1) + "/" + date + " " + hour + ":"
			+ (min < 10 ? "0" + min : min) + " " + getFocustime() + "초 집중";
	}

	/**
	 * 5:09:17
	 * @return
	 */
	public String toShortTimeString() {
		return hour + ":" + min + ":" + sec;
	}

	/**
	 * 요일을 문자열로 바꿔 리턴해준다.
	 * 
	 * @param day
	 * @return
	 */
	public String getDayString() {
		String sday = "";
		switch (day) {
			case Calendar.SUNDAY:
				sday = "일";
				break;
			case Calendar.MONDAY:
				sday = "월";
				break;
			case Calendar.TUESDAY:
				sday = "화";
				break;
			case Calendar.WEDNESDAY:
				sday = "수";
				break;
			case Calendar.THURSDAY:
				sday = "목";
				break;
			case Calendar.FRIDAY:
				sday = "금";
				break;
			case Calendar.SATURDAY:
				sday = "토";
				break;
		}
		return sday;
	}

	/**
	 * "month"/"date"
	 * 
	 * @return
	 */
	public String getDateString(String monthdiv, String datediv) {
		return (month + 1) + monthdiv + date + datediv;
	}

	/**
	 * 9h 0m 3s
	 * 
	 * @return
	 */
	public String getTimeString() {
		return hour + "h " + min + "m " + sec + "s";
	}

	public static String getFocusTimeInString(int focustime) {
		if (focustime == 0)
			return "-";
		else {
			if (focustime < 60) // 1초~60초
			{
				return focustime + "s";
			} else {
				int min = focustime / 60;
				if (min < 60)
					return min + "m";
				else {
					int hour = min / 60;
					min = min % 60;
					int decimal_min = min * 100 / 60;
					return hour + "." + String.valueOf(decimal_min).charAt(0)
						+ "h";
				}
			}
		}
	}

	public static Record getPrevDate(Record currentDate) {
		Record prevDate = new Record(currentDate); // 하드카피

		if (prevDate.date == 1) // 오늘이 1일이면, 전 달 마지막날로 간다.
		{
			if (prevDate.month == Calendar.JANUARY) // 오늘이 1월이면, 전년도 12월로 간다.
			{
				prevDate.month = Calendar.DECEMBER;
				prevDate.year--;
			} else // 1월이 아니면 전달로 간다.
			{
				prevDate.month--;
			}
			// 여기까지 month이동은 다 된 상태이므로 현재 month의 마지막 날짜로 이동한다.
			prevDate.resetCal();
			prevDate.date = prevDate.cal.getActualMaximum(Calendar.DATE);
			prevDate.resetCal();
		} else {
			prevDate.date--;
			prevDate.resetCal();
		}
		return prevDate;
	}

	public void toPrevDate() {
		if (date == 1) {
			if (month == Calendar.JANUARY) {
				month = Calendar.DECEMBER;
				year--;
			}
			else {
				month--;
			}
			resetCal();
			date = getMaximumDateOfThisMonth();
		}
		else {
			date--;
		}
		resetCal();
	}

	public static Record getNextDate(Record currentDate) {
		Record todayDate = new Record(currentDate); // 하드카피
		todayDate.toNextDate();
		return todayDate;
	}

	public void toNextDate() {
		if (date == getMaximumDateOfThisMonth()) {
			if (month == Calendar.DECEMBER) {
				month = Calendar.JANUARY;
				year++;
			}
			else {
				month++;
			}
			date = 1;
		}
		else {
			date++;
		}
		resetCal();
	}

	public void setFocustime(int focustime) {
		this.focustime = focustime;
	}

	public void setWeekth(Weekth weekth) {
		this.weekth = weekth;
	}

	public void resetCal() {
		cal.set(year, month, date, hour, min, sec);
		day = cal.get(Calendar.DAY_OF_WEEK);
	}

	public void setCal(int year, int month, int date, int hour, int min, int sec) {
		cal.set(year, month, date, hour, min, sec);
		day = cal.get(Calendar.DAY_OF_WEEK);
	}

	public void setTimeZone(TimeZone tz) {
		cal.setTimeZone(tz);
	}

	public void setTimeInSec(long sec) {
		cal.setTimeInMillis(1000 * sec);
	}

	public int getTimeInSec() {
		return (int)(cal.getTimeInMillis() / 1000);
	}

	public void setDate(int date) {
		this.date = date;
		this.resetCal();
	}

	public void setTime(int hour, int min, int sec) {
		this.hour = hour;
		this.min = min;
		this.sec = sec;
		this.resetCal();
	}

	public int getDay() {
		return day;
	}

	public int getDate() {
		return date;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getHour() {
		return hour;
	}

	public int getAMPM() {
		return cal.get(Calendar.AM_PM);
	}

	public int getHourAMPM() {
		return cal.get(Calendar.HOUR);
	}

	public int getMin() {
		return min;
	}

	public int getSec() {
		return sec;
	}

	public Record clone() {
		return new Record(this);
	}

	public int getFocustime() {
		return focustime;
	}

	public int getMaximumDateOfThisMonth() {
		return cal.getActualMaximum(Calendar.DATE);
	}

	public static Record getWendesdayInThisWeekRecord(int year, int month, int weekDay) {
		Record firstWednesdayInThisMonth = getFirstWednesdayInThisMonthRecord(year, month);
		int firstWendesdayDate = firstWednesdayInThisMonth.getDate();
		Record wednesdayOfThisWeek = new Record(year, month, firstWendesdayDate + 7 * (weekDay - 1), 4, 0, 0);
		return wednesdayOfThisWeek;
	}

	public static Record getFirstWednesdayInThisMonthRecord(int year, int month) {
		Record firstWednesdayInThisMonth = new Record(year, month, 1, 4, 0, 0);
		while (firstWednesdayInThisMonth.getDay() != Calendar.WEDNESDAY) {
			firstWednesdayInThisMonth = Record.getNextDate(firstWednesdayInThisMonth);
		}
		return firstWednesdayInThisMonth;
	}
}
