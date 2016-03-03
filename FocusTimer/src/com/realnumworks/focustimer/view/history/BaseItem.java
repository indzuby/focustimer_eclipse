package com.realnumworks.focustimer.view.history;

import java.util.ArrayList;

import com.realnumworks.focustimer.data.DateTime;

public class BaseItem extends Record {
	/**
	 * Header일 경우 Focustime이 0이거나 음수이다.
	 */

	public static final int ITYPE_DATA = 0;
	public static final int ITYPE_HEADER = 1;

	public static final int DTYPE_EACH = 0;
	public static final int DTYPE_DAY = 1;
	public static final int DTYPE_WEEK = 2;
	public static final int DTYPE_MONTH = 3;

	private int itemType;
	private int dataType;

	public BaseItem(Record record) {
		super(record);
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public int getItemType() {
		return itemType;
	}

	public int getDataType() {
		return dataType;
	}

	public int getDataCode() {
		switch (dataType) {
			case DTYPE_EACH:
				return getTimeInSec();
			case DTYPE_DAY:
				return getYear() * 10000 + getMonth() * 100 + getDate();
			default:
				return -1;
		}
	}
	
	public static void inputWeekthInRecordList(ArrayList<Record> recordList)	{
		for(int recordIndex = 0; recordIndex < recordList.size(); recordIndex++){
			Record indexedRecord = recordList.get(recordIndex);
			indexedRecord.weekth = DateTime.getWeekth(indexedRecord);
		}
	}
}
