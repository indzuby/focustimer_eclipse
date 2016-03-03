package com.realnumworks.focustimer.singleton;

import java.util.Locale;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.realnumworks.focustimer.utils.Logs;

/**
 * SingleTon Pattern으로 짜여짐 센서 초기화, 리스너 등록 및 해제, 가속도 센서와 리스너 관리
 * 부분적 코드 정리
 * @author Yedam, birdea
 * @see http://egloos.zum.com/oniondev/v/9667598
 */
public class SensorControl {

	/* [TODO]
	 * -SingleTon Pattern 의 생성자는 private -> 타 SingleTon class들도 수정 완료
	 * -Sensor 전역변수를 유지할 필요는 없어보임
	 * -Sensor 리스너의 reg/unreg 작업을 반복하는 것 보다는 Enable 분기 처리가 낳음
	 * -init / release 호출 시점에 대한 고민 필요
	 */

	private SensorManager mSensorManager;
	private SensorEventListener mSensorEventListener;
	private boolean isEnableAccelerometer;
	private boolean isEnableProximity;

	// Construct should be private type as singleton-pattern
	private SensorControl() {
	}

	private static SensorControl instance;

	public static SensorControl getInstance() {
		synchronized (SensorControl.class) {
			if (instance == null) {
				instance = new SensorControl();
			}
		}
		return instance;
	}

	public void init(Context context) {

		mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

		setOnSensorEventListener();

		isEnableAccelerometer = false;
		isEnableProximity = false;

		registerListener(Sensor.TYPE_ACCELEROMETER);
		registerListener(Sensor.TYPE_PROXIMITY);

		enableListener(Sensor.TYPE_ACCELEROMETER, true);
		enableListener(Sensor.TYPE_PROXIMITY, false);
	}

	public void release() {

		unregisterListener(Sensor.TYPE_ACCELEROMETER);
		unregisterListener(Sensor.TYPE_PROXIMITY);

		enableListener(Sensor.TYPE_ACCELEROMETER, false);
		enableListener(Sensor.TYPE_PROXIMITY, false);
	}

	public void enableListener(int type, boolean enable) {
		switch (type) {
			case Sensor.TYPE_ACCELEROMETER:
				isEnableAccelerometer = enable;
				break;
			case Sensor.TYPE_PROXIMITY:
				//				Logs.d("Proximity Sensor : "+((enable == true)?"enabled":"disabled"));
				isEnableProximity = enable;
				break;
		}
	}

	private void registerListener(int type) {
		Sensor sensor = mSensorManager.getDefaultSensor(type);
		if (sensor == null) {
			return;
		}
		Logs.d(Logs.SENSORTEST, "[IN] " + sensor.toString());
		mSensorManager.registerListener(mSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL); // 가속도 리스너 등록
	}

	private void unregisterListener(int type) {
		Sensor sensor = mSensorManager.getDefaultSensor(type);
		if (sensor == null) {
			return;
		}
		Logs.d(Logs.SENSORTEST, "[OUT] " + sensor.toString());
		mSensorManager.unregisterListener(mSensorEventListener, sensor);
	}

	private void setOnSensorEventListener() {
		mSensorEventListener = new SensorEventListener() {
			public void onSensorChanged(SensorEvent event) {
				switch (event.sensor.getType()) {
					case Sensor.TYPE_ACCELEROMETER:
						handleAccelerometer(event);
						break;
					case Sensor.TYPE_PROXIMITY:
						handleProximity(event);
						break;
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				Logs.i(String.format(Locale.getDefault(), "[onAccuracyChanged] name:%s accuracy:%d", sensor.getName(), accuracy));
			}
		};
	}

	private void handleAccelerometer(SensorEvent event) {
		if (false == isEnableAccelerometer) {
			return;
		}
		float x = event.values[0], y = event.values[1], z = event.values[2];
		Logs.d(String.format(Locale.getDefault(), "[Accelerometer-changed] x:%.2f  y:%.2f  z:%.2f", x, y, z));
		StateSingleton.getInstance().setZRate((float)(0.35 * StateSingleton.getInstance().getZRate() + 0.65 * z));
	}

	private void handleProximity(SensorEvent event) {
		if (false == isEnableProximity) {
			return;
		}
		Logs.d(String.format(Locale.getDefault(), "[Proximity-chan, xged] x:%.2f/%.2f", event.values[0], event.sensor.getMaximumRange()));
		if (event.values[0] < 5) {
			StateSingleton.getInstance().setPstate(StateSingleton.PSTATE_MODE_CLOSE); // 가까울 때
		} else {
			StateSingleton.getInstance().setPstate(StateSingleton.PSTATE_MODE_AWAY);
		}
	}
}
