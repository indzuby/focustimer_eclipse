package com.realnumworks.focustimer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.Logs;

public class ScreenOnOffReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			Logs.d(Logs.SCREENONOFFTEST, "Screen OFF");
			// 메인화면에 있을때 화면이 꺼지면, OTHER모드로 바꿔준다. (뒤집어도 타이머 측정 안됨)
			if (StateSingleton.getInstance().getTstate() == StateSingleton.TSTATE_MODE_MAIN) {
				StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_OTHER);
			}
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Logs.d(Logs.SCREENONOFFTEST, "Screen ON");
		}
	}
}
