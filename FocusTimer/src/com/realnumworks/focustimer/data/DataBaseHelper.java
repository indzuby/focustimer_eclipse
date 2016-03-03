package com.realnumworks.focustimer.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.history.BaseItem;
import com.realnumworks.focustimer.view.history.Record;
import com.realnumworks.focustimer.view.settings.Settings;
import com.realnumworks.focustimer.view.theme.Theme;

/**
 * Created by Yedam on 2015-05-08.
 */

class DataBaseInfo {
	static class DB {
		public static final String NAME = "focustimer.db";
		public static final int VERSION = 2;
	}

	static class Table {
		static class RecordTable {
			public static final String NAME = "Record";
			public static final String COL_1 = "startTime"; // sec 기준
			public static final String COL_2 = "focustime"; // sec 기준
			public static final String COL_3 = "themeId"; // 테마 Id
		}

		class ThemeTable {
			public static final String NAME = "Theme";
			public static final String COL_1 = "themeId"; // 테마 생성 시간 기준, 최초 3개
															// 테마의 id는 0, 1, 2
			public static final String COL_2 = "themeName";
			public static final String COL_3 = "themeColor";
			public static final String COL_4 = "themeOrder";
		}

		class SettingsTable {
			public static final String NAME = "Settings";
			public static final String COL_1 = "startWith"; // "SUN=1" OR
															// "MON=2"
			public static final String COL_2 = "alarm01"; // 분단위
			public static final String COL_3 = "alarm02"; // 분단위
			public static final String COL_4 = "alarmSelected"; // 0, 1, 2
			public static final String COL_5 = "isTutorial01Shown"; // true(0)면
																	// 기존시작,
																	// false(1)면
																	// 최초시작
			public static final String COL_6 = "isTutorial02Shown"; // true(0)면
																	// 기존시작,
																	// false(1)면
																	// 최초시작
			public static final String COL_7 = "isRedAlertShown"; // true(0)면
																	// 기존시작,
																	// false(1)면
																	// 최초시작
			public static final String COL_8 = "isVibrateOn"; // true(0)면 진동,
																// false(1)면
																// 진동안함
		}
	}
}

public class DataBaseHelper extends SQLiteOpenHelper {
	public static final String LOG_TAG = "DataBaseHelper";
	private SQLiteDatabase db;

	public DataBaseHelper(Context context) {
		super(context, DataBaseInfo.DB.NAME, null, DataBaseInfo.DB.VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Logs.d(LOG_TAG, "onCreate");
		this.db = db;
		RecordHelper.create(db);
		ThemeHelper.create(db);
		SettingsHelper.create(db);
	}

	static class RecordHelper {
		static void create(SQLiteDatabase db) {
			db.execSQL("create table " + DataBaseInfo.Table.RecordTable.NAME
				+ "("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ DataBaseInfo.Table.RecordTable.COL_1
				+ " INTEGER NOT NULL, " // startTime
				+ DataBaseInfo.Table.RecordTable.COL_2
				+ " INTEGER NOT NULL, " // finishTime
				+ DataBaseInfo.Table.RecordTable.COL_3 + " TEXT NOT NULL);"); // Theme
																				// id
		}

		static String getSQLtoSelectInSecondRange(TimeRange secondRange,
				String themeId) {
			if (themeId == null) {
				return "SELECT * FROM " + DataBaseInfo.Table.RecordTable.NAME
					+ " WHERE " + DataBaseInfo.Table.RecordTable.COL_1
					+ ">=" + secondRange.getStartTimePoint() + " AND "
					+ DataBaseInfo.Table.RecordTable.COL_1 + "<"
					+ secondRange.getFinishTimePoint() + ";";
			} else {
				return "SELECT * FROM " + DataBaseInfo.Table.RecordTable.NAME
					+ " WHERE " + DataBaseInfo.Table.RecordTable.COL_1
					+ ">=" + secondRange.getStartTimePoint() + " AND "
					+ DataBaseInfo.Table.RecordTable.COL_1 + "<"
					+ secondRange.getFinishTimePoint() + " AND "
					+ DataBaseInfo.Table.RecordTable.COL_3 + "=" + themeId
					+ ";";
			}
		}
	}

	static class ThemeHelper {
		static void create(SQLiteDatabase db) {
			db.execSQL("create table "
				+ DataBaseInfo.Table.ThemeTable.NAME
				+ "("
				+ DataBaseInfo.Table.ThemeTable.COL_1
				+ " TEXT PRIMARY KEY, " // id 생성시간기준, 오버플로우 방지하기 위해 TEXT로
										// 선언, String으로 넣을것
				+ DataBaseInfo.Table.ThemeTable.COL_2
				+ " TEXT NOT NULL, " // name
				+ DataBaseInfo.Table.ThemeTable.COL_3
				+ " INTEGER NOT NULL, " // color
				+ DataBaseInfo.Table.ThemeTable.COL_4
				+ " INTEGER NOT NULL);"); // order
		}
	}

	static class SettingsHelper {
		// 테이블 재생성
		static void create(SQLiteDatabase db) {
			db.execSQL("create table "
				+ DataBaseInfo.Table.SettingsTable.NAME
				+ "("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, " // startWith(2일,
															// 1월)
				+ DataBaseInfo.Table.SettingsTable.COL_1
				+ " INTEGER NOT NULL, " // startWith
				+ DataBaseInfo.Table.SettingsTable.COL_2
				+ " INTEGER NOT NULL, " // alarm01
				+ DataBaseInfo.Table.SettingsTable.COL_3
				+ " INTEGER NOT NULL, " // alarm02
				+ DataBaseInfo.Table.SettingsTable.COL_4
				+ " INTEGER NOT NULL, " // alarm selected
				+ DataBaseInfo.Table.SettingsTable.COL_5
				+ " INTEGER NOT NULL, " // 튜토리얼 1 shown
				+ DataBaseInfo.Table.SettingsTable.COL_6
				+ " INTEGER NOT NULL, " // 튜토리얼 2 shown
				+ DataBaseInfo.Table.SettingsTable.COL_7
				+ " INTEGER NOT NULL, " // Red Alert Shown
				+ DataBaseInfo.Table.SettingsTable.COL_8
				+ " INTEGER NOT NULL" // Vibrate On/Off
				+ ");"); // tutorial shown
		}

		// 테이블 삭제
		static void drop(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS "
				+ DataBaseInfo.Table.SettingsTable.NAME + ";");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
		if (old_version < 2) {
			db.execSQL("ALTER TABLE " + DataBaseInfo.Table.SettingsTable.NAME
				+ " ADD " + DataBaseInfo.Table.SettingsTable.COL_8
				+ " INTEGER DEFAULT 0;");
		}
		// addSettingsField(DataBaseInfo.Table.SettingsTable.COL_8,
		// "INTEGER NOT NULL", "0");
	}

	//
	// public void addSettingsField(String columnName, String dataType, String
	// initialValue)
	// {
	// String addFieldQuery =
	// "ALTER TABLE "+DataBaseInfo.Table.SettingsTable.NAME+" ADD "+columnName+" "+dataType+";";
	// db = getWritableDatabase();
	// db.execSQL(addFieldQuery);
	// String updateQuery = "UPDATE " + DataBaseInfo.Table.SettingsTable.NAME +
	// " SET "
	// + columnName + " = " + initialValue
	// + " WHERE id = 0;";
	// db.execSQL(updateQuery);
	// }

	public Cursor getCursor(String sql) {
		db = getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	/**
	 * 특정 Second Range에 속한 Record들을 전부 리턴한다. themeId가 null일 경우 테마 상관없이 가져온다. -
	 * Clean Complete
	 * 
	 * @return
	 */
	public ArrayList<Record> getRecordsInSecondRangeByTheme(
			TimeRange secondRange, String themeId) {
		ArrayList<Record> recordListInSecondRangeByTheme = new ArrayList<Record>();
		String sqltoSelectRecordsInSecondRangeByTheme = RecordHelper
			.getSQLtoSelectInSecondRange(secondRange, themeId);

		Cursor cursor = getCursor(sqltoSelectRecordsInSecondRangeByTheme);
		if (cursor.getCount() < 1) {
			cursor.close();
			return null;
		} else {
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				Record record = new Record(cursor.getInt(1)); // starttime 기준으로
																// 날짜측정
				// record.weekth = DateTimeManager.getWeekth(record);
				// 이거 굳이 메인에서 안 해도 될거같아서 뺌
				record.setFocustime(cursor.getInt(2)); // focustime

				recordListInSecondRangeByTheme.add(record);
				cursor.moveToNext();
			}
			cursor.close();
			return recordListInSecondRangeByTheme;
		}
	}

	/**
	 * 데이터를 삽입할 때는 sec 단위로 삽입한다.
	 * 
	 * @param startTime
	 * @param focusTime
	 */
	public void insertRecord(int startTime, int focusTime, String themeId) {
		String sql = "INSERT INTO " + DataBaseInfo.Table.RecordTable.NAME
			+ " VALUES (null, " + startTime + ", " + focusTime + ", \""
			+ themeId + "\");";
		Record record = new Record(startTime);
		record.setFocustime(focusTime);
		Logs.d("시나리오", "<DB Insert> " + record.toString() + " 집중 시간 = "
			+ record.getFocustime() + " 테마 = " + themeId);
		db = getWritableDatabase();
		db.execSQL(sql);
	}

	public void deleteRecord(int startTime) {
		String sql = "DELETE FROM " + DataBaseInfo.Table.RecordTable.NAME
			+ " WHERE " + DataBaseInfo.Table.RecordTable.COL_1 + "="
			+ startTime + ";";
		Record record = new Record(startTime);
		Logs.d("delete", record.toString());
		db = getWritableDatabase();
		db.execSQL(sql);
	}

	private void deleteRecord(int year, int month, int date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, date, 4, 0, 0);
		int startTimeInSec = (int)(calendar.getTimeInMillis() / 1000);
		int finishTimeInSec = Record.getNextDate(new Record(startTimeInSec)).getTimeInSec();
		String sql = "DELETE FROM " + DataBaseInfo.Table.RecordTable.NAME
			+ " WHERE " + DataBaseInfo.Table.RecordTable.COL_1 + ">="
			+ startTimeInSec + " AND " + DataBaseInfo.Table.RecordTable.COL_1 + "<" + finishTimeInSec + ";";
		db = getWritableDatabase();
		db.execSQL(sql);
	}

	public void deleteHistoryItem(int dataType, int dataCode) {
		switch (dataType) {
			case BaseItem.DTYPE_EACH:
				deleteRecord(dataCode);
				break;
			case BaseItem.DTYPE_DAY:
				deleteRecord(dataCode / 10000, (dataCode / 100) % 100, dataCode % 100);
				break;
		}
	}

	public ArrayList<Record> getRecordListOfAllByTheme(String themeId) {
		ArrayList<Record> list = new ArrayList<Record>();

		String sql = "SELECT * FROM " + DataBaseInfo.Table.RecordTable.NAME
			+ " WHERE " + DataBaseInfo.Table.RecordTable.COL_3 + "=\""
			+ themeId + "\";";
		Cursor cursor = getCursor(sql);
		if (cursor.getCount() >= 1) {
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				Record record = new Record(cursor.getInt(1)); // startTime설정
				record.setFocustime(cursor.getInt(2));

				list.add(record);
				cursor.moveToNext();
			}
		}
		cursor.close();
		return list;
	}

	/**
	 * Theme Control : 모든 것은 id를 기준으로 컨트롤한다.
	 */

	public void insertTheme(Theme theme) {
		String sql = "INSERT INTO " + DataBaseInfo.Table.ThemeTable.NAME
			+ " VALUES(\"" + theme.getId() + "\", \"" + theme.getName()
			+ "\", " + theme.getColor() + ", " + theme.getOrder() + ");";
		Logs.d("insert", theme.toString());
		Logs.d("insert sql", sql);
		db = getWritableDatabase();
		db.execSQL(sql);
	}

	public void deleteTheme(String themeId) {
		String sql = "DELETE FROM " + DataBaseInfo.Table.ThemeTable.NAME
			+ " WHERE " + DataBaseInfo.Table.ThemeTable.COL_1 + "=\""
			+ themeId + "\";";
		Logs.d("delete", "id = " + themeId);
		db = getWritableDatabase();
		db.execSQL(sql);
	}

	/**
	 * 테마 Id로 테마 수정, theme id를 가진 것을 현재 theme 값에 맞게. 그래봐야 수정할 수 있는 건 이름과 색상 정도지만
	 * 
	 * @param theme
	 */
	public void modifyTheme(Theme theme) {
		String sql = "UPDATE " + DataBaseInfo.Table.ThemeTable.NAME + " SET "
			+ DataBaseInfo.Table.ThemeTable.COL_2 + " = \""
			+ theme.getName() + "\", "
			+ DataBaseInfo.Table.ThemeTable.COL_3 + " = "
			+ theme.getColor() + ", " + DataBaseInfo.Table.ThemeTable.COL_4
			+ " = " + theme.getOrder() + " WHERE "
			+ DataBaseInfo.Table.ThemeTable.COL_1 + " = \"" + theme.getId()
			+ "\";";
		db = getWritableDatabase();
		db.execSQL(sql);
	}

	/**
	 * 현재 등록된 모든 테마를 리턴해준다.
	 * 
	 * @return
	 */
	public ArrayList<Theme> getThemesListOfAll() {
		ArrayList<Theme> list = new ArrayList<Theme>();

		String sql = "SELECT * FROM " + DataBaseInfo.Table.ThemeTable.NAME
			+ ";";
		Cursor cursor = getCursor(sql);
		if (cursor.getCount() >= 1) {
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				Theme theme = new Theme(cursor.getString(0), // id
				cursor.getString(1), // name
				cursor.getInt(2), // color
				cursor.getInt(3)); // order

				list.add(theme);
				cursor.moveToNext();
			}
		}
		cursor.close();
		return list;
	}

	public Theme getThemeById(String id) {
		Theme theme = new Theme();
		String sql = "SELECT * FROM " + DataBaseInfo.Table.ThemeTable.NAME
			+ " WHERE " + DataBaseInfo.Table.ThemeTable.COL_1 + " = \""
			+ id + "\";";
		Cursor cursor = getCursor(sql);
		if (cursor.getCount() >= 1) {
			cursor.moveToFirst();
			theme = new Theme(cursor.getString(0), // id
			cursor.getString(1), // name
			cursor.getInt(2), // color
			cursor.getInt(3)); // order
		}
		cursor.close();
		return theme;
	}

	public Theme getThemeByOrder(int order) {
		Theme theme = new Theme();
		String sql = "SELECT * FROM " + DataBaseInfo.Table.ThemeTable.NAME
			+ " WHERE " + DataBaseInfo.Table.ThemeTable.COL_4 + " = "
			+ order + ";";
		Cursor cursor = getCursor(sql);
		if (cursor.getCount() >= 1) {
			cursor.moveToFirst();
			theme = new Theme(cursor.getString(0), // id
			cursor.getString(1), // name
			cursor.getInt(2), // color
			cursor.getInt(3)); // order
		}
		cursor.close();
		return theme;
	}

	// 설정 초기
	public void insertInitSettings() // id는 항상 0
	{
		String sql = "INSERT INTO " + DataBaseInfo.Table.SettingsTable.NAME
			+ " VALUES(0, 1, 15, 30, 0, 1, 1, 1, 0);";
		Logs.d("insert sql", sql);
		db = getWritableDatabase();
		db.execSQL(sql);
	}

	// Settings 수정
	public void modifySettings(Settings s) // id는 항상 0, 레코드는 항상 1개
	{
		String sql = "UPDATE " + DataBaseInfo.Table.SettingsTable.NAME
			+ " SET " + DataBaseInfo.Table.SettingsTable.COL_1 + " = "
			+ s.getStartWith() + ", "
			+ DataBaseInfo.Table.SettingsTable.COL_2 + " = "
			+ s.getAlarm01() + ", "
			+ DataBaseInfo.Table.SettingsTable.COL_3 + " = "
			+ s.getAlarm02() + ", "
			+ DataBaseInfo.Table.SettingsTable.COL_4 + " = "
			+ s.getAlarmSelected() + ", "
			+ DataBaseInfo.Table.SettingsTable.COL_5 + " = "
			+ s.getTutorial01Shown() + ", "
			+ DataBaseInfo.Table.SettingsTable.COL_6 + " = "
			+ s.getTutorial02Shown() + ", "
			+ DataBaseInfo.Table.SettingsTable.COL_7 + " = "
			+ s.getRedAlertShown() + ", "
			+ DataBaseInfo.Table.SettingsTable.COL_8 + " = "
			+ s.getIsVibrateOn() + " WHERE id = 0;";
		db = getWritableDatabase();
		db.execSQL(sql);
	}

	public Settings getSettings() {

		Settings settings = new Settings();
		String sql = "SELECT * FROM " + DataBaseInfo.Table.SettingsTable.NAME
			+ ";";
		Cursor cursor = getCursor(sql);
		Logs.d(LOG_TAG, cursor.toString());
		if (cursor.getCount() >= 1) {
			cursor.moveToFirst();
			settings.set(cursor.getInt(1), // startWith(일2, 월1)
			cursor.getInt(2), // alarm01
			cursor.getInt(3), // alarm02
			cursor.getInt(4), // alarm selected
			cursor.getInt(5), cursor.getInt(6), cursor.getInt(7), // tutorial
																	// shown
			cursor.getInt(8) // vibrate
			);
		}
		cursor.close();
		return settings;
	}

	public void close() {
		db.close();
	}

	/** input Dummy Datas **/
	public void insertDummyDatas(ArrayList<Theme> themeList) {
		// 모든 테마(7개)에
		// 하루 3개씩, 각 8시간 미만의 데이터를 넣는다.
		// 더미 데이터 들어가는 날짜 : 1년 전

		Calendar cal = Calendar.getInstance();

		for (int themeSize = 0; themeSize < themeList.size(); themeSize++) {
			int year = 2010, month = Calendar.OCTOBER, day = 8, hour = 1;
			for (int index = 0; index < 770; index++) {
				cal.set(year, month, day, hour, 0, 0);// 시작 : 1년 전
				Record record = new Record(year, month + 1, day, hour, 0, 0);
				Random rand = new Random();
				int focustime = rand.nextInt(28799) + 1;
				record.setFocustime(focustime);
				Logs.d(Logs.DUMMYTEST, "●더미 : " + themeList.get(themeSize).getName() + ", " + year + "/" + (month + 1)
					+ "/" + day + " " + hour + "시, " + focustime + "초 집중");
				insertRecord(record.getTimeInSec(), focustime, themeList.get(themeSize).getId());
				switch (hour) {
					case 1:
						hour = 9;
						break;
					case 9:
						hour = 17;
						break;
					case 17:
						hour = 1;
						// 이 달의 마지막 날일 경우 다음 달 1일로 ㄱㄱ
						if (day == cal.getActualMaximum(Calendar.DATE)) {
							// 12월일 경우 1월 1일로 ㄱㄱ
							if (month == Calendar.DECEMBER) {
								month = Calendar.JANUARY;
								year++;
							} else
								month++;
							day = 1;
						} else
							day++;
						break;
				}
			}
		}
	}
}
