package com.realnumworks.focustimer.data;

import java.util.ArrayList;
import java.util.Calendar;

import android.util.Log;

import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.history.Record;
import com.realnumworks.focustimer.view.history.Weekth;

/**
 * 시작 Second과 끝 Second를 객체화하여 다루는 함수. DB접근을 Range 기준으로 하기 때문에, 이 클래스에서 접근 범위를
 * 받아와서 DB에 접근한다.
 * 
 * @author Yedam
 *
 */

public class TimeRangeManager {

	/**
	 * Record객체가 속한 한 주 전체의 Second Range를 리턴
	 */
	public static TimeRange getTimeRangeOfWeekByRecord(Record todayRecord) {
		Record newRecord = new Record(todayRecord);
		//4시 이전이면 이전 일자로 처리
		if(todayRecord.getHour() < 4){
			newRecord = Record.getPrevDate(newRecord);
		}
		// 한 주의 첫날을 설정한다.
		Record thisWeekStartRecord = DateTime
			.getWeekStartRecord(newRecord); // 한 주의 시작초
		int thisWeekStartSecond = thisWeekStartRecord.getTimeInSec();
		// 다음주의 첫날을 설정하기 위해 7일간 뺑뺑 돌린다.
		Record thisWeekFinishRecord = thisWeekStartRecord.clone();
		for (int numOfDay = 0; numOfDay < 7; numOfDay++) {
			thisWeekFinishRecord = Record.getNextDate(thisWeekFinishRecord);
		}
		int thisWeekFinishSecond = thisWeekFinishRecord.getTimeInSec();
//		Logs.d(Logs.HISTORY_MONTH, "이번 주 영역 : "
//			+ thisWeekStartRecord.getDateString("/", "") + " "
//			+ thisWeekFinishRecord.getTimeString() + " ~ "
//			+ thisWeekFinishRecord.getDateString("/", "") + " "
//			+ thisWeekFinishRecord.getTimeString());
		return new TimeRange(thisWeekStartSecond, thisWeekFinishSecond);
	}

	/**
	 * Second Range를 1일씩 분할해서 그 배열을 리턴한다.
	 */
	public static ArrayList<TimeRange> getSpiltedSecondRangeByDays(
			TimeRange wholeTimeRange, int spiltSize) {
		ArrayList<TimeRange> rangeList = new ArrayList<TimeRange>();
		Record startRecord = new Record(wholeTimeRange.getStartTimePoint());
		for (int dayCountIndex = 0; dayCountIndex < spiltSize; dayCountIndex++) {
			TimeRange spiltedTimeRange = getTimeRangeOfDay(startRecord.getYear(), startRecord.getMonth(), startRecord.getDate(), startRecord.getHour());
			rangeList.add(spiltedTimeRange);
			startRecord = Record.getNextDate(startRecord);
		}
		return rangeList;
	}

	public static TimeRange getTimeRangeOfDay(int year, int month, int day, int hour) {
		Record startRecord;
		if (hour >= 4)
			startRecord = new Record(year, month, day, 4, 0, 0);
		else
			startRecord = new Record(year, month, day - 1, 4, 0, 0);
		Record finishRecord = Record.getNextDate(startRecord);
		return new TimeRange(startRecord.getTimeInSec(), finishRecord.getTimeInSec());
	}

	public static TimeRange getTimeRangeOfWeekByYMW(int year, int month, int weekth) {
		Record wednesdayOfThisWeek = Record.getWendesdayInThisWeekRecord(year, month, weekth);
//		Logs.d(Logs.HISTORY_MONTH, "수 "+wednesdayOfThisWeek.toString());
		return TimeRangeManager.getTimeRangeOfWeekByRecord(wednesdayOfThisWeek);
	}
	
	public static TimeRange getTimeRangeOfMonthByYM(int year, int month) {
		int startPoint = getTimeRangeOfWeekByYMW(year, month, 1).getStartTimePoint();
		int finishPoint;
		if (month == Calendar.DECEMBER) {
			finishPoint = getTimeRangeOfWeekByYMW(year + 1, Calendar.JANUARY, 1).getStartTimePoint();
		}
		else {
			finishPoint = getTimeRangeOfWeekByYMW(year, month + 1, 1).getStartTimePoint();
		}
		return new TimeRange(startPoint, finishPoint);
	}
}
