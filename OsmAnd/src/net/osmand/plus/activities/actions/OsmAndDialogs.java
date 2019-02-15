package net.osmand.plus.activities.actions;

import android.app.Activity;
import android.app.Dialog;

import java.util.HashMap;
import java.util.Map;

public class OsmAndDialogs {
	private static final Map<Integer, OsmAndAction> dialogActions = new HashMap<>();

	public static Dialog createDialog(int dialogID, Activity activity) {
		OsmAndAction action = dialogActions.get(dialogID);
		if(action != null) {
			return action.createDialog(activity);
		}
		return null;
	}


	static void registerDialogAction(OsmAndAction action) {
		if(action.getDialogID() != 0) {
			dialogActions.put(action.getDialogID(), action);
		}
	}

	static final int DIALOG_START_GPS = 207;
}