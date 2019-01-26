package net.osmand.plus.mapcontextmenu.editors;

import android.support.v4.app.Fragment;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.AndroidUiHelper;

public abstract class PointEditor {

	final OsmandApplication app;
	MapActivity mapActivity;

	boolean isNew;

	private boolean portraitMode;
	private boolean nightMode;

	PointEditor(MapActivity mapActivity) {
		this.app = mapActivity.getMyApplication();
		this.mapActivity = mapActivity;
		updateLandscapePortrait();
		updateNightMode();
	}

	public void setMapActivity(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
	}
	public boolean isNew() {
		return isNew;
	}
	public boolean isLight() {
		return !nightMode;
	}

	public void updateLandscapePortrait() {
		portraitMode = AndroidUiHelper.isOrientationPortrait(mapActivity);
	}

	public void updateNightMode() {
		nightMode = mapActivity.getMyApplication().getDaynightHelper().isNightModeForMapControls();
	}

	public abstract String getFragmentTag();

	public void setCategory(String name) {
		Fragment fragment = mapActivity.getSupportFragmentManager().findFragmentByTag(getFragmentTag());
		if (fragment != null) {
			PointEditorFragment editorFragment = (PointEditorFragment) fragment;
			editorFragment.setCategory(name);
		}
	}
}
