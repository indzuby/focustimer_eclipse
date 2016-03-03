package com.realnumworks.focustimer.singleton;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.Window;
import android.view.WindowManager;

import com.realnumworks.focustimer.utils.Logs;

/**
 * SingleTon Pattern으로 짜여짐 화면 ON/OFF를 함께 다루는 클래스
 * 
 * @author Yedam
 * 
 */

public class DeviceControl {

	private Context applicationContext;

	private PowerManager powermanager;
	private PowerManager.WakeLock wakeLock; // 화면이 켜진 상태를 유지하는 wakelock
	// private float currentScreenBrightness = -1;
	KeyguardManager km;
	KeyguardManager.KeyguardLock keyLock;

	public final boolean isDraggging = false;

	private DeviceControl() {
	}

	private static DeviceControl instance = new DeviceControl();

	public static DeviceControl getInstance() {
		return instance;
	}

	/**
	 * Vibrate에 필요한 Context 설정(필수)
	 * 
	 * @param context
	 */
	public void setApplicationContext(Context context) {
		this.applicationContext = context;
	}

	public Context getApplicationContext() {
		return applicationContext;
	}

	public void setScreenOnWakeLock() {
		powermanager = (PowerManager)applicationContext.getSystemService(Context.POWER_SERVICE);
		wakeLock = powermanager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Screen Control");
	}

	public void acquireScreenOnWakeLock() {
		if (wakeLock == null) {
			setScreenOnWakeLock();
		}
		if (!wakeLock.isHeld())
			wakeLock.acquire();
	}

	public void releaseScreenOnWakeLock() {
		if (wakeLock == null) {
			setScreenOnWakeLock();
		}
		if (wakeLock.isHeld())
			wakeLock.release();
	}

	public void vibrate(int millis) {
		Vibrator vibrator = (Vibrator)applicationContext.getSystemService(Service.VIBRATOR_SERVICE);
		vibrator.vibrate(millis);
		Logs.d(Logs.VIBRATETEST, "vibrate " + millis);
	}

	/**
	 * 화면을 dim 시킨다.
	 */
	public void dimScreen(Activity activity) {
		Window window = activity.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = 0f;
		window.setAttributes(lp);
		Logs.d("dim", "dim activity : " + activity);
	}

	public void undimScreen(Activity activity) {
		Window window = activity.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = 1f;
		window.setAttributes(lp);
		Logs.d("dim", "undim");
	}

	public void disableLockScreen() {
		km = (KeyguardManager)applicationContext.getSystemService(Service.KEYGUARD_SERVICE);
		keyLock = km.newKeyguardLock(Service.KEYGUARD_SERVICE);
		keyLock.disableKeyguard();
	}

	public void enableLockScreen() {
		km = (KeyguardManager)applicationContext.getSystemService(Service.KEYGUARD_SERVICE);
		keyLock = km.newKeyguardLock(Service.KEYGUARD_SERVICE);
		keyLock.reenableKeyguard();
	}

	public int getDeviceScreenWidth() {
		return applicationContext.getResources().getDisplayMetrics().widthPixels;
	}

	public int getDeviceScreenHeight() {
		return applicationContext.getResources().getDisplayMetrics().heightPixels;
	}

	public float getDeviceDpi() {
		return applicationContext.getResources().getDisplayMetrics().densityDpi;
	}

	public float getDeviceDensity() {
		return getDeviceDpi() / 160;
	}

	public float px2dp(int px) {
		return px / getDeviceDensity();
	}

	public float dp2px(int dp) {
		return dp * getDeviceDensity();
	}

	public static boolean isThreadRunning = true;

}
