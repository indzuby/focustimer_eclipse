package com.realnumworks.focustimer.view.tutorial;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.view.settings.Settings;

public class TutorialHelper {

	private Activity activity;
	private DataBaseHelper dbm;
	private Settings set;

	public TutorialHelper(Activity activity, DataBaseHelper dbm, Settings s){
		this.activity = activity;
		this.dbm = dbm;
		this.set = s;
	}

	public static void popupTutorial01(Activity activity) {
		Intent intent = new Intent(activity, TutorialActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}

	public static void popupTutorial02(Activity activity) {
		Intent intent = new Intent(activity, Tutorial02Activity.class);
		activity.startActivity(intent);
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	public void setTutorial03()
	{
		RelativeLayout layout_redtoast = (RelativeLayout)activity.findViewById(R.id.layout_main_redtoast);
		ImageButton btn_redtoast_close = (ImageButton)activity.findViewById(R.id.imgbtn_main_redtoastclose);
		if(set.isRedAlertShown())	//보여준적이 있으면, 없앤다.
		{
			layout_redtoast.setVisibility(View.GONE);			
		}
		else	//보여준적이 없으면, 생긴다.
		{
			layout_redtoast.setVisibility(View.VISIBLE);						
			btn_redtoast_close.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					set.setRedAlertShown(true);
					dbm.modifySettings(set);
					setTutorial03();
				}
			});
		}
		layout_redtoast.bringToFront();
	}
}
