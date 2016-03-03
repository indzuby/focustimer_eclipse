package com.realnumworks.focustimer.view.chart;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TableLayout.LayoutParams;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.utils.DrawableHelper;

public class ChartActivity extends ActionBarActivity {

	// private Tracker mTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_chart);

		setActionBar();

	}

	public void setActionBar() {
		setTheme(R.style.NoActionBarShadowTheme); // 액션바 그림자 제거
		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(DrawableHelper.getDrawable(this,
				R.drawable.actionbar_gradient));

		View v = getLayoutInflater().inflate(R.layout.actionbar_chart, null);
		actionBar.setCustomView(v, new ActionBar.LayoutParams(new LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT)));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// String name = "차트 화면";
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
}
