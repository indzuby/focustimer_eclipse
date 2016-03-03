package com.realnumworks.focustimer.view.history;

import static com.realnumworks.focustimer.utils.Logs.HISTORY_WEEK;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.R.string;
import android.util.Log;

import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.data.TimeRange;
import com.realnumworks.focustimer.data.TimeRangeManager;
import com.realnumworks.focustimer.utils.Logs;

public class WeeklyManager {

	public static ArrayList<ItemString> getItemStringListFromRecordList(ArrayList<Record> recordList) {
		Weekth mostPreviousWeekth = DateTime.getWeekth(recordList.get(recordList.size() - 1));
		Weekth thisWeekth = DateTime.getWeekth(Record.getTodayRecord());
		ArrayList<ItemString> itemStringList = new ArrayList<ItemString>();
		Weekth weekthPoint;

		for (weekthPoint = mostPreviousWeekth; !Weekth.isTwoWeekthSame(weekthPoint, thisWeekth); weekthPoint.toNextWeek()) {
			inputItemInItemStringList(weekthPoint, mostPreviousWeekth, recordList, itemStringList);
		}
		inputItemInItemStringList(weekthPoint, mostPreviousWeekth, recordList, itemStringList);
		inputHeaderInItemStringList(thisWeekth, itemStringList);
		Collections.reverse(itemStringList);
		return itemStringList;
	}

	private static void inputItemInItemStringList(Weekth weekthPoint, Weekth mostPreviousWeekth,
			ArrayList<Record> recordList,
			ArrayList<ItemString> itemStringList) {
		if (!Weekth.isTwoWeekthSame(weekthPoint, mostPreviousWeekth)) {
			Weekth prevWeekth = Weekth.getPrevWeekth(weekthPoint);
			if (prevWeekth.getMonth() != weekthPoint.getMonth()) {
				inputHeaderInItemStringList(prevWeekth, itemStringList);
			}
		}
		inputDataInItemStringList(weekthPoint, recordList, itemStringList);
	}

	private static void inputHeaderInItemStringList(Weekth weekth, ArrayList<ItemString> itemStringList) {
		String header = weekth.getYear() + "년 " + (weekth.getMonth() + 1) + "월";
		ItemString headerItemString = new ItemString(header, null, null, null, BaseItem.DTYPE_WEEK, 0);
		itemStringList.add(headerItemString);
	}

	private static void inputDataInItemStringList(Weekth weekthPoint, ArrayList<Record> recordList,
			ArrayList<ItemString> itemStringList) {
		TimeRange range = TimeRangeManager.getTimeRangeOfWeekByYMW(weekthPoint.getYear(), weekthPoint.getMonth(), weekthPoint.getWeekDay());
		ArrayList<Record> partList = DateTime.getRecordsInTimeRange(range, recordList);
		int sumOfFocusTime = getSumOfFocusTimeInPartList(partList);
		String title = getTitleByWeekthPoint(weekthPoint);
		String text = getTextByTimeRange(range);
		String bigText = getBigTextByFocusTime(sumOfFocusTime);
		ItemString itemString = new ItemString(null, title, text, bigText, BaseItem.DTYPE_WEEK, 0);
		itemStringList.add(itemString);
	}

	private static int getSumOfFocusTimeInPartList(ArrayList<Record> partList) {
		int sumOfFocusTime = 0;
		for (int partIndex = 0; partIndex < partList.size(); partIndex++) {
			sumOfFocusTime += partList.get(partIndex).getFocustime();
		}
		return sumOfFocusTime;
	}

	public static String getTitleByWeekthPoint(Weekth weekthPoint) {
		return (weekthPoint.getMonth() + 1) + "월 " + (weekthPoint.getWeekDay()) + "주차";
	}

	public static String getTextByTimeRange(TimeRange range) {
		Record rangeStartRecord = new Record(range.getStartTimePoint());
		Record rangeFinishRecord = new Record(range.getFinishTimePoint() - 1);
		if (rangeFinishRecord.getHour() < 4) {
			rangeFinishRecord.toPrevDate();
		}
		String text = (rangeStartRecord.getMonth() + 1) + "/" + (rangeStartRecord.getDate()) + "("
			+ rangeStartRecord.getDayString() + ")"
			+ "~"
			+ (rangeFinishRecord.getMonth() + 1) + "/" + (rangeFinishRecord.getDate()) + "("
			+ rangeFinishRecord.getDayString() + ")";
		return text;
	}

	public static String getBigTextByFocusTime(int focusTime) {
		return DateTime.getTimeMinString(DateTime.getHourMinSecFromTimeSec(focusTime));
	}
}
