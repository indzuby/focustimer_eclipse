package com.realnumworks.focustimer.view.history;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.mobsandgeeks.ui.TypefaceTextView;
import com.realnumworks.focustimer.R;
import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.StateSingleton;
import com.realnumworks.focustimer.utils.DrawableHelper;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.theme.Theme;

public class HistoryActivity extends ActionBarActivity {

	// private Tracker mTracker;

	ArrayList<Record> recordList;
	ArrayList<ItemString> itemStringList;
	ArrayList<ArrayList<ItemString>> itemStringListArray;
	HistoryAdapter historyAdapter;
	DataBaseHelper dbhelper;
	String currentThemeId;
	Theme currentTheme;
	SwipeMenuListView historyListView;
	TypefaceTextView topButtons[];
	int selectedButtonIndex = 0;

	boolean isDataChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Obtain the shared Tracker instance.
		// AnalyticsApplication application = (AnalyticsApplication)
		// getApplication();
		// mTracker = application.getDefaultTracker();

		setContentView(R.layout.activity_history);

		bringCurrentThemeIdFromIntent();
		bringDatasFromDB();
		initViews();
		makeActionBarAppearance();
		setTopButtonsOnClickListener();
		BaseItem.inputWeekthInRecordList(recordList);
		Collections.reverse(recordList);
		refreshScreen(selectedButtonIndex);
	}

	public void bringCurrentThemeIdFromIntent() {
		Intent intent = getIntent();
		currentThemeId = intent.getStringExtra("themeId");
		StateSingleton.getInstance().currentThemeId = currentThemeId;
	}

	public void bringDatasFromDB() {
		dbhelper = new DataBaseHelper(this);
		currentTheme = dbhelper.getThemeById(currentThemeId);
		recordList = dbhelper.getRecordListOfAllByTheme(currentThemeId);
		dbhelper.close();
	}

	public void initViews()
	{
		initItemStringList();
		initItemStringListArray();
		findViews();
	}

	public void initItemStringList() {
		itemStringList = null;
	}

	public void initItemStringListArray() {
		itemStringListArray = new ArrayList<ArrayList<ItemString>>();
		for (int topButtoonIndex = 0; topButtoonIndex < 4; topButtoonIndex++) {
			itemStringListArray.add(null);
		}
	}

	public void findViews() {
		historyListView = (SwipeMenuListView)findViewById(R.id.list_history);
		topButtons = new TypefaceTextView[4];
		topButtons[0] = (TypefaceTextView)findViewById(R.id.tvbtn_history_each);
		topButtons[1] = (TypefaceTextView)findViewById(R.id.tvbtn_history_day);
		topButtons[2] = (TypefaceTextView)findViewById(R.id.tvbtn_history_week);
		topButtons[3] = (TypefaceTextView)findViewById(R.id.tvbtn_history_month);
	}

	public void makeActionBarAppearance() {
		setTheme(R.style.NoActionBarShadowTheme); // 액션바 그림자 제거
		ActionBar actionBar = getSupportActionBar();
		actionBar.setBackgroundDrawable(DrawableHelper.getDrawable(this,
			R.drawable.actionbar_gradient));

		View v = getLayoutInflater().inflate(R.layout.actionbar_record, null);
		actionBar.setCustomView(v, new ActionBar.LayoutParams(new LayoutParams(
			ActionBar.LayoutParams.MATCH_PARENT,
			ActionBar.LayoutParams.MATCH_PARENT)));
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		TextView tv_title = (TextView)v
			.findViewById(R.id.tv_actionbar_record_themename);
		tv_title.setText(currentTheme.getName());
	}

	public void setTopButtonsOnClickListener() {
		View.OnClickListener topButtonListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int buttonIndex = 0; buttonIndex < topButtons.length; buttonIndex++) {
					if (topButtons[buttonIndex].getId() == v.getId()) {
						if (isDataChanged) {
							initItemStringListArray();
							bringDatasFromDB();
							BaseItem.inputWeekthInRecordList(recordList);
							isDataChanged = false;
						}
						refreshScreen(buttonIndex);
						break;
					}
				}
			}
		};
		for (int buttonIndex = 0; buttonIndex < topButtons.length; buttonIndex++) {
			topButtons[buttonIndex].setOnClickListener(topButtonListener);
		}
	}

	public void refreshScreen(int selectedButtonIndex) {
		if (recordList.size() == 0) { //레코드가 없으면
			showTextOfNoData();
		} else {
			selectPage(selectedButtonIndex);
		}
	}

	public void showTextOfNoData() {
		TypefaceTextView noRecordTextView = (TypefaceTextView)findViewById(R.id.tv_history_nodata);
		noRecordTextView.setVisibility(View.VISIBLE);
		historyListView.setVisibility(View.GONE);
	}

	public void selectPage(int index) {
		selectButtonOfIndex(index);
		int dataType = BaseItem.DTYPE_EACH;
		switch (index) {
			case 0:
				dataType = BaseItem.DTYPE_EACH;
				break;
			case 1:
				dataType = BaseItem.DTYPE_DAY;
				break;
			case 2:
				dataType = BaseItem.DTYPE_WEEK;
				break;
			case 3:
				dataType = BaseItem.DTYPE_MONTH;
				break;
		}
		setSwipeMenu(index);
		inputItemsToListViewByDataType(dataType);
	}

	public void selectButtonOfIndex(int index) {
		topButtons[selectedButtonIndex].setSelected(false);
		selectedButtonIndex = index;
		topButtons[selectedButtonIndex].setSelected(true);
	}

	public void setSwipeMenu(int selectedButtonIndex) {
		switch (selectedButtonIndex) {
			case 0:
			case 1:
				setSwipeMenuCreater(true);
				setOnSwipeMenuItemClickListener();
				historyListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
				break;
			case 2:
			case 3:
				setSwipeMenuCreater(false);
				break;
		}
	}

	private void setSwipeMenuCreater(boolean b) {
		SwipeMenuCreator creator;
		if (b) {
			creator = new SwipeMenuCreator() {
				@Override
				public void create(SwipeMenu menu) {
					if (menu.getViewType() == BaseItem.ITYPE_DATA) {
						SwipeMenuItem deleteItem = getSwipeMenuItemOfDelete();
						menu.addMenuItem(deleteItem);
					}
				}
			};
		}
		else {
			creator = null;
		}
		// set creator
		historyListView.setMenuCreator(creator);
	}

	private SwipeMenuItem getSwipeMenuItemOfDelete() {
		SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
		deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
		deleteItem.setWidth(dp2px(60));
		deleteItem.setIcon(R.drawable.ic_delete_white_24dp);
		//						deleteItem.setTitle("삭제");
		deleteItem.setTitleSize(18);
		deleteItem.setTitleColor(Color.WHITE);
		return deleteItem;
	}

	private int dp2px(int dp) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
			getResources().getDisplayMetrics());
	}

	private void setOnSwipeMenuItemClickListener() {
		historyListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				boolean ifItemExistInSectionAlone = getIfItemExistOnlyInSection(position);
				removeItemAtPositionFromList(position, ifItemExistInSectionAlone);

				// 데이터가 없을 때, "저장된 데이터가 없습니다"
				if (recordList.size() == 0) {
					showTextOfNoData();
				}
				historyAdapter.notifyDataSetChanged();
				// false : not close the menu; true : close the menu
				return true;
			}
		});
	}

	private void removeItemAtPositionFromList(int position, boolean ifItemExistInSectionAlone) {
		ItemString selectedItem = itemStringList.get(position);
		//혼자 있으면 Header도 같이 지워 준다.
		if (ifItemExistInSectionAlone) {
			itemStringList.remove(position - 1); //헤더 삭제
			position--;
		}
		itemStringList.remove(position);
		dbhelper.deleteHistoryItem(selectedItem.getDataType(), selectedItem.getDataCode());
		isDataChanged = true;
	}

	private boolean getIfItemExistOnlyInSection(int position) {
		//Item이 Section에 혼자 있는지 확인
		//조건 1) 바로 직전 아이템이 Header이고
		if (itemStringList.get(position - 1).getHeader() != null) {
			//조건 2) 전체의 마지막 Item이거나 바로 뒤가 Header이면(Section과 Section 사이)
			if (position == itemStringList.size() - 1
				|| itemStringList.get(position + 1).getHeader() != null) {
				return true;
			}
		}
		return false;
	}

	public void inputItemsToListViewByDataType(int dataType) {
		makeItemStringListFromRecordList(dataType);
		setListViewSelectorAndAdapter();
	}

	private void makeItemStringListFromRecordList(int dataType) {
		switch (dataType) {
			case BaseItem.DTYPE_EACH:
				itemStringList = EachItem.getItemStringListFromRecordList(recordList);
				break;
			case BaseItem.DTYPE_DAY:
				ArrayList<DayItem> dayItemList = DayItem.getItemListFromRecordList(recordList);
				itemStringList = DayItem.getItemStringListFromItemList(dayItemList);
				break;
			case BaseItem.DTYPE_WEEK:
				itemStringList = WeeklyManager.getItemStringListFromRecordList(recordList);
				break;
			case BaseItem.DTYPE_MONTH:
				itemStringList = MonthlyManager.getItemStringListFromRecordList(recordList);
				break;
		}
	}

	public void setListViewSelectorAndAdapter() {
		historyAdapter = new HistoryAdapter(this, itemStringList);
		historyListView.setSelector(R.drawable.listselector);
		historyListView.setAdapter(historyAdapter);
	}

	/**
	 * Override Methods
	 */

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) // 뒤로가기 키를 눌렀을 때, 종료
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.menu_record, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// String name = "측정 기록 화면";
		// Log.i(LogTags.ANALYTICS, "Setting screen name: " + name);
		// mTracker.setScreenName("Image~" + name);
		// mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

}
