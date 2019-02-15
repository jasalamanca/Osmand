package net.osmand.plus.activities.actions;

import android.app.Activity;
import android.app.Dialog;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.activities.MapActivity;

class OsmAndAction {
	final MapActivity mapActivity;

	OsmAndAction(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
		OsmAndDialogs.registerDialogAction(this);
	}

	OsmandSettings getSettings() {
		return mapActivity.getMyApplication().getSettings();
	}
	OsmandApplication getMyApplication() {
		return mapActivity.getMyApplication();
	}

	public int getDialogID() {
		return 0;
	}
	
	public Dialog createDialog(Activity activity) {
		return null;
	}

	void showDialog() {
		mapActivity.showDialog(getDialogID());		
	}
}