package com.realnumworks.focustimer.view.tutorial;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.settings.Settings;

public class TutorialPagerAdapter extends PagerAdapter {
	// Declare Variables
	Context context;
	Drawable[] backgroundImgs;
	String[] tutorialStrings;
	LayoutInflater inflater;
	ImageButton startBtn;
	Activity superActivity;

	public TutorialPagerAdapter(Context context, Drawable[] backgroundImgs,
			String[] tutorialStrings, Activity superActivity) {
		this.context = context;
		this.backgroundImgs = backgroundImgs;
		this.tutorialStrings = tutorialStrings;
		this.superActivity = superActivity;
	}

	@Override
	public int getCount() {
		return tutorialStrings.length;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		// Declare Variables
		RelativeLayout layoutBackground;
		TypefaceTextView tvString;

		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.tutorial_item, container,
				false);

		// Locate the Components
		layoutBackground = (RelativeLayout) itemView
				.findViewById(R.id.layout_tutorial_bg);
		tvString = (TypefaceTextView) itemView.findViewById(R.id.tv_tutorial);
		startBtn = (ImageButton) itemView.findViewById(R.id.btn_tutorial_start);

		// Capture position and set to the Views
		tvString.setText(tutorialStrings[position]);
		layoutBackground.setBackground(backgroundImgs[position]);

		if (position < getCount() - 1) {
			startBtn.setVisibility(View.GONE);
		}

		// Set Listener at the Button of Last Page
		startBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DataBaseHelper dbhelper = new DataBaseHelper(context);
				Settings s = dbhelper.getSettings();
				s.setTutorial01Shown(true);
				dbhelper.modifySettings(s);
				Logs.d(Logs.TUTORIAL, "Settings : " + s.toString());
				TutorialHelper.popupTutorial02(superActivity);
				superActivity.finish();
			}
		});

		// Add viewpager_item.xml to ViewPager
		container.addView(itemView);

		return itemView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// Remove viewpager_item.xml from ViewPager
		container.removeView((RelativeLayout) object);

	}
}
