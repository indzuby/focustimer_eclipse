package com.realnumworks.focustimer.view.tutorial;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.DrawableHelper;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.settings.Settings;
import com.viewpagerindicator.CirclePageIndicator;

public class TutorialActivity extends Activity {
	// private Tracker mTracker;

	// Declare Variables
	DataBaseHelper dbhelper;
	ViewPager viewPager;
	TutorialPagerAdapter adapter;
	Drawable[] backgroundImgs;
	String[] tutorialStrings;
	CirclePageIndicator mIndicator;
	Settings currentSettings;

	TypefaceTextView prevBtn, nextBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		bringCurrentSettingsFromDB();
		
		// Get the view
		setContentView(R.layout.activity_tutorial);

		StateSingleton.getInstance()
			.setTstate(StateSingleton.TSTATE_MODE_OTHER); // TSTATE를 OTHER로

		// Generate Initial data
		setButtons();
		setBackgroundImgsToArray();
		setTutorialStringsToArray();

		// Locate the ViewPager in viewpager_main.xml
		viewPager = (ViewPager)findViewById(R.id.tutorial_pager);
		viewPager.setCurrentItem(0);

		// Pass results to ViewPagerAdapter Class
		adapter = new TutorialPagerAdapter(TutorialActivity.this,
			backgroundImgs, tutorialStrings, this);
		// Binds the Adapter to the ViewPager
		viewPager.setAdapter(adapter);

		// ViewPager Indicator
		mIndicator = (CirclePageIndicator)findViewById(R.id.indicator_tutorial);

		// Set when Page Changed, prev/next button visibility
		mIndicator
			.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {

					Logs.d(Logs.TUTORIAL, "onPageSelected called : "
						+ position);

					if (position == 0) {
						prevBtn.setVisibility(View.GONE);
						nextBtn.setVisibility(View.VISIBLE);
					} else if (position == adapter.getCount() - 1) {
						prevBtn.setVisibility(View.VISIBLE);
						nextBtn.setVisibility(View.GONE);
					} else {
						prevBtn.setVisibility(View.VISIBLE);
						nextBtn.setVisibility(View.VISIBLE);
					}
				}

				@Override
				public void onPageScrolled(int position,
						float positionOffset, int positionOffsetPixels) {
				}

				@Override
				public void onPageScrollStateChanged(int state) {
				}
			});

		mIndicator.setViewPager(viewPager);

	}

	public void bringCurrentSettingsFromDB() {
		dbhelper = new DataBaseHelper(this);
		currentSettings = dbhelper.getSettings();
		dbhelper.close();
	}

	public void setBackgroundImgsToArray() {
		backgroundImgs = new Drawable[4];
		backgroundImgs[0] = DrawableHelper.getDrawable(this,
			R.drawable.img_tutorial_01_3x);
		backgroundImgs[1] = DrawableHelper.getDrawable(this,
			R.drawable.img_tutorial_02_3x);
		backgroundImgs[2] = DrawableHelper.getDrawable(this,
			R.drawable.img_tutorial_03_3x);
		backgroundImgs[3] = DrawableHelper.getDrawable(this,
			R.drawable.img_tutorial_04_3x);
	}

	public void setTutorialStringsToArray() {
		tutorialStrings = new String[4];
		tutorialStrings[0] = "진짜 집중한 시간만\n측정해보세요.";
		tutorialStrings[1] = "휴대폰을 뒤집으면,";
		tutorialStrings[2] = "타이머가 시작됩니다";
		tutorialStrings[3] = "";
	}

	public void setButtons() {
		prevBtn = (TypefaceTextView)findViewById(R.id.btn_tutorial_prev);
		nextBtn = (TypefaceTextView)findViewById(R.id.btn_tutorial_next);

		prevBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
			}
		});

		nextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) // 뒤로가기 키를 눌렀을 때, 알림창
															// 띄우고 종료
	{

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);

			alertDlg.setTitle("종료하시겠습니까?");
			alertDlg.setPositiveButton("예",
				new DialogInterface.OnClickListener() { // 확인 버튼
					@Override
					public void onClick(DialogInterface dialog,
							int whichButton) {
						moveTaskToBack(true);
						finish();
						//							System.exit(0); // 시스템 종료
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				});
			alertDlg.setNegativeButton("아니오",
				new DialogInterface.OnClickListener() { // 취소 버튼
					@Override
					public void onClick(DialogInterface dialog,
							int whichButton) {

						dialog.cancel();
					}
				});
			AlertDialog alert = alertDlg.create();
			alert.show();
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// String name = getClass().getSimpleName();
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
}
