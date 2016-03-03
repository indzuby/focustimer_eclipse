package com.realnumworks.focustimer.view.history;

import java.util.ArrayList;
import java.util.Collections;

import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.data.TimeRange;
import com.realnumworks.focustimer.data.TimeRangeManager;
import com.realnumworks.focustimer.utils.Logs;

public class MonthlyManager {

	private int startDate;
	private int finishDate;

	public void setStartDate(int startDate) {
		this.startDate = startDate;
	}

	public void setFinishDate(int finishDate) {
		this.finishDate = finishDate;
	}

	public int getStartDate() {
		return startDate;
	}

	public int getFinishDate() {
		return finishDate;
	}

	public static ArrayList<ItemString> getItemStringListFromRecordList(ArrayList<Record> recordList) {
		Weekth mostPreviousWeekth = DateTime.getWeekth(recordList.get(recordList.size() - 1));
		Weekth thisWeekth = DateTime.getWeekth(Record.getTodayRecord());
		ArrayList<ItemString> itemStringList = new ArrayList<ItemString>();
		Weekth weekthPoint;

		for (weekthPoint = mostPreviousWeekth; !Weekth.isTwoWeekthInSameMonth(weekthPoint, thisWeekth); weekthPoint.toNextMonth()) {
			inputItemInItemStringList(weekthPoint, recordList, itemStringList);
		}
		inputItemInItemStringList(weekthPoint, recordList, itemStringList);
		inputHeaderInItemStringList(thisWeekth, itemStringList);
		Collections.reverse(itemStringList);
		return itemStringList;
	}

	private static void inputItemInItemStringList(Weekth weekthPoint, ArrayList<Record> recordList,
			ArrayList<ItemString> itemStringList) {
		Weekth prevMonth = Weekth.getPrevMonthWeekth(weekthPoint);
		if (prevMonth.getYear() != weekthPoint.getYear()) {
			inputHeaderInItemStringList(prevMonth, itemStringList);
		}
		inputDataInItemStringList(weekthPoint, recordList, itemStringList);
	}

	private static void inputHeaderInItemStringList(Weekth weekth, ArrayList<ItemString> itemStringList) {
		String header = weekth.getYear() + "년";
		ItemString headerItemString = new ItemString(header, null, null, null, BaseItem.DTYPE_WEEK, 0);
		itemStringList.add(headerItemString);
	}

	private static void inputDataInItemStringList(Weekth weekthPoint, ArrayList<Record> recordList,
			ArrayList<ItemString> itemStringList) {
		TimeRange range = TimeRangeManager.getTimeRangeOfMonthByYM(weekthPoint.getYear(), weekthPoint.getMonth());
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
		return (weekthPoint.getMonth() + 1) + "월";
	}

	public static String getTextByTimeRange(TimeRange range) {
		Record rangeStartRecord = new Record(range.getStartTimePoint());
		Record rangeFinishRecord = new Record(range.getFinishTimePoint() - 1);
		Weekth rangeFinishWeekth;
		if (rangeFinishRecord.getHour() < 4) {
			rangeFinishRecord.toPrevDate();
		}
		rangeFinishWeekth = DateTime.getWeekth(rangeFinishRecord);
		int weekthCount = rangeFinishWeekth.getWeekDay();
		String text = (rangeStartRecord.getMonth() + 1) + "/" + (rangeStartRecord.getDate()) + "("
			+ rangeStartRecord.getDayString() + ")"
			+ "~"
			+ (rangeFinishRecord.getMonth() + 1) + "/" + (rangeFinishRecord.getDate()) + "("
			+ rangeFinishRecord.getDayString() + ")"
			+ "[" + weekthCount + "W]";
		return text;
	}

	public static String getBigTextByFocusTime(int focusTime) {
		return DateTime.getTimeMinString(DateTime.getHourMinSecFromTimeSec(focusTime));
	}

	public static boolean isTwoRecordsInSameMonth(Record r1, Record r2) {
		return isTwoWeekthsInSameMonth(r1.weekth, r2.weekth);
	}

	public static boolean isTwoWeekthsInSameMonth(Weekth w1, Weekth w2) {
		return (w1.getMonth() == w2.getMonth())
				&& (w1.getYear() == w2.getYear());
	}
}
