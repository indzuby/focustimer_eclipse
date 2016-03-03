package com.realnumworks.focustimer.view.main;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.utils.Logs;
import com.viewpagerindicator.PageIndicator;

public abstract class MainViewPagerActivity extends FragmentActivity {

	MainFragmentAdapter mAdapter;
	ViewPager mPager;
	PageIndicator mIndicator;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		Logs.d("startTest", "BaseViewPagerActivity.onCreateOptionsMenu");
		return true;
	}

	public void addFragmentPage() {
		Logs.d("startTest", "BaseViewPagerActivity.addFragmentPage : "
				+ mAdapter.getCount() + " -> " + (mAdapter.getCount() + 1));
		if (mAdapter.getCount() <= 7) {
			mIndicator.notifyDataSetChanged();
		}
	}

	public void removeFragmentPage() {
		Logs.d("startTest", "BaseViewPagerActivity.removeFragmentPage : "
				+ mAdapter.getCount() + " -> " + (mAdapter.getCount() - 1));
		if (mAdapter.getCount() >= 0) {
			mIndicator.notifyDataSetChanged();
		}
	}
}