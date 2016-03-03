package com.realnumworks.focustimer;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;

import com.realnumworks.focustimer.singleton.DeviceControl;
import com.realnumworks.focustimer.singleton.SensorControl;
import com.realnumworks.focustimer.utils.Logs;

@ReportsCrashes(
	formKey = "", // Google Docs Key, it should be blank that Google Docs has block
	mode = ReportingInteractionMode.DIALOG, // notice mode 
	resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
	resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
	resDialogText = R.string.crash_dialog_text,
	// resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
	resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. When defined, adds a user text field input with this text resource as a label
	// resDialogEmailPrompt = R.string.crash_user_email_label, // optional. When defined, adds a user email text entry with this text resource as label. The email address will be populated from SharedPreferences and will be provided as an ACRA field if configured.
	// resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
	mailTo = "support@realnumworks.com", 	// report location
	customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE }// , ReportField.LOGCAT } // report contents
)
public class MainApplication extends Application {

	@Override
	public void onCreate() {
		init();
		super.onCreate();
		Logs.i("== MainApplication onCreated");
	}

	/**
	 * Application 최초 구동시 수행해야 할 작업을 처리
	 */
	private void init() {
		Context context = getApplicationContext();
		// - init Sensor
		SensorControl.getInstance().init(context);
		// - init Device?
		DeviceControl.getInstance().setApplicationContext(context);
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}
}