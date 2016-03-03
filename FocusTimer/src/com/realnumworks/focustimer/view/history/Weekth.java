package com.realnumworks.focustimer.view.history;

import java.util.Calendar;

import android.util.Log;

import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.data.TimeRange;
import com.realnumworks.focustimer.data.TimeRangeManager;
import com.realnumworks.focustimer.utils.Logs;

/**
 * 주차 클래스. 몇 년 몇 월의 몇 주차인지 포함하고 있다.
 * 
 * @author Yedam
 *
 */
public class Weekth {
	private int year;
	private int month;
	private int weekDay;

	public Weekth(int _year, int _month, int _weekth) {
		setYear(_year);
		setMonth(_month);
		setWeekDay(_weekth);
	}

	public Weekth() {
		setYear(0);
		setMonth(0);
		setWeekDay(0);
	}

	@Override
	public String toString() {
		return getYear() + "년 " + (getMonth() + 1) + "월 " + getWeekDay() + "주차";
	}

	public int getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public static boolean isTwoRecordsInSameWeek(Record record1, Record record2) {
		return isTwoWeekthSame(record1.weekth, record2.weekth);
	}

	public static boolean isTwoWeekthSame(Weekth wk1, Weekth wk2) {
		return wk1.getWeekDay() == wk2.getWeekDay()
				&& isTwoWeekthInSameMonth(wk1, wk2);
	}

	public static boolean isTwoWeekthInSameMonth(Weekth wk1, Weekth wk2) {
		return wk1.getMonth() == wk2.getMonth()
				&& wk1.getYear() == wk2.getYear();
	}

	public void toNextWeek() {
		Record firstWednesdayOfThisMonth = Record.getFirstWednesdayInThisMonthRecord(year, month);
		int assumedNextWeekDate = firstWednesdayOfThisMonth.getDate() + 7 * (weekDay);
		if (assumedNextWeekDate > firstWednesdayOfThisMonth.getMaximumDateOfThisMonth()) {
			//이번달이 12월이면 다음년으로
			if (month == Calendar.DECEMBER) {
				year++;
				month = Calendar.JANUARY;
			}
			else {
				month++;
			}
			weekDay = 1;
		}
		else {
			weekDay++;
		}
	}

	public static Weekth getNextWeekth(Weekth weekth) {
		Weekth newWeekth = new Weekth(weekth.year, weekth.month, weekth.weekDay);
		newWeekth.toNextWeek();
		return newWeekth;
	}

	public static Weekth getPrevWeekth(Weekth weekth) {
		Weekth newWeekth = new Weekth(weekth.year, weekth.month, weekth.weekDay);
		newWeekth.toPrevWeek();
		return newWeekth;
	}

	public static Weekth getPrevMonthWeekth(Weekth weekth) {
		Weekth newWeekth = new Weekth(weekth.year, weekth.month, weekth.weekDay);
		if (newWeekth.month == Calendar.JANUARY) {
			newWeekth.year--;
			newWeekth.month = Calendar.DECEMBER;
		}
		else {
			newWeekth.month--;
		}
		return newWeekth;
	}

	public void toPrevMonth() {
		if (month == Calendar.JANUARY) {
			year--;
			month = Calendar.DECEMBER;
		}
		else {
			month--;
		}
	}

	public void toPrevWeek() {
		//if 1주차이면
		if (weekDay == 1) {
			//이전월의 마지막주차로 넘어감
			if (month == Calendar.JANUARY) {
				year--;
				month = Calendar.DECEMBER;
			}
			else {
				month--;
			}
			Record firstWendesdayOfPrevMonth = Record.getFirstWednesdayInThisMonthRecord(year, month);
			int assumedPrevWeekDate = firstWendesdayOfPrevMonth.getDate() + 7 * (5 - 1); //5주차까지 있다고 가정하고
			if (assumedPrevWeekDate > firstWendesdayOfPrevMonth.getMaximumDateOfThisMonth()) { //이전월의 최대날짜보다 가정날짜가 많으면(이상상황)
				weekDay = 4;
			} //4주차이다.
			else {
				weekDay = 5;
			}
		}
		else {
			weekDay--;
		}
	}

	public void toNextMonth() {
		if (month == Calendar.DECEMBER) {
			month = Calendar.JANUARY;
			year++;
		}
		else {
			month++;
		}
	}
}
