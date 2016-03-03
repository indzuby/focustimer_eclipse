package com.realnumworks.focustimer.view.history;

import android.view.View;

public class BaseViewHolder {

	protected View baseView; //Adapter의 convertView

	public BaseViewHolder(View base) {
		this.baseView = base;
		findViews();
	}

	public View getBaseView() {
		return baseView;
	}

	public void findViews() {
	}
}
