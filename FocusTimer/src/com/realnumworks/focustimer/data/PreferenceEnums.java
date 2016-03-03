package com.realnumworks.focustimer.data;

public class PreferenceEnums {

	public enum StartDay {
		MONDAY("MONDAY"),
		TUESDAY("TUESDAY"),
		WEDNESDAY("WEDNESDAY"),
		THURSDAY("THURSDAY"),
		FRIDAY("FRIDAY"),
		SATURDAY("SATURDAY"),
		SUNDAY("SUNDAY"), ;

		private final String tag;

		StartDay(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return tag;
		}

		// default valus is MONDAY
		public static StartDay fromValue(int val) {
			for (StartDay day : StartDay.values()) {
				if (day.ordinal() == val) {
					return day;
				}
			}
			return MONDAY;
		}
	}

	public enum StartVibrate {
		ON,
		OFF
	}

	public enum StartTimeAlarm1 {
		MIN_5("5분", 60 * 5),
		MIN_10("10분", 60 * 10),
		MIN_15("15분", 60 * 15),
		MIN_20("20분", 60 * 20),
		MIN_25("25분", 60 * 25),
		MIN_30("30분", 60 * 30),
		MIN_35("35분", 60 * 35),
		MIN_40("40분", 60 * 40),
		MIN_45("45분", 60 * 45),
		MIN_50("50분", 60 * 50), ;

		private final String tag;
		private final long time_sec;

		StartTimeAlarm1(String tag, long time_sec) {
			this.tag = tag;
			this.time_sec = time_sec;
		}

		public String getTag() {
			return tag;
		}

		public long getTimeAsSecond() {
			return time_sec;
		}
	}

	public enum StartTimeAlarm2 {
		MIN_10("10분", 60 * 10),
		MIN_20("20분", 60 * 20),
		MIN_30("30분", 60 * 30),
		MIN_40("40분", 60 * 40),
		MIN_50("50분", 60 * 50),
		MIN_60("60분", 60 * 60),
		MIN_70("70분", 60 * 70),
		MIN_80("80분", 60 * 80),
		MIN_90("90분", 60 * 90), ;

		private final String tag;
		private final long time_sec;

		StartTimeAlarm2(String tag, long time_sec) {
			this.tag = tag;
			this.time_sec = time_sec;
		}

		public String getTag() {
			return tag;
		}

		public long getTimeAsSecond() {
			return time_sec;
		}
	}
}
