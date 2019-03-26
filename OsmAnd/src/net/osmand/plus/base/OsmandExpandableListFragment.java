package net.osmand.plus.base;

import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.OsmandActionBarActivity;

public abstract class OsmandExpandableListFragment extends BaseOsmAndFragment
		implements OnChildClickListener {
	protected ExpandableListView listView;
	protected ExpandableListAdapter adapter;
	
	@Override
	public View onCreateView(@NonNull android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
		View v = createView(inflater, container);
		listView = v.findViewById(android.R.id.list);
		listView.setOnChildClickListener(this);
		if(this.adapter != null) {
			listView.setAdapter(this.adapter);
		}
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getExpandableListView().setBackgroundColor(
				getResources().getColor(
						getMyApplication().getSettings().isLightContent() ? R.color.bg_color_light
								: R.color.bg_color_dark));
	}

	private View createView(android.view.LayoutInflater inflater, android.view.ViewGroup container) {
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.expandable_list, container, false);
	}
	
	protected void setAdapter(ExpandableListAdapter a) {
		this.adapter = a;
		if(listView != null) {
			listView.setAdapter(a);
		}
		
	}
	
	protected void fixBackgroundRepeat(View view) {
		Drawable bg = view.getBackground();
		if (bg != null) {
			if (bg instanceof BitmapDrawable) {
				BitmapDrawable bmp = (BitmapDrawable) bg;
				bmp.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
			}
		}
	}

	protected ExpandableListView getExpandableListView() {
		return listView;
	}

	protected void setListView(ExpandableListView listView) {
		this.listView = listView;
		listView.setOnChildClickListener(this);
	}

	protected MenuItem createMenuItem(Menu m, int id, int titleRes, int iconLight, int iconDark, int menuItemType) {
		int r = isLightActionBar() ? iconLight : iconDark;
		MenuItem menuItem = m.add(0, id, 0, titleRes);
		if (r != 0) {
			menuItem.setIcon(r);
		}
		menuItem.setOnMenuItemClickListener(item -> onOptionsItemSelected(item));
		menuItem.setShowAsAction(menuItemType);
		return menuItem;
	}

	protected boolean isLightActionBar() {
		return ((OsmandApplication) getActivity().getApplication()).getSettings().isLightActionBar();
	}

	protected void collapseTrees(final int count) {
		getActivity().runOnUiThread(() -> {
			synchronized (adapter) {
				final ExpandableListView expandableListView = getExpandableListView();
				for (int i = 0; i < adapter.getGroupCount(); i++) {
					int cp = adapter.getChildrenCount(i);
					if (cp < count) {
						expandableListView.expandGroup(i);
					} else {
						expandableListView.collapseGroup(i);
					}
				}
			}
		});

	}

	protected OsmandActionBarActivity getActionBarActivity() {
		if (getActivity() instanceof OsmandActionBarActivity) {
			return (OsmandActionBarActivity) getActivity();
		}
		return null;
	}
}
