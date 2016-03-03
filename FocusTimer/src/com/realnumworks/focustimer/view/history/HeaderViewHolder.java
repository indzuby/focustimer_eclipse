package com.realnumworks.focustimer.view.history;

import android.view.View;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;

public class HeaderViewHolder extends BaseViewHolder {

	//Header
	private TypefaceTextView tv_header;

	public HeaderViewHolder(View base) {
		super(base);
	}

	@Override
	public void findViews() {
		tv_header = (TypefaceTextView)baseView.findViewById(R.id.tv_record_header);
	}

	public void setTextInHeader(ItemString itemString) {
		tv_header.setText(itemString.getHeader());
	}

	//	/**
	//	 * Weekth를 받아서 header textview에 String 형식으로 출력
	//	 * @param thisWeekth
	//	 */
	//	public void setTextInHeader(Weekth thisWeekth)	{
	//		String headerString =
	//				(thisWeekth.getMonth() + 1) + "월 "
	//				+ thisWeekth.getWeekth() + "주차, "
	//				+ thisWeekth.getYear() + "년";
	//		tv_header.setText(headerString);
	//	}
}
