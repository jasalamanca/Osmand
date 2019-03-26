package net.osmand.plus.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;


public abstract class OsmandListActivity extends
		ActionBarProgressActivity implements AdapterView.OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		getListView().setBackgroundColor(
				getResources().getColor(
						getMyApplication().getSettings().isLightContent() ? R.color.bg_color_light
								: R.color.bg_color_dark));
	}


	public OsmandApplication getMyApplication() {
		return (OsmandApplication)getApplication();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				finish();
				return true;

		}
		return false;
	}

	protected MenuItem createMenuItem(Menu m, int id, int titleRes, int iconDark, int menuItemType) {
		MenuItem menuItem = m.add(0, id, 0, titleRes);
		if (iconDark != 0) {
			menuItem.setIcon(getMyApplication().getIconsCache().getIcon(iconDark));
		}
		menuItem.setOnMenuItemClickListener(item -> onOptionsItemSelected(item));
		menuItem.setShowAsAction(menuItemType);
		return menuItem;
	}

	protected void setListAdapter(ListAdapter adapter){
		((ListView)findViewById(android.R.id.list)).setAdapter(adapter);
		setOnItemClickListener(this);

	}

	protected ListView getListView() {
		return (ListView)findViewById(android.R.id.list);
	}

	public ListAdapter getListAdapter() {
		ListAdapter adapter = getListView().getAdapter();
		if (adapter instanceof HeaderViewListAdapter) {
			return ((HeaderViewListAdapter)adapter).getWrappedAdapter();
		} else {
			return adapter;
		}
	}

	protected void setOnItemClickListener(AdapterView.OnItemClickListener childClickListener){
		((ListView)findViewById(android.R.id.list)).setOnItemClickListener(childClickListener);
	}

// --Commented out by Inspection START (10/01/19 21:03):
//	public boolean isLightActionBar() {
//		return ((OsmandApplication) getApplication()).getSettings().isLightActionBar();
//	}
// --Commented out by Inspection STOP (10/01/19 21:03)

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}
}
