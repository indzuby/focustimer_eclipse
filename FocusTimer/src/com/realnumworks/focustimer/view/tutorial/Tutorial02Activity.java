package com.realnumworks.focustimer.view.tutorial;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.view.settings.Settings;

public class Tutorial02Activity extends Activity {
	// private Tracker mTracker;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_tutorial02);
		context = this;

		ImageButton closeBtn = (ImageButton) findViewById(R.id.imgbtn_tutorial02_close);
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DataBaseHelper dbm = new DataBaseHelper(context);
				Settings s = dbm.getSettings();
				s.setTutorial02Shown(true);
				dbm.modifySettings(s);
				dbm.close();
				finish();
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
							System.exit(0); // 시스템 종료
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
