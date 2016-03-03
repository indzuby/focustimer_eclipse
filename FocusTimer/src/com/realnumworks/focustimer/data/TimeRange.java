package com.realnumworks.focustimer.data;

import com.realnumworks.focustimer.view.history.Record;

/**
 * 시작 초와 끝 초를 포함하는 "범위" 객체. 시작 초 <= 범위 < 끝 초이다.
 * 
 * @author Yedam
 *
 */
public class TimeRange {
	private int startTimePoint;
	private int finishTimePoint;

	public void setStartTimePoint(int startPoint) {
		this.startTimePoint = startPoint;
	}

	public void setFinishTimePoint(int finishPoint) {
		this.finishTimePoint = finishPoint;
	}

	public int getStartTimePoint() {
		return startTimePoint;
	}

	public int getFinishTimePoint() {
		return finishTimePoint;
	}

	public TimeRange(int startPoint, int finishPoint) {
		this.startTimePoint = startPoint;
		this.finishTimePoint = finishPoint;
	}

	@Override
	public TimeRange clone() {
		return new TimeRange(startTimePoint, finishTimePoint);
	}

	@Override
	public String toString() {
		return "Range : " + startTimePoint + "~" + finishTimePoint;
	}

	public String toDateString() {
		return "Range : " + new Record(startTimePoint).toShortString() + "~"
			+ new Record(finishTimePoint).toShortString();
	}
}
