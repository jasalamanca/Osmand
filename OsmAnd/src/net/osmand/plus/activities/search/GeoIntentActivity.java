package net.osmand.plus.activities.search;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import net.osmand.data.PointDescription;
import net.osmand.plus.AppInitializer;
import net.osmand.plus.AppInitializer.AppInitializeListener;
import net.osmand.plus.AppInitializer.InitEvents;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.OsmandListActivity;
import net.osmand.util.Algorithms;
import net.osmand.util.GeoPointParserUtil;
import net.osmand.util.GeoPointParserUtil.GeoParsedPoint;

public class GeoIntentActivity extends OsmandListActivity {
	private ProgressDialog progressDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_address_offline);
		getSupportActionBar().setTitle(R.string.search_osm_offline);
		
		getMyApplication().checkApplicationIsBeingInitialized(new AppInitializeListener() {
			@Override
			public void onProgress(AppInitializer init, InitEvents event) {
			}
			
			@Override
			public void onFinish() {
			}
		});

		final Intent intent = getIntent();
		if (intent != null) {
			final ProgressDialog progress = ProgressDialog.show(GeoIntentActivity.this, getString(R.string.searching),
					getString(R.string.searching_address));
			final GeoIntentTask task = new GeoIntentTask(progress, intent);

			progress.setOnCancelListener(dialog -> task.cancel(true));
			progress.setCancelable(true);

			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			setIntent(null);
		}
	}

	private class GeoIntentTask extends AsyncTask<Void, Void, GeoParsedPoint> {
		private final ProgressDialog progress;
		private final Intent intent;

		private GeoIntentTask(final ProgressDialog progress, final Intent intent) {
			this.progress = progress;
			this.intent = intent;
		}

		@Override
		protected void onPreExecute() {
		}

		/**
		 * Extracts information from geo and map intents:
		 * 
		 * geo:47.6,-122.3<br/>
		 * geo:47.6,-122.3?z=11<br/>
		 * geo:0,0?q=34.99,-106.61(Treasure)<br/>
		 * geo:0,0?q=1600+Amphitheatre+Parkway%2C+CA<br/>
		 */
		@Override
		protected GeoParsedPoint doInBackground(Void... nothing) {
			try {
				while (getMyApplication().isApplicationInitializing()) {
					Thread.sleep(200);
				}
				Uri uri = intent.getData();
				return GeoPointParserUtil.parse(uri.toString());
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(GeoPointParserUtil.GeoParsedPoint p ) {
			if (progress != null && progress.isShowing()) {
				try {
					progress.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				OsmandSettings settings = getMyApplication().getSettings();
				if (p != null && p.isGeoPoint()) {
					PointDescription pd = new PointDescription(p.getLatitude(), p.getLongitude());
					if (!Algorithms.isEmpty(p.getLabel())) {
						pd.setName(p.getLabel());
					}
					settings.setMapLocationToShow(p.getLatitude(), p.getLongitude(),
							settings.getLastKnownMapZoom(), pd); //$NON-NLS-1$
					MapActivity.launchMapActivityMoveToTop(GeoIntentActivity.this);
				} else {
					Uri uri = intent.getData();
					String searchString = p != null && p.isGeoAddress() ? p.getQuery() : uri.toString();
					settings.setSearchRequestToShow(searchString);
					MapActivity.launchMapActivityMoveToTop(GeoIntentActivity.this);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void onStop() {
		dismiss();
		super.onStop();
	}

	private void dismiss() {
		if (progressDlg != null) {
			progressDlg.dismiss();
			progressDlg = null;
		}
	}
}