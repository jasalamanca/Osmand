package net.osmand.plus.activities.actions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.Version;

public class ShareDialog {
	private static final String ZXING_BARCODE_SCANNER_COMPONENT = "com.google.zxing.client.android"; //$NON-NLS-1$
	private static final String ZXING_BARCODE_SCANNER_ACTIVITY = "com.google.zxing.client.android.ENCODE"; //$NON-NLS-1$

	public static void sendMessage(Activity a, String msg) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, msg);
		intent.setType("text/plain");
		a.startActivity(Intent.createChooser(intent, a.getString(R.string.send_location)));
	}
	
	public static void sendQRCode(final Activity activity, String encodeType, Bundle encodeData, String strEncodeData) {
		Intent intent = new Intent();
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setAction(ZXING_BARCODE_SCANNER_ACTIVITY);
		ResolveInfo resolved = activity.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (resolved != null) {
			intent.putExtra("ENCODE_TYPE", encodeType);
			if(strEncodeData != null ) {
				intent.putExtra("ENCODE_DATA", strEncodeData);
			} else {
				intent.putExtra("ENCODE_DATA", encodeData);
			}
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			activity.startActivity(intent);
		} else {
			if (Version.isMarketEnabled((OsmandApplication) activity.getApplication())) {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(activity.getString(R.string.zxing_barcode_scanner_not_found));
				builder.setPositiveButton(activity.getString(R.string.shared_string_yes), (dialog, which) -> {
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(Version.getUrlWithUtmRef((OsmandApplication) activity.getApplication(), ZXING_BARCODE_SCANNER_COMPONENT)));
                    try {
                        activity.startActivity(intent1);
                    } catch (ActivityNotFoundException e) {
                    }
                });
				builder.setNegativeButton(activity.getString(R.string.shared_string_no), null);
				builder.show();
			} else {
				Toast.makeText(activity, R.string.zxing_barcode_scanner_not_found, Toast.LENGTH_LONG).show();
			}
		}
	}

	public static void sendToClipboard(Activity activity, String text) {
		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText("", text));
		Toast.makeText(activity, R.string.copied_to_clipboard, Toast.LENGTH_LONG).show();
	}
}
