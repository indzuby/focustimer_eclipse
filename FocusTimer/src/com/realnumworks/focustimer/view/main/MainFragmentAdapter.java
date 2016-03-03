package com.realnumworks.focustimer.view.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.realnumworks.focustimer.data.DataBaseHelper;
import com.realnumworks.focustimer.singleton.DeviceControl;
import com.realnumworks.focustimer.utils.Logs;
import com.realnumworks.focustimer.view.theme.Theme;
import com.realnumworks.focustimer.view.theme.ThemeListActivity.NoAscCompare;

class MainFragmentAdapter extends FragmentStatePagerAdapter {

	private List<Theme> themelist;
	private DataBaseHelper dbm;
	protected static ArrayList<String> content;

	public MainFragmentAdapter(FragmentManager fm) {
		super(fm);
		Logs.d("startTest", "MainFragmentAdapter");
		content = new ArrayList<String>();
		setContent();
	}

	public void setContent() {
		Logs.d("startTest", "MainFragmentAdapter.setContent");
		// Input Initial Themes : 테마가 한 개도 없으면 테마 3개를 Input한다.
		content.clear();
		dbm = new DataBaseHelper(DeviceControl.getInstance().getApplicationContext());
		themelist = dbm.getThemesListOfAll();
		if (themelist.size() == 0) {
			Theme theme01 = new Theme("0", "Reading", 0, 0); // 초기 3테마의 Id는 0, 1, 2로 고정된다.
			Theme theme02 = new Theme("1", "Studying", 1, 1);
			Theme theme03 = new Theme("2", "Meditation", 2, 2);
			dbm.insertTheme(theme01);
			dbm.insertTheme(theme02);
			dbm.insertTheme(theme03);
			themelist = dbm.getThemesListOfAll();
		}
		Collections.sort(themelist, new NoAscCompare()); // 일단 order 순으로 다 sort한다. -> order 순서로 페이지가 표시됨
		for (int i = 0; i < themelist.size(); i++) {
			Logs.d("MainFragmentAdapter", "mtheme" + i + " : " + themelist.get(i).toString());
			content.add(themelist.get(i).getId()); // 컨텐츠는 테마의 id로 설정한다.
		}
	}

	@Override
	public Fragment getItem(int position) {
		Logs.d("startTest", "MainFragmentAdapter.getItem(" + position + ") : id = " + content.get(position));
		Fragment mainFragment = MainFragment.newInstance(content.get(position % content.size()));
		// Fragment mainFragment =
		// MainFragment.newInstance(themelist.get(position).getId());
		return mainFragment;// 여기서 프래그먼트를 설정함
	}

	@Override
	public int getCount() {
		// LogTags.printLog("startTest",
		// "MainFragmentAdapter.getCount = "+mCount);
		return content.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Logs.d("startTest", "MainFragmentAdapter.getPageTitle(" + position + ")");
		// return themelist.get(position).getId();
		return MainFragmentAdapter.content.get(position % content.size());
	}

//	@Override
//	public void notifyDataSetChanged() {
//		// TODO Auto-generated method stub
//		Logs.d("startTest", "MainFragment.nofifydatasetchanged");
//		content.clear();
//		themelist = dbm.getThemesListOfAll();
//		Collections.sort(themelist, new NoAscCompare()); // 일단 order 순으로 다 sort한다. -> order 순서로 페이지가 표시됨
//		for (int i = 0; i < themelist.size(); i++) {
//			Logs.d("MainFragmentAdapter", "mtheme" + i + " : " + themelist.get(i).toString());
//			content.add(themelist.get(i).getId()); // 컨텐츠는 테마의 id로 설정한다.
//		}
//		super.notifyDataSetChanged();
//	}
	public void update(List<Theme> themes){
		themelist = themes;
		Collections.sort(themelist,new NoAscCompare());
		if(themelist.size() != content.size()) {
			content = new ArrayList<String>();
			for(Theme theme : themes) {
				content.add(theme.getId()); // 컨텐츠는 테마의 id로 설정한다.
			}
			notifyDataSetChanged();
		}else {
			for(int i =0 ; i<getCount();i++) {
				content.set(i,themelist.get(i).getId()); // 컨텐츠는 테마의 id로 설정한다.
			}
			
		}
	}
	@Override
	public int getItemPosition(Object item) {
		Logs.d("startTest", "MainFragmentAdapter.getItemPosition Object = " + item.toString());
		return POSITION_NONE;
	}
}
