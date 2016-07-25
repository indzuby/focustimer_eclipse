package com.realnumworks.focustimer.view.theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.CustomBundleSingleton;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.DrawableHelper;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.history.Record;

public class ThemeListActivity extends ActionBarActivity {
	int mode = StateSingleton.getInstance().getThemeMode();

	// private Tracker mTracker;

	ActionBar actionBar;
	DragSortListView listView;
	ThemeListAdapter adapter;
	ArrayList<Theme> list;
	Button btn_edit, btn_close;
	ImageButton btn_addNewTheme;
	DragSortController controller;
	DataBaseHelper dbm;
	int numOfThemes;
	TypefaceTextView tv_addNewTheme;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_theme_list);

		setActionBar();
		setThemesDB();
		setListView();
		setComponents();
		setListeners();

	}

	public void setActionBar() {
		setTheme(R.style.NoActionBarShadowTheme); // 액션바 그림자 제거
		actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(DrawableHelper.getDrawable(this, R.drawable.actionbar_gradient));

		View v = getLayoutInflater().inflate(R.layout.actionbar_theme, null);
		actionBar.setCustomView(v, new ActionBar.LayoutParams(new LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		setActionBarTitle("테마 목록");
		btn_edit = (Button) findViewById(R.id.btn_actionbar_edit);
		btn_close = (Button) findViewById(R.id.btn_actionbar_close);
		btn_edit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mode == StateSingleton.THEME_MODE_LIST) {
					// 신규 테마 추가 안보이게
					tv_addNewTheme.setVisibility(View.GONE);
					btn_addNewTheme.setVisibility(View.GONE);

					listView.setDragEnabled(true);
					btn_edit.setText("완료");
					btn_edit.setTypeface(null, Typeface.BOLD);
					mode = StateSingleton.THEME_MODE_EDIT;
					StateSingleton.getInstance().setThemeMode(mode);
					adapter.notifyDataSetChanged();
				} else if (mode == StateSingleton.THEME_MODE_EDIT) {
					// 신규 테마 추가 보이게
					tv_addNewTheme.setVisibility(View.VISIBLE);
					btn_addNewTheme.setVisibility(View.VISIBLE);

					// DB Insert 처리
					for (int i = 0; i < list.size(); i++) {
						Logs.d("ThemeList", "Order Update : " + list.get(i).toString());
						dbm.modifyTheme(list.get(i)); // 각각의 order update
					}
					listView.setDragEnabled(false);
					btn_edit.setTypeface(null, Typeface.NORMAL);
					btn_edit.setText("편집");
					mode = StateSingleton.THEME_MODE_LIST;
					StateSingleton.getInstance().setThemeMode(mode);
					adapter.notifyDataSetChanged();
				}
			}
		});

		btn_close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finishActivity();
			}
		});
	}

	public void setThemesDB() {
		dbm = new DataBaseHelper(this);
		list = dbm.getThemesListOfAll();
		Collections.sort(list, new NoAscCompare());
		numOfThemes = list.size();
		for (int i = 0; i < numOfThemes; i++) {
			int totalFocusTime = 0;
			Logs.d(Logs.THEMETEST, "themetest " + i + " : " + list.get(i).toString());
			ArrayList<Record> recsList = dbm.getRecordListOfAllByTheme(list.get(i).getId());
			if (recsList.size() == 0)
				continue;
			Logs.d(Logs.THEMETEST, "recsList sz=" + recsList.size());
			Collections.reverse(recsList);
			list.get(i).setLastFocusTime(recsList.get(0).getTimeInSec());
			list.get(i).setNumOfFocus(recsList.size());
			for (int j = 0; j < recsList.size(); j++) {
				Logs.d("dd", "themetest rec[" + j + "]" + recsList.get(j).toString());
				totalFocusTime += recsList.get(j).getFocustime();
			}
			list.get(i).setTotalFocusTime(totalFocusTime);
			Logs.d(Logs.THEMETEST, "themetest detail : " + list.get(i).toString());
		}
	}

	public void setListView() {
		listView = (DragSortListView) findViewById(R.id.dslv_theme);

		adapter = new ThemeListAdapter(ThemeListActivity.this, this, R.layout.listitem_themelist, list, numOfThemes);
		listView.setAdapter(adapter);
		listView.setDropListener(onDrop);

		controller = new DragSortController(listView);
		controller.setDragHandleId(R.id.iv_drag_handle);
		controller.setRemoveEnabled(false);
		controller.setSortEnabled(true);
		controller.setDragInitMode(1);
		controller.setBackgroundColor(getResources().getColor(R.color.ft_selectedgray));

		listView.setFloatViewManager(controller);
		listView.setOnTouchListener(controller);
		listView.setDragEnabled(false);
	}

	/**
	 * 테마 순서 오름차순 정렬
	 * 
	 * @author Yedam
	 * 
	 */
	public static class NoAscCompare implements Comparator<Theme> {

		@Override
		public int compare(Theme arg0, Theme arg1) {
			return arg0.getOrder() < arg1.getOrder() ? -1 : arg0.getOrder() > arg1.getOrder() ? 1 : 0;
		}

	}

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {
				Theme item = adapter.getItem(from);
				list.remove(from);
				list.add(to, item);
				// Reorder
				for (int i = 0; i < list.size(); i++) {
					list.get(i).setOrder(i);
				}
				adapter.notifyDataSetChanged();
			}
		}
	};

	public void setActionBarTitle(String title) {
		TypefaceTextView tv_title = (TypefaceTextView) findViewById(R.id.tv_actionbar_title);
		tv_title.setText(title);
	}

	public void setComponents() {
		btn_addNewTheme = (ImageButton) findViewById(R.id.btn_themelist_addnewtheme);
		tv_addNewTheme = (TypefaceTextView) findViewById(R.id.tv_themelist_addnewtheme);
		if (numOfThemes == Theme.MAX_THEME_COUNT) {
			/**
			 * 테마 수가 최대일 때, add new 기능을 히든시킨다.
			 */
			showAddNewTheme(false);
		} else {
			showAddNewTheme(true);
		}
	}

	public void showAddNewTheme(boolean isShow) {
		if (isShow) {
			btn_addNewTheme.setVisibility(View.VISIBLE);
			tv_addNewTheme.setVisibility(View.VISIBLE);
		} else {
			btn_addNewTheme.setVisibility(View.GONE);
			tv_addNewTheme.setVisibility(View.GONE);
		}
	}

	public void setListeners() {
		btn_addNewTheme.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ThemeDetailActivity.class);
				intent.putExtra("mode", 1); // mode가 1이면 신규테마추가. 이 경우 id는 넣지
											// 않는다.
				intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				startActivityForResult(intent, 1); // 신규
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		// String name = getClass().getSimpleName();
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
		//
		Logs.d("ThemeList", "onResume");
		setThemesDB();
		adapter = new ThemeListAdapter(ThemeListActivity.this, this, R.layout.listitem_themelist, list, numOfThemes);
		listView.setAdapter(adapter);

		adapter.notifyDataSetChanged();
		refreshAddNewThemes();
	}

	public void refreshAddNewThemes() {
		numOfThemes = listView.getCount();
		if (numOfThemes == Theme.MAX_THEME_COUNT) {
			showAddNewTheme(false);
		} else
			showAddNewTheme(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finishActivity();
		}
		return false;
	}

	public void finishActivity() {
		if (StateSingleton.getInstance().getThemeMode() == StateSingleton.THEME_MODE_EDIT) {
			AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);

			alertDlg.setMessage("이 페이지를 벗어나시겠습니까?\n변경사항은 저장되지 않습니다.");
			alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() { // 확인 버튼
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							StateSingleton.getInstance().setThemeMode(StateSingleton.THEME_MODE_LIST);
							finish();
						}
					});
			alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() { // 취소 버튼
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.cancel();
						}
					});
			AlertDialog alert = alertDlg.create();
			alert.show();
		} else if (StateSingleton.getInstance().getThemeMode() == StateSingleton.THEME_MODE_LIST)
			finish();
	}

	@Override
	public void finish() {
		super.finish();
		CustomBundleSingleton.getInstance().setRedraw(false);
	}
}

// 어댑터 클래스
class ThemeListAdapter extends BaseAdapter {

	Context con;
	LayoutInflater inflacter;
	ArrayList<Theme> mThemeList;
	Activity activity;
	int layout;
	int numOfThemes = 0;

	public ThemeListAdapter(Activity activity, Context context, int alayout, ArrayList<Theme> list, int numOfThemes) {
		this.activity = activity;
		con = context;
		inflacter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mThemeList = list;
		layout = alayout;
		this.numOfThemes = numOfThemes;
	}

	// 어댑터에 몇 개의 항목이 있는지 조사
	@Override
	public int getCount() {
		return mThemeList.size();
	}

	// position 위치의 DateTimeRecord 객체 반환
	@Override
	public Theme getItem(int position) {
		return mThemeList.get(position);
	}

	// position 위치의 항목 ID 반환
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 각 항목의 뷰 생성 후 반환. focustime이 0이면 header로 인식하며, 주차의 텍스트뷰를 반환한다.
	 */
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflacter.inflate(layout, parent, false);
		}

		TextView tv_name = (TextView) convertView.findViewById(R.id.tv_listtheme_name);
		TextView tv_lastFocus = (TextView) convertView.findViewById(R.id.tv_listtheme_lastfocus);
		TextView tv_lftime = (TextView) convertView.findViewById(R.id.tv_listtheme_lftime);
		ImageView iv_dragHandle = (ImageView) convertView.findViewById(R.id.iv_drag_handle);
		ImageView iv_clickHandle = (ImageView) convertView.findViewById(R.id.iv_click_handle);
		ImageView iv_colordot = (ImageView) convertView.findViewById(R.id.iv_listtheme_colordot);
		View v_grayLine = convertView.findViewById(R.id.v_listtheme_grayline);

		LinearLayout layout_left = (LinearLayout) convertView.findViewById(R.id.layout_themelist_left);
		final LinearLayout layout_right = (LinearLayout) convertView.findViewById(R.id.layout_themelist_right);

		// Content Layout Listener
		layout_left.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Logs.d("startTime", "ThemeListAdapter.ContentOnclick");
				DataBaseHelper dbm = new DataBaseHelper(con);
				v.setBackgroundColor(con.getResources().getColor(R.color.ft_selectedgray));
				Theme currentTheme = dbm.getThemeByOrder(position);
				StateSingleton.getInstance().currentThemeId = currentTheme.getId();
				activity.finish();
			}
		});

		// colordot color change
		int colordotId = 0;
		switch (mThemeList.get(position).getColor()) {
		case 0:
			colordotId = R.drawable.ic_circle_1_green_3x;
			break;
		case 1:
			colordotId = R.drawable.ic_circle_2_yellow_3x;
			break;
		case 2:
			colordotId = R.drawable.ic_circle_3_orange_3x;
			break;
		case 3:
			colordotId = R.drawable.ic_circle_4_red_3x;
			break;
		case 4:
			colordotId = R.drawable.ic_circle_5_purple_3x;
			break;
		case 5:
			colordotId = R.drawable.ic_circle_6_lightblue_3x;
			break;
		case 6:
			colordotId = R.drawable.ic_circle_7_darkblue_3x;
			break;
		}
		iv_colordot.setImageDrawable(DrawableHelper.getDrawable(con, colordotId));

		// TextView SetTexts
		tv_name.setText(mThemeList.get(position).getName());
		tv_name.setTextColor(mThemeList.get(position).getRealColor());
		tv_lastFocus.setText("최근");

		if (mThemeList.get(position).getLastFocusTime() != 0) {
			Record lfRecord = new Record(mThemeList.get(position).getLastFocusTime());
			String lfstr = (lfRecord.getMonth() + 1) + "월 " + lfRecord.getDate() + "일(" + lfRecord.getDayString() + ")";
			tv_lftime.setText(lfstr);
		} else
			tv_lftime.setText("기록 없음");

		switch (StateSingleton.getInstance().getThemeMode()) {
		case StateSingleton.THEME_MODE_LIST:
			iv_dragHandle.setVisibility(View.GONE);
			iv_clickHandle.setVisibility(View.VISIBLE);
			v_grayLine.setVisibility(View.GONE);
			iv_clickHandle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Logs.d("Theme.getView", "테마 상세 화면");
					layout_right.setBackgroundColor(con.getResources().getColor(R.color.ft_selectedgray));

					Intent intent = new Intent(con, ThemeDetailActivity.class);
					intent.putExtra("mode", 0); // 모드 0이면 상세 화면
					intent.putExtra("themeId", mThemeList.get(position).getId()); // 선택된 테마의 id 넣음
					intent.putExtra("recent", mThemeList.get(position).getLastFocusTime()); // 최근 측정일 넣음
					intent.putExtra("numOfFocus", mThemeList.get(position).getNumOfFocus()); // 총 측정 횟수 넣음
					intent.putExtra("totalFocusTime", mThemeList.get(position).getTotalFocusTime()); // 총 측정 시간 넣음
					intent.putExtra("numOfThemes", numOfThemes);
					Logs.d("Theme.getView", "Extras : " + mThemeList.get(position).toString());
					intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					con.startActivity(intent); // Detail
				}
			});
			break;
		case StateSingleton.THEME_MODE_EDIT:
			iv_dragHandle.setVisibility(View.VISIBLE);
			iv_clickHandle.setVisibility(View.GONE);
			v_grayLine.setVisibility(View.GONE);
			break;
		}

		return convertView;
	}
}
