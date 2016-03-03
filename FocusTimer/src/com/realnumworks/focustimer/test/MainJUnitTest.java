package com.realnumworks.focustimer.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.TextView;

import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.view.main.MainActivity;

public class MainJUnitTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private Activity activity;

	/**
	 * 테스트에 앞서 기본적인 작업을 수행하는 메소드.
	 * 각각의 메서드가 수행되기 전에 이 메소드가 호출된다.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity(); //Activity 실행
	}

	/**
	 * 각각의 메서드가 수행된 후에 이 메소드가 호출된다.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public MainJUnitTest() {
		super(MainActivity.class);
	}

	@LargeTest
	public void test() {
		// check activity is null
		assertNotNull("Activity is null? : ", activity);
		// compare a.value with b.value
		final TextView tv = (TextView)activity.findViewById(R.id.tv_alarm);
		final String expected = activity.getString(R.string.crash_dialog_ok_toast);
		final String actual = tv.getText().toString();
		assertEquals(expected, actual);
	}
}
