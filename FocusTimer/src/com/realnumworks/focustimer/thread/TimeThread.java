package com.realnumworks.focustimer.thread;

import android.os.Handler;

import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.Logs;

public class TimeThread extends Thread {

	Handler handler = null;
	private boolean isRun = false;

	// 생성자
	public TimeThread(Handler handler) {
		this.handler = handler;
	}

	public TimeThread() {
	}

	@Override
	public synchronized void start() {
		super.start();
		isRun = true;
		Logs.d(Logs.THREADTEST, "[IN] 타임스레드");
		isRun = true;
	}

	public boolean isRunning() {
		return isRun;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	// 스레드 완전 정지 시키는 메소드
	public void stopForever() {
		synchronized (this) {
			isRun = false;
			Logs.d(Logs.THREADTEST, "[OUT] 타임스레드");
			notify();
		}
	}

	// 스레드 본체(한번 끝난 thread는 다시 쓸수없다. 다시 객체를 재생성해서 사용해야함)
	public void run() {
		while (isRun) {
			Logs.d(Logs.THREADTEST, "Running 타임스레드");
			try {
				// 매주기 0.5 초씩 쉰다.
				Thread.sleep(500);
			} catch (Exception e) {
				// TODO: handle exception
			}
			// 핸들러가 할당되지 않았을 때는 Continue-Loop with do-nothing.
			if (handler == null) {
				continue;
			}
			// 핸들러에 메세지를 보낸다
			switch (StateSingleton.getInstance().getTstate()) {
				case StateSingleton.TSTATE_MODE_FOCUSING: // Focusing 모드일 때는 0, Main화면 일 때는 1을 전달
					handler.sendEmptyMessage(0);
					break;
				case StateSingleton.TSTATE_MODE_MAIN:
					handler.sendEmptyMessage(1);
					break;
				case StateSingleton.TSTATE_MODE_LAPPINGRESULT:
					handler.sendEmptyMessage(2);
					break;
				default:
					handler.sendEmptyMessage(-1);
					break;
			}
		}
	}

	@Override
	protected TimeThread clone() {
		return new TimeThread(handler);
	}
}
