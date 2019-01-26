package net.osmand.plus.activities.actions;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.OsmandMapTileView;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

class OsmAndAction {

	final MapActivity mapActivity;

	OsmAndAction(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
		OsmAndDialogs.registerDialogAction(this);
	}
	
	public MapActivity getMapActivity() {
		return mapActivity;
	}
	
	public OsmandMapTileView getMapView() {
		return mapActivity.getMapView();
	}
	
	OsmandSettings getSettings() {
		return mapActivity.getMyApplication().getSettings();
	}
	
	OsmandApplication getMyApplication() {
		return mapActivity.getMyApplication();
	}
	
	protected String getString(int res) {
		return mapActivity.getString(res);
	}

	
	public void run() {
	}
	
	public int getDialogID() {
		return 0;
	}
	
	public Dialog createDialog(Activity activity, Bundle args) {
		return null;
	}
	
	public void prepareDialog(Activity activity, Bundle args, Dialog dlg) {
	}

	void showDialog() {
		mapActivity.showDialog(getDialogID());		
	}
}
