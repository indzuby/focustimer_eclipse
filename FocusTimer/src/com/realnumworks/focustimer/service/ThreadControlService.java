package com.realnumworks.focustimer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.Logs;

public class ThreadControlService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();

		Logs.d(Logs.SERVICE, "Service.onCreate()");

		// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		//
	}

	/**
	 * startService() 호출 후 서비스가 시작되는 시점
	 */
	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Logs.d(Logs.SERVICE, "onStartCommand()");


		// StateCheckThread, TimeThread 시작
		if (StateSingleton.getInstance().stateCheckThread != null
			&& !StateSingleton.getInstance().stateCheckThread.isRunning()) {
			StateSingleton.getInstance().stateCheckThread.start();
		}
		if (StateSingleton.getInstance().timerThread != null && !StateSingleton.getInstance().timerThread.isRunning()) {
			StateSingleton.getInstance().timerThread.start();
		}

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		// StateCheckThread 종료
		// TimeThread 종료
		StateSingleton.getInstance().stateCheckThread.stopForever();
		StateSingleton.getInstance().timerThread.stopForever();
	}
	
}
