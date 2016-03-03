package com.realnumworks.focustimer.service;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.DeviceControl;
import com.realnumworks.focustimer.singleton.SensorControl;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.singleton.TimerSingleton;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.settings.Settings;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class StateCheckService extends Service implements Runnable{
	
	private Handler changeScreenTypeHandler = null;
	private Handler showTimerHandler = null;
	private boolean runnable = false;
	
	Settings currentSettings = null;
	Thread mThread;

	
	@Override
	public void onCreate() {
		init();
		super.onCreate();
		mThread = new Thread(this);
		mThread.start();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(!isRunning()){
			runnable = true;
		}
		popupNotification(startId);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void stopForever() {
		synchronized (this) {
			runnable = false;
			Logs.d(Logs.THREADTEST, "StateCheckThread is Gone");
			notify();
		}
	}

	public void setChangeScreenTypeHandler(Handler changeScreenTypeHandler) {
		this.changeScreenTypeHandler = changeScreenTypeHandler;
	}

	public void setShowTimerActivityHandler(Handler sHandler) {
		this.showTimerHandler = sHandler;
	}

	private void init() {
		// 초기 설정을 아직 받아오지 않은 경우, DB에서 초기 설정을 가져온다.
		if (currentSettings == null) {
			DataBaseHelper dbhelper = new DataBaseHelper(DeviceControl.getInstance().getApplicationContext());
			currentSettings = dbhelper.getSettings();
			dbhelper.close();
		}
	}
	
	public void popupNotification(int startId){
		// 노티피케이션 띄우기
		Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.app_name), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, ThreadControlService.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, getText(R.string.app_name), "실행 중입니다", pendingIntent);
		startForeground(startId, notification);
	}

	public boolean isRunning() {
		return runnable;
	}

	@Override
	public void run() {
		while (runnable) {

			updateZstate();

			int zstate = StateSingleton.getInstance().getZstate();
			int tstate = StateSingleton.getInstance().getTstate();
			int pstate = StateSingleton.getInstance().getPstate();

			StateSingleton.getInstance().logStates();

			Logs.d(Logs.SETTINGSTEST, currentSettings.toString());

			switch (zstate) {
				case StateSingleton.ZSTATE_MODE_FACEUP: {
					// FACEUP 일 때
					doTaskOnFaceUp(tstate);
					break;
				}
				case StateSingleton.ZSTATE_MODE_TURNING: {
					// 시작 진동 ON 상태 & 근접 센서가 등록이 안 되어 있으면, 근접 센서 리스너를 등록한다.
					if (currentSettings.isVibrateOn()) {
						enableProximitySensor(true);
					}
					break;
				}
				case StateSingleton.ZSTATE_MODE_FACEDOWN: {
					doTaskOnFaceDown(tstate, pstate);
					break;
				}
				default:
					break;
			}

			try {
				Thread.sleep(50); // 1/20초 sleep
			} catch (InterruptedException e) {
				Logs.d(Logs.DEBUG, "Sleep error : " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void doTaskOnFaceUp(int tstate) {
		// 진동 설정 On일 경우
		if (!currentSettings.isVibrateOn()) {
			enableProximitySensor(false); // 가속도 센서 unregistered
		}

		switch (tstate) {
			case StateSingleton.TSTATE_MODE_FOCUSING:
				Log.d("StartLapping",TimerSingleton.getInstance().getLapCount()+"");
				if (TimerSingleton.getInstance().getLapCount() > 0) {
					startLapping(); // 재집중 가능 횟수가 MAX보다 적으면
				} else {
					startResult(); // 재집중이 불가능하면
				}
				break;
		}
	}

	private void doTaskOnFaceDown(int tstate, int pstate) {
		switch (tstate) {
		// MAIN
			case StateSingleton.TSTATE_MODE_MAIN:
				switch (pstate) {
					case StateSingleton.PSTATE_MODE_CLOSE:
					case -1:
						// tstate -> FOCUSING
						StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_FOCUSING);
						// 스크린 최초 팝업
						showScreen();
						// Screen Type을 Focusing으로 정하고
						setScreenType(StateSingleton.TSTATE_MODE_FOCUSING);
						// 최초 집중 : 모든 변수를 초기화하고 집중 시작(집중 시작 시간 저장)
						TimerSingleton.getInstance().startFocusing();
						// Lap Count ++
						TimerSingleton.getInstance().plusLapCount();
						break;
				}
				break;
			// FOCUSING
			case StateSingleton.TSTATE_MODE_FOCUSING:
				switch (pstate) {
				// AWAY
					case StateSingleton.PSTATE_MODE_AWAY:
						Log.d("StartLapping",TimerSingleton.getInstance().getLapCount()+"");
						if (TimerSingleton.getInstance().getLapCount() > 0) {
							startLapping(); // 재집중 가능 횟수가 MAX보다 적으면
						} else {
							startResult(); // 재집중이 불가능하면
						}
						break;
				}
				break;
			// LAPPING
			case StateSingleton.TSTATE_MODE_LAPPINGRESULT:
				switch (pstate) {
					case StateSingleton.PSTATE_MODE_CLOSE:
					case -1:
						restartFocusing();
						break;
				}
				break;
		}
	}

	private void enableProximitySensor(boolean enable) {
		if (false == enable) {
			StateSingleton.getInstance().setPstate(-1);
		}
		SensorControl.getInstance().enableListener(Sensor.TYPE_PROXIMITY, enable);
	}

	/**
	 * zRate 값을 통해 zState를 update해준다.
	 */
	private void updateZstate() {
		// zRate 계산
		float zRate = StateSingleton.getInstance().getZRate();

		Logs.d(Logs.CONTINUOUS, "zRate = " + (float)((int)(zRate * 100)) / 100 + ", "
			+ StateSingleton.getInstance().getZString() + ", " + StateSingleton.getInstance().getPString());

		if (zRate > 1) {
			StateSingleton.getInstance().setZstate(StateSingleton.ZSTATE_MODE_FACEUP); // 바로 있을때
		} else if (zRate > -8) {
			StateSingleton.getInstance().setZstate(StateSingleton.ZSTATE_MODE_TURNING); // 터닝
		} else {
			StateSingleton.getInstance().setZstate(StateSingleton.ZSTATE_MODE_FACEDOWN); // 뒤집혔을 때
		}
	}

	/**
	 * LAPPING SCREEN, FOCUSING SCREEN, RESULT SCREEN 중에서 하나로 변경한다.
	 */
	private void setScreenType(int currentTstate) {
		Logs.d(Logs.MAIN_THREAD_CONFIRM, "setScreenType - " + changeScreenTypeHandler);
		while (changeScreenTypeHandler == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		changeScreenTypeHandler.sendEmptyMessage(currentTstate);
	}

	/**
	 * TimerActivity를 popup 시켜준다.
	 */
	private void showScreen() {
		while (showTimerHandler == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		showTimerHandler.sendEmptyMessage(0);
	}

	/**
	 * 측정 재시작
	 */
	private void restartFocusing() {
		// 진동설정이 On으로 되어 있다면
		if (currentSettings.isVibrateOn()) {
			DeviceControl.getInstance().vibrate(400); // 0.4s 진동
		}
		// Stop 15sec Timer
		if (TimerSingleton.getInstance().getIsLapping()) {
			TimerSingleton.getInstance().finishFifteenTimer();
			long time = TimerSingleton.getInstance().getLappingTime();
			Log.e("LAPPING TIME",time+"");
		}
		// Add Lap Count
		TimerSingleton.getInstance().plusLapCount();
		// Change tstate to FOCUSING
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_FOCUSING);
		// Change Screen to Focusing
		setScreenType(StateSingleton.TSTATE_MODE_FOCUSING);
	}

	/**
	 * Lapping 관련 세팅
	 */
	private void startLapping() {
		// tstate -> LAPPINGRESULT
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_LAPPINGRESULT);
		// Change Screen to Lapping
		setScreenType(StateSingleton.TSTATE_MODE_LAPPINGRESULT);
		// //Save CurrentTime info FinishedTime
		// TimerSingleton.getInstance().setFinishTime();
		// Start 15 Timer
		TimerSingleton.getInstance().startFifteenTimer();
	}

	/**
	 * Result 관련 세팅
	 */
	private void startResult() {
		// tstate -> RESULT
		StateSingleton.getInstance().setTstate(StateSingleton.TSTATE_MODE_RESULT);
		// Change Screen to RESULT
		setScreenType(StateSingleton.TSTATE_MODE_RESULT);
		// //Save CurrentTime info FinishedTime
		// TimerSingleton.getInstance().setFinishTime();
	}
}
