package com.realnumworks.focustimer.view.history;

import java.util.ArrayList;

import com.realnumworks.focustimer.data.DateTime;

public class EachItem extends BaseItem {
	public EachItem(Record record) {
		super(record);
		setDataType(DTYPE_EACH);
		setItemType(ITYPE_DATA);
	}

	public EachItem(Record record, int itemType) {
		super(record);
		setDataType(DTYPE_EACH);
		setItemType(itemType);
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

	public static ArrayList<ItemString> getItemStringListFromRecordList(ArrayList<Record> recordList) {
		ArrayList<EachItem> itemList = new ArrayList<EachItem>();
		ArrayList<ItemString> itemStringList = new ArrayList<ItemString>();
		for (int recordListIndex = 0; recordListIndex < recordList.size(); recordListIndex++) {
			EachItem newItem = new EachItem(recordList.get(recordListIndex));
			//현재 Item 앞에 Header가 들어가야 하는 경우
			//Case 1 첫 번째 레코드 바로 앞
			//Case 2 이전 Record와 Weekth가 다를 경우
			// (셋중 하나라도 직전 주차와 안맞으면(주차-월-년 순으로 비교한다)))
			if (itemList.size() == 0)
			{
				inputInItemList(itemStringList, itemList, newItem, BaseItem.ITYPE_HEADER);
			}
			else
			{
				EachItem lastItemInItemList = itemList.get(itemList.size() - 1);

				if (lastItemInItemList.weekth.getWeekDay() != newItem.weekth.getWeekDay()
					|| lastItemInItemList.weekth.getMonth() != newItem.weekth.getMonth()
					|| lastItemInItemList.weekth.getYear() != newItem.weekth.getYear()) {
					inputInItemList(itemStringList, itemList, newItem, BaseItem.ITYPE_HEADER);
				}
			}
			inputInItemList(itemStringList, itemList, newItem, BaseItem.ITYPE_DATA);
		}
		return itemStringList;
	}

	private static void inputInItemList(ArrayList<ItemString> itemStringList, ArrayList<EachItem> itemList,
			EachItem item, int itemType) {
		if (itemType == BaseItem.ITYPE_HEADER) {
			EachItem headerItem = new EachItem(item, BaseItem.ITYPE_HEADER);
			itemList.add(headerItem);
			itemStringList.add(headerItem.getItemString());
		}
		else
		{
			itemList.add(item);
			itemStringList.add(item.getItemString());
		}
	}
}
