package net.osmand.plus.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;

public abstract class OsmandExpandableListActivity extends
		ActionBarProgressActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}
	
	protected void onStart() {
		super.onStart();
		getExpandableListView().setBackgroundColor(
				getResources().getColor(
						getMyApplication().getSettings().isLightContent() ? R.color.bg_color_light
								: R.color.bg_color_dark));
	}

	public OsmandApplication getMyApplication() {
		return (OsmandApplication) getApplication();
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

	private ExpandableListView getExpandableListView() {
		return (ExpandableListView) findViewById(android.R.id.list);
	}
}
