package com.realnumworks.focustimer.view.theme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.data.DateTime;
import com.realnumworks.focustimer.utils.DrawableHelper;
import com.realnumworks.focustimer.utils.Logs;

public class ThemeDetailActivity extends ActionBarActivity {
	public static final int THEMEMODE_DETAIL = 0, THEMEMODE_NEWTHEME = 1;

	// private Tracker mTracker;

	ActionBar actionBar;
	ImageButton btns_color[] = new ImageButton[7];
	EditText et_themeName;
	ImageButton btn_delete;
	Theme currentTheme;
	TypefaceTextView tv_currentFocusTime, tv_totalFocusTime, tv_numOfFocus,
			tv_averageFocusTime;
	LinearLayout layout_bottom;
	int mode;
	Intent intent;
	DataBaseHelper dbm;
	String themeId;
	boolean isChanged = false;
	int numOfThemes = 0;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_theme_detail);
		context = this;
		isChanged = false;
		setActionBar();
		setComponents();

		intent = getIntent();
		dbm = new DataBaseHelper(this);
		mode = intent.getIntExtra("mode", 0); // mode가 0이면 Detail, 1이면 신규
		if (mode == THEMEMODE_NEWTHEME) {
			layout_bottom.setVisibility(View.GONE);
			currentTheme = new Theme(); // 저장누르면 여기에 name, color, order가 들어가게
										// 된다. 초기 id는 세팅되어 있고, color는 기본값 0으로
										// 세팅되어 있다.
			et_themeName.setText(currentTheme.getName());
			reloadDotsDrawable();
			selectColorDot(currentTheme.getColor());
			et_themeName.setTextColor(currentTheme.getRealColor());
			et_themeName.setSelection(currentTheme.getName().length());
			et_themeName
					.setOnFocusChangeListener(new View.OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							if (hasFocus) {
								et_themeName.postDelayed(new Runnable() {
									@Override
									public void run() {
										InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
										imm.showSoftInput(et_themeName, InputMethodManager.SHOW_FORCED);
									}
								}, 10);
							}
						}
					});
		} else if (mode == THEMEMODE_DETAIL) // Detail
		{
			themeId = intent.getStringExtra("themeId");
			showDetail();
		}
	}

	public void showDetail() {
		setListeners();

		currentTheme = dbm.getThemeById(themeId);

		// Extras 받아오기
		currentTheme.setLastFocusTime(intent.getIntExtra("recent", 0));
		currentTheme.setNumOfFocus(intent.getIntExtra("numOfFocus", 0));
		currentTheme.setTotalFocusTime(intent.getIntExtra("totalFocusTime", 0));

		numOfThemes = intent.getIntExtra("numOfThemes", 0);
		Logs.d(Logs.THEMETEST, "num Of themes = " + numOfThemes);

		Logs.d("ThemeDetail", "getExtras : " + currentTheme.toString());

		et_themeName.setTextColor(currentTheme.getRealColor());
		et_themeName.setText(currentTheme.getName());
		et_themeName.setSelection(currentTheme.getName().length());

		if (currentTheme.getLastFocusTime() != 0) {
			long lastfocusSec = currentTheme.getLastFocusTime();

			tv_currentFocusTime.setText(DateTime
					.getDateString((int) lastfocusSec));
			tv_totalFocusTime.setText(DateTime.getTimeString(
					currentTheme.getTotalFocusTime(), 2));
			tv_numOfFocus.setText(currentTheme.getNumOfFocus() + "회");
			tv_averageFocusTime.setText(DateTime.getTimeString(
					currentTheme.getAvgFocusTime(), 1) + "/회");
		} else {
			tv_currentFocusTime.setText("없음");
			tv_totalFocusTime.setText("없음");
			tv_numOfFocus.setText("0회");
			tv_averageFocusTime.setText("없음");
		}

		reloadDotsDrawable();
		selectColorDot(currentTheme.getColor());

		if (numOfThemes == 1) // 테마가 1개면 삭제 버튼을 없애버림
		{
			btn_delete.setVisibility(View.GONE);
		}
	}

	public void setActionBar() {
		setTheme(R.style.NoActionBarShadowTheme); // 액션바 그림자 제거
		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(DrawableHelper.getDrawable(this,
				R.drawable.actionbar_gradient));

		View v = getLayoutInflater().inflate(R.layout.actionbar_theme_detail,
				null);
		actionBar.setCustomView(v, new ActionBar.LayoutParams(new LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT)));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		TypefaceTextView tv_title = (TypefaceTextView) v
				.findViewById(R.id.tv_actionbardetail_title);
		tv_title.setText("테마 정보");

		Button btn_back = (Button) v
				.findViewById(R.id.btn_actionbardetail_back);
		Button btn_save = (Button) v
				.findViewById(R.id.btn_actionbardetail_save);

		btn_back.setOnClickListener(new View.OnClickListener() {// 뒤로가기
			@Override
			public void onClick(View arg0) {
				// Dialog : 페이지를 벗어나시겠습니까? 정보가 저장되지 않습니다.
				if (mode == THEMEMODE_NEWTHEME
						|| (mode == THEMEMODE_DETAIL && isChanged == true)) {
					AlertDialog.Builder alertDlg = new AlertDialog.Builder(
							context);

					alertDlg.setTitle("이 페이지를 벗어나시겠습니까?");
					alertDlg.setMessage("변경사항은 저장되지 않습니다.");
					alertDlg.setPositiveButton("예",
							new DialogInterface.OnClickListener() { // 확인 버튼
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
									InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(et_themeName.getWindowToken(), 0);
								}
							});
					alertDlg.setNegativeButton("아니오",
							new DialogInterface.OnClickListener() { // 취소 버튼
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.cancel();
								}
							});
					AlertDialog alert = alertDlg.create();
					alert.show();
				} else if (mode == THEMEMODE_DETAIL && isChanged == false) {
					finish();
				}
			}
		});

		btn_save.setOnClickListener(new View.OnClickListener() { // 저장
			@Override
			public void onClick(View arg0) {
				// TODO currentTheme를 DB에 Update
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(et_themeName.getWindowToken(), 0);
				currentTheme.setName(et_themeName.getText().toString());
				Logs.d("ThemeDetail.save", currentTheme.toString());
				if (mode == THEMEMODE_DETAIL)
					dbm.modifyTheme(currentTheme);
				else if (mode == THEMEMODE_NEWTHEME)
					dbm.insertTheme(currentTheme);
				finish();
			}
		});

	}

	public void setComponents() {
		et_themeName = (EditText) findViewById(R.id.et_themedetail_name);
		tv_currentFocusTime = (TypefaceTextView) findViewById(R.id.tv_themedetail_curfocustime);
		tv_totalFocusTime = (TypefaceTextView) findViewById(R.id.tv_themedetail_totalfocustime);
		tv_numOfFocus = (TypefaceTextView) findViewById(R.id.tv_themedetail_numoffocus);
		tv_averageFocusTime = (TypefaceTextView) findViewById(R.id.tv_themedetail_avgfocustime);

		btns_color[0] = (ImageButton) findViewById(R.id.ib_themedetail_green);
		btns_color[1] = (ImageButton) findViewById(R.id.ib_themedetail_yellow);
		btns_color[2] = (ImageButton) findViewById(R.id.ib_themedetail_orange);
		btns_color[3] = (ImageButton) findViewById(R.id.ib_themedetail_red);
		btns_color[4] = (ImageButton) findViewById(R.id.ib_themedetail_purple);
		btns_color[5] = (ImageButton) findViewById(R.id.ib_themedetail_lightblue);
		btns_color[6] = (ImageButton) findViewById(R.id.ib_themedetail_darkblue);
		btns_color[0].setOnClickListener(colordotListener);
		btns_color[1].setOnClickListener(colordotListener);
		btns_color[2].setOnClickListener(colordotListener);
		btns_color[3].setOnClickListener(colordotListener);
		btns_color[4].setOnClickListener(colordotListener);
		btns_color[5].setOnClickListener(colordotListener);
		btns_color[6].setOnClickListener(colordotListener);

		btn_delete = (ImageButton) findViewById(R.id.btn_themedetail_delete);
		layout_bottom = (LinearLayout) findViewById(R.id.layout_themedetail_bottom);
	}

	OnClickListener colordotListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// 버튼 누르면 버튼 이미지 변하고
			// EditText의 테마색 변하고
			// currentTheme의 color값이 변함
			switch (v.getId()) {
			case R.id.ib_themedetail_green:
				currentTheme.setColor(0);
				break;
			case R.id.ib_themedetail_yellow:
				currentTheme.setColor(1);
				break;
			case R.id.ib_themedetail_orange:
				currentTheme.setColor(2);
				break;
			case R.id.ib_themedetail_red:
				currentTheme.setColor(3);
				break;
			case R.id.ib_themedetail_purple:
				currentTheme.setColor(4);
				break;
			case R.id.ib_themedetail_lightblue:
				currentTheme.setColor(5);
				break;
			case R.id.ib_themedetail_darkblue:
				currentTheme.setColor(6);
				break;
			}
			isChanged = true;
			reloadDotsDrawable();
			selectColorDot(currentTheme.getColor());
			et_themeName.setTextColor(currentTheme.getRealColor());
		}
	};

	public void setListeners() {
		btn_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alertDlg = new AlertDialog.Builder(context);

				alertDlg.setMessage("정말 삭제하시겠습니까?\n복구하실 수 없습니다!");
				alertDlg.setPositiveButton("예",
						new DialogInterface.OnClickListener() { // 확인 버튼
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dbm.deleteTheme(currentTheme.getId());
								Toast.makeText(getApplicationContext(),
										"삭제되었습니다", Toast.LENGTH_SHORT).show();
								finish();
							}
						});
				alertDlg.setNegativeButton("아니오",
						new DialogInterface.OnClickListener() { // 취소 버튼
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						});
				AlertDialog alert = alertDlg.create();
				alert.show();
			}
		});

		// EditText의 위치가 변할 경우, '변경사항 있음' 으로 설정한다.
		et_themeName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				isChanged = !arg0.toString().equals(currentTheme.getName());
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}

	public void reloadDotsDrawable() {
		btns_color[0].setImageResource(R.drawable.ic_circle_1_green_3x);
		btns_color[1].setImageResource(R.drawable.ic_circle_2_yellow_3x);
		btns_color[2].setImageResource(R.drawable.ic_circle_3_orange_3x);
		btns_color[3].setImageResource(R.drawable.ic_circle_4_red_3x);
		btns_color[4].setImageResource(R.drawable.ic_circle_5_purple_3x);
		btns_color[5].setImageResource(R.drawable.ic_circle_6_lightblue_3x);
		btns_color[6].setImageResource(R.drawable.ic_circle_7_darkblue_3x);
	}

	public void selectColorDot(int color) {
		switch (color) {
		case 0:
			btns_color[0].setImageResource(R.drawable.ic_circle_1_green_on_3x);
			break;
		case 1:
			btns_color[1].setImageResource(R.drawable.ic_circle_2_yellow_on_3x);
			break;
		case 2:
			btns_color[2].setImageResource(R.drawable.ic_circle_3_orange_on_3x);
			break;
		case 3:
			btns_color[3].setImageResource(R.drawable.ic_circle_4_red_on_3x);
			break;
		case 4:
			btns_color[4].setImageResource(R.drawable.ic_circle_5_purple_on_3x);
			break;
		case 5:
			btns_color[5]
					.setImageResource(R.drawable.ic_circle_6_lightblue_on_3x);
			break;
		case 6:
			btns_color[6]
					.setImageResource(R.drawable.ic_circle_7_darkblue_on_3x);
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// String name = getClass().getSimpleName();
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
