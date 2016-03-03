package com.realnumworks.focustimer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.DeviceControl;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.singleton.TimerSingleton;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.history.Record;

public class PowerOffEventReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String intentType = intent.getAction();
		if (intentType.equals(Intent.ACTION_SHUTDOWN)) {
			int startTime = (int)TimerSingleton.getInstance().getStartTime();
			int focusTime = TimerSingleton.getInstance().getFocusTimeInSec();
			String themeId = StateSingleton.getInstance().currentThemeId;

			if (focusTime >= 3) {
				DataBaseHelper dbm = new DataBaseHelper(DeviceControl.getInstance().getApplicationContext());
				dbm.insertRecord(startTime, focusTime, themeId);
				StateSingleton.getInstance().lastFocusedTimeInSec = startTime + focusTime;
				Logs.d(Logs.SHUTDOWNTEST, "Power Off, Focusing Time Saved : "
					+ new Record(focusTime).toShortTimeString());
				// StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_MAIN);
			}
		}
	}
}
