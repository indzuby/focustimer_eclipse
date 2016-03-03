package com.realnumworks.focustimer.view.history;

import android.view.View;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;

public class ItemViewHolder extends BaseViewHolder {

	//Item
	private TypefaceTextView tv_title, tv_text, tv_bigText;

	/**
	 * Adapter에서 findView를 통해 자식 뷰를 초기화한 후에 ViewHolder를 생성해야 함.
	 * @param base
	 */
	public ItemViewHolder(View base) {
		super(base);
	}

	@Override
	public void findViews() {
		tv_title = (TypefaceTextView)baseView
			.findViewById(R.id.tv_record_title);
		tv_text = (TypefaceTextView)baseView
			.findViewById(R.id.tv_record_text);
		tv_bigText = (TypefaceTextView)baseView
			.findViewById(R.id.tv_record_bigtext);
	}

	public void setTextInItem(ItemString itemString) {
		tv_title.setText(itemString.getTitle());
		tv_text.setText(itemString.getText());
		tv_bigText.setText(itemString.getBigText());
	}
	//
	//	/**
	//	 * 10월 4일 (금)
	//	 * @param record
	//	 */
	//	private void setTextInTitle(Record record) {
	//		String textInDateDay =
	//				(record.getMonth() + 1) + "월 "
	//					+ record.getDate() + "일"
	//					+ "(" + record.getDayString() + ")";
	//		tv_title.setText(textInDateDay);
	//	}
	//
	//	/**
	//	 * 오후 7:45
	//	 * @param record
	//	 */
	//	private void setTextInText() {
	//
	//		String textInTime = DateTime.getAMPMString(record.getHour(), record.getMin());
	//		tv_text.setText(textInTime);
	//	}
	//
	//	/**
	//	 * 0시간 0분 또는 0분 0초
	//	 * @param record
	//	 */
	//	private void setTextInBigText(Record record) {
	//		String textInFocusTime =
	//				DateTime.getTimeMinString(DateTime.getHourMinSecFromTimeSec(record.getFocustime()));
	//		tv_bigText.setText(textInFocusTime);
	//	}
}
