package net.osmand.plus.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import net.osmand.plus.AppInitializer;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.Version;

public class WhatsNewDialogFragment extends DialogFragment {
	public static boolean SHOW = true;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final OsmandApplication osmandApplication = (OsmandApplication) getActivity().getApplication();
		final String appVersion = Version.getAppVersion(osmandApplication);
		builder.setTitle(getString(R.string.whats_new) + " " + appVersion)
				.setMessage(getString(R.string.release_2_9))
				.setNegativeButton(R.string.shared_string_close, null);
		builder.setPositiveButton(R.string.read_more, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(AppInitializer.LATEST_CHANGES_URL));
				startActivity(i);
				dismiss();
			}
		});
		return builder.create();
	}
}