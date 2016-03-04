package com.realnumworks.focustimer.thread;

import android.hardware.Sensor;
import android.os.Handler;

import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.DeviceControl;
import com.realnumworks.focustimer.singleton.SensorControl;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.singleton.TimerSingleton;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.settings.Settings;

/**
 * 현재는 0.1초에 한번씩 동작하도록 설계되어 있음.
 * 부분적 코드 정리 
 * @author Yedam, birdea
 * 
 */
public class StateCheckThread extends Thread {

	private Handler changeScreenTypeHandler = null;
	private Handler showTimerHandler = null;
	private boolean runnable = false;

	Settings currentSettings = null;

	// 생성자
	public StateCheckThread() {
		init();
	}

	public void updateCurrentSettings(Settings currentSettings) {
		this.currentSettings = currentSettings;
	}

	@Override
	public synchronized void start() {
		super.start();
		runnable = true;
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
					Logs.d("TURNING",currentSettings.isVibrateOn()+"");
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
			enableProximitySensor(false); // 근 센서 unregistered
		}else 
			enableProximitySensor(true); // 근 센서 unregistered
		switch (tstate) {
			case StateSingleton.TSTATE_MODE_FOCUSING:
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
						if (TimerSingleton.getInstance().getLapCount() >0) {
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

	@Override
	protected StateCheckThread clone() {
		StateCheckThread newThread = new StateCheckThread();
		newThread.setShowTimerActivityHandler(showTimerHandler);
		newThread.setChangeScreenTypeHandler(changeScreenTypeHandler);
		return newThread;
	}
}
