package com.realnumworks.focustimer.view.history;

import java.util.ArrayList;
import java.util.Calendar;

import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.data.TimeRange;
import com.realnumworks.focustimer.data.TimeRangeManager;
import com.realnumworks.focustimer.utils.Logs;

public class DayItem extends BaseItem {

	private int startTime;
	private int finishTime;

	private int count = 0;

	/**
	 * DTYPE_DAY에서
	 * 횟수를 세는 데 쓰임.
	 */

	public DayItem(Record record) {
		super(record);
		setDataType(DTYPE_DAY);
		setItemType(ITYPE_DATA);
		setStartTime(record.getTimeInSec());
		setFinishTime(record.getTimeInSec() + record.getFocustime());
	}

	public DayItem(Record record, int itemType) {
		super(record);
		setDataType(DTYPE_DAY);
		setItemType(itemType);
		setStartTime(record.getTimeInSec());
		setFinishTime(record.getTimeInSec() + record.getFocustime());
	}

	private void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setFinishTime(int finishTime) {
		this.finishTime = finishTime;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getFinishTime() {
		return finishTime;
	}

	public void plusCount() {
		count++;
	}

	public int getCount() {
		return count;
	}

	public ItemString getItemString() {
		String header = null, title = null, text = null, bigText = null;
		switch (getItemType()) {
			case BaseItem.ITYPE_HEADER:
				header = (weekth.getMonth() + 1) + "월 " + weekth.getWeekDay() + "주차, " + weekth.getYear() + "년";
				break;
			case BaseItem.ITYPE_DATA:
				title = (getMonth() + 1) + "월 " + getDate() + "일" + "(" + getDayString() + ")";
				text = DateTime.getAMPMString(getHour(), getMin());
				bigText = DateTime.getTimeMinString(DateTime.getHourMinSecFromTimeSec(getFocustime()));
				break;
		}
		return new ItemString(header, title, text, bigText, getDataType(), getDataCode());
	}

	public static boolean isTwoItemsInEqualDay(DayItem item1, DayItem item2) {
		TimeRange rangeOfThisDay = TimeRangeManager.getTimeRangeOfDay(item1.getYear(), item1.getMonth(), item1.getDate(), item1.getHour());
		return rangeOfThisDay.getStartTimePoint() <= item2.getFinishTime()
				&& item2.getFinishTime() < rangeOfThisDay.getFinishTimePoint();
	}

	public static ArrayList<DayItem> getItemListFromRecordList(ArrayList<Record> recordList) {
		ArrayList<DayItem> itemList = new ArrayList<DayItem>();
		for (int recordIndex = 0; recordIndex < recordList.size(); recordIndex++) {
			DayItem newItem = new DayItem(recordList.get(recordIndex));
			if (itemList.size() == 0) {
				inputInItemList(itemList, newItem, ITYPE_HEADER);
				inputInItemList(itemList, newItem, ITYPE_DATA);
			}
			else {
				DayItem lastItemInItemList = itemList.get(itemList.size() - 1);
				if (isTwoItemsInEqualDay(lastItemInItemList, newItem)) {
					addFocusTimeAtLastItemInItemList(itemList, newItem);
				}
				else {
					if (lastItemInItemList.weekth.getWeekDay() != newItem.weekth.getWeekDay()
						|| lastItemInItemList.weekth.getMonth() != newItem.weekth.getMonth()
						|| lastItemInItemList.weekth.getYear() != newItem.weekth.getYear()) {
						inputInItemList(itemList, newItem, ITYPE_HEADER);
					}
					inputInItemList(itemList, newItem, ITYPE_DATA);
				}
			}
		}
		return itemList;
	}

	private static void inputInItemList(ArrayList<DayItem> itemList, DayItem item, int itemType) {
		if (itemType == BaseItem.ITYPE_HEADER) {
			DayItem headerItem = new DayItem(item, BaseItem.ITYPE_HEADER);
			itemList.add(headerItem);
		}
		else {
			item.plusCount();
			itemList.add(item);
		}
	}

	private static void addFocusTimeAtLastItemInItemList(ArrayList<DayItem> itemList, DayItem item) {
		DayItem lastItem = itemList.get(itemList.size() - 1);
		itemList.remove(lastItem);
		lastItem.setFocustime(lastItem.getFocustime() + item.getFocustime());
		lastItem.setStartTime(item.getStartTime());
		lastItem.plusCount();
		itemList.add(lastItem);
	}

	public static ArrayList<ItemString> getItemStringListFromItemList(ArrayList<DayItem> itemList) {
		ArrayList<ItemString> itemStringList = new ArrayList<ItemString>();
		String header, title, text, bigText;
		for (int itemIndex = 0; itemIndex < itemList.size(); itemIndex++) {
			DayItem indexedItem = itemList.get(itemIndex);
			header = title = text = bigText = null;
			switch (indexedItem.getItemType()) {
				case BaseItem.ITYPE_HEADER:
					header = (indexedItem.weekth.getMonth() + 1) + "월 " + indexedItem.weekth.getWeekDay() + "주차, "
						+ indexedItem.weekth.getYear() + "년";
					itemStringList.add(new ItemString(header, title, text, bigText, BaseItem.DTYPE_DAY, indexedItem.getDataCode()));
					break;
				case BaseItem.ITYPE_DATA:
					title = (indexedItem.getMonth() + 1) + "월 " + indexedItem.getDate() + "일" + "("
						+ indexedItem.getDayString() + ")";
					Record startTimeOfIndexedItem = new Record(indexedItem.getStartTime());
					Record finishTimeOfIndexedItem = new Record(indexedItem.getFinishTime());
					text = DateTime.getAMPMString(startTimeOfIndexedItem.getHour(), startTimeOfIndexedItem.getMin())
						+ " ~ "
						+ DateTime.getAMPMString(finishTimeOfIndexedItem.getHour(), finishTimeOfIndexedItem.getMin())
						+ " [" + indexedItem.getCount() + "회]";
					bigText = DateTime.getTimeMinString(DateTime.getHourMinSecFromTimeSec(indexedItem.getFocustime()));
					itemStringList.add(new ItemString(header, title, text, bigText, BaseItem.DTYPE_DAY, indexedItem.getDataCode()));
					break;
			}
		}
		return itemStringList;
	}

	public int getDataCode() {
		return getYear() * 10000 + getMonth() * 100 + getDate();
	}

	public static void printItemList(ArrayList<DayItem> itemList) {
		Logs.d(Logs.HISTORY_DAY, "&& Print Item List &&");
		for (int itemIndex = 0; itemIndex < itemList.size(); itemIndex++) {
			Logs.d(Logs.HISTORY_DAY, itemList.get(itemIndex).toShortString());
		}
	}
}
