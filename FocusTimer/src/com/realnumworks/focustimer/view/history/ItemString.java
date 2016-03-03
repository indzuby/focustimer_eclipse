package com.realnumworks.focustimer.view.history;

import java.util.ArrayList;

import com.realnumworks.focustimer.utils.Logs;

public class ItemString {

	private int dataType;
	private int dataCode;

	/**
	 * dataCode는 dataType에 따라 달라짐.
	 * EACH - timeInSec(int)
	 * DAY - YYYYMMDD(int)
	 */

	private String header;
	private String title;
	private String text;
	private String bigText;

	public ItemString(String header, String title, String text, String bigText, int dataType, int dataCode) {
		this.header = header;
		this.title = title;
		this.text = text;
		this.bigText = bigText;
		this.dataType = dataType;
		this.dataCode = dataCode;
	}

	public String getHeader() {
		return header;
	}

	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}

	public String getBigText() {
		return bigText;
	}

	public int getDataCode() {
		return dataCode;
	}

	public int getDataType() {
		return dataType;
	}

	public static void printItemStringList(String logTag, ArrayList<ItemString> itemStringList) {
		Logs.d(logTag, "^^ Print Item String List ^^");
		for (ItemString item : itemStringList) {
			Logs.d(logTag, item.toString());
		}
	}

	public String toString() {
		return getHeader() + "/" + getTitle() + "/" + getText() + "/" + getBigText();
	}
}
