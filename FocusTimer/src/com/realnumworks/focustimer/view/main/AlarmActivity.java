package com.realnumworks.focustimer.view.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.view.settings.Settings;

public class AlarmActivity extends Activity
{
	TypefaceTextView closeButton, noneButton, alarm01Button, alarm02Button;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBackgroundToDark();
		setContentView(R.layout.activity_alarm);
		
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_OTHER);
		
		setComponents();
		initializeButtonDatas();
		setButtonOnclickListeners();
	}
	
	private void setBackgroundToDark()
	{
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		params.dimAmount = 0.7f;
		getWindow().setAttributes(params);
	}
	
	private void setComponents()
	{
		closeButton = (TypefaceTextView)findViewById(R.id.btn_alarm_close);
		noneButton = (TypefaceTextView)findViewById(R.id.btn_alarm_none);
		alarm01Button = (TypefaceTextView)findViewById(R.id.btn_alarm_01);
		alarm02Button = (TypefaceTextView)findViewById(R.id.btn_alarm_02);
	}
	
	private void initializeButtonDatas()
	{
		DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
		Settings currentSettings = dataBaseHelper.getSettings();
		
		//Text Set
		alarm01Button.setText(currentSettings.getAlarm01()+"분");
		alarm02Button.setText(currentSettings.getAlarm02()+"분");
		
		//Selected button set
		int selectedIndex = currentSettings.getAlarmSelected();
		setSelectedAlarm(selectedIndex);
		dataBaseHelper.close();
	}
	
	private void setButtonOnclickListeners()
	{
		View.OnClickListener alarmButtonListener = new OnClickListener() {
			@Override
			public void onClick(View clickedView) {
				switch(clickedView.getId())
				{
					case R.id.btn_alarm_close:
						break;
					case R.id.btn_alarm_none:
						setSelectedAlarm(0);
						break;
					case R.id.btn_alarm_01:
						setSelectedAlarm(1);
						break;
					case R.id.btn_alarm_02:
						setSelectedAlarm(2);
						break;
				}
				finish();
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			}
		};
		
		closeButton.setOnClickListener(alarmButtonListener);
		noneButton.setOnClickListener(alarmButtonListener);
		alarm01Button.setOnClickListener(alarmButtonListener);
		alarm02Button.setOnClickListener(alarmButtonListener);
	}
	
	@SuppressLint("ResourceAsColor")
	private void setSelectedAlarm(int index)
	{
		DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
		Settings currentSettings = dataBaseHelper.getSettings();

		noneButton.setSelected(false);
		alarm01Button.setSelected(false);
		alarm02Button.setSelected(false);
		
		switch(index)
		{
			case 0:	//none
				noneButton.setSelected(true);
				break;
			case 1:	//1
				alarm01Button.setSelected(true);
				break;
			case 2:	//2
				alarm02Button.setSelected(true);
				break;
		}

		currentSettings.setAlarmSelected(index);
		dataBaseHelper.modifySettings(currentSettings);
	}
	
	@Override
	public void finish() {
		setResult(RESULT_OK, getIntent());
		super.finish();
	}
}
