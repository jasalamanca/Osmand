package net.osmand.plus.download;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.StatFs;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Toast;

import net.osmand.IndexConstants;
import net.osmand.PlatformUtil;
import net.osmand.map.WorldRegion;
import net.osmand.map.WorldRegion.RegionParams;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.OsmandSettings.OsmandPreference;
import net.osmand.plus.R;
import net.osmand.plus.Version;
import net.osmand.plus.base.BasicProgressAsyncTask;
import net.osmand.plus.download.DownloadFileHelper.DownloadFileShowWarning;
import net.osmand.plus.helpers.DatabaseHelper;
import net.osmand.plus.resources.ResourceManager;
import net.osmand.util.Algorithms;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DownloadIndexesThread {
	private final static Log LOG = PlatformUtil.getLog(DownloadIndexesThread.class);
	private static final int NOTIFICATION_ID = 45;
	private final OsmandApplication app;

	private DownloadEvents uiActivity = null;
	private final DatabaseHelper dbHelper;
	private final DownloadFileHelper downloadFileHelper;
	private final List<BasicProgressAsyncTask<?, ?, ?, ?>> currentRunningTask = Collections.synchronizedList(new ArrayList<>());
	private final ConcurrentLinkedQueue<IndexItem> indexItemDownloading = new ConcurrentLinkedQueue<>();
	private IndexItem currentDownloadingItem = null;
	private int currentDownloadingItemProgress = 0;

	private DownloadResources indexes;
	private Notification notification;
	
	public interface DownloadEvents {
		void newDownloadIndexes();
		void downloadInProgress();
		void downloadHasFinished();
	}

	public DownloadIndexesThread(OsmandApplication app) {
		this.app = app;
		indexes = new DownloadResources(app);
		updateLoadedFiles();
		downloadFileHelper = new DownloadFileHelper(app);
		dbHelper = new DatabaseHelper(app);
	}

	public void updateLoadedFiles() {
		indexes.updateLoadedFiles();
	}

	/// UI notifications methods
	public void setUiActivity(DownloadEvents uiActivity) {
		this.uiActivity = uiActivity;
	}

	public void resetUiActivity(DownloadEvents uiActivity) {
		if (this.uiActivity == uiActivity) {
			this.uiActivity = null;
		}
	}
	
	@UiThread
	private void downloadInProgress() {
		if (uiActivity != null) {
			uiActivity.downloadInProgress();
		}
		updateNotification();
	}
	
	private void updateNotification() {
		if(getCurrentDownloadingItem() != null) {
			BasicProgressAsyncTask<?, ?, ?, ?> task = getCurrentRunningTask();
			final boolean isFinished = task == null
					|| task.getStatus() == AsyncTask.Status.FINISHED;
			Intent contentIntent = new Intent(app, DownloadActivity.class);
			PendingIntent contentPendingIntent = PendingIntent.getActivity(app, 0, contentIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			Notification.Builder bld = new Notification.Builder(app);
			String msg = Version.getAppName(app);
			if(!isFinished) {
				msg = task.getDescription();
			}
			StringBuilder contentText = new StringBuilder();
			List<IndexItem> ii = getCurrentDownloadingItems();
			for(IndexItem i : ii) {
				if(contentText.length() > 0) {
					contentText.append(", ");
				}
				contentText.append(i.getVisibleName(app, app.getRegions()));
			}
			bld.setContentTitle(msg).setSmallIcon(android.R.drawable.stat_sys_download)
					.setContentText(contentText.toString())
					.setContentIntent(contentPendingIntent).setOngoing(true);
			int progress = getCurrentDownloadingItemProgress();
			bld.setProgress(100, Math.max(progress, 0), progress < 0);
			notification = bld.build();
			NotificationManager mNotificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		} else {
			if(notification != null) {
				NotificationManager mNotificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(NOTIFICATION_ID);
				notification = null;
			}
		}
	}

	@UiThread
	private void downloadHasFinished() {
		if (uiActivity != null) {
			uiActivity.downloadHasFinished();
		}
		updateNotification();
	}

	public void initSettingsFirstMap(WorldRegion reg) {
		if (app.getSettings().FIRST_MAP_IS_DOWNLOADED.get() || reg == null) {
			return;
		}
		app.getSettings().FIRST_MAP_IS_DOWNLOADED.set(true);
		RegionParams params = reg.getParams();
		if (!app.getSettings().DRIVING_REGION_AUTOMATIC.get()) {
			app.setupDrivingRegion(reg);
		}
		String lang = params.getRegionLang();
		if (lang != null) {
			String lng = lang.split(",")[0];
			String setTts = null;
			for (String s : OsmandSettings.TTS_AVAILABLE_VOICES) {
				if (lng.startsWith(s)) {
					setTts = s + "-tts";
					break;
				} else if (lng.contains("," + s)) {
					setTts = s + "-tts";
				}
			}
			if (setTts != null) {
				app.getSettings().VOICE_PROVIDER.set(setTts);
			}
		}
	}
	
	@UiThread
	private void newDownloadIndexes() {
		if (uiActivity != null) {
			uiActivity.newDownloadIndexes();
		}
	}

	// PUBLIC API

	public DownloadResources getIndexes() {
		return indexes;
	}
	
	public List<IndexItem> getCurrentDownloadingItems() {
		List<IndexItem> res = new ArrayList<>();
		IndexItem ii = currentDownloadingItem;
		if(ii != null) {
			res.add(ii);
		}
		res.addAll(indexItemDownloading);
		return res;
	}

	public boolean isDownloading(IndexItem item) {
		if(item == currentDownloadingItem) {
			return true;
		}
		for(IndexItem ii : indexItemDownloading) {
			if (ii == item) {
				return true;
			}
		}
		return false;
	}

	int getCountedDownloads() {
		int i = 0;
		if(currentDownloadingItem != null && DownloadActivityType.isCountedInDownloads(currentDownloadingItem)) {
			i++;
		}
		for(IndexItem ii : indexItemDownloading) {
			if (DownloadActivityType.isCountedInDownloads(ii)) {
				i++;
			}
		}
		return i;
	}

	public void runReloadIndexFilesSilent() {
		if (checkRunning(true)) {
			return;
		}
		execute(new ReloadIndexesTask());
	}

	public void runReloadIndexFiles() {
		if (checkRunning(false)) {
			return;
		}
		execute(new ReloadIndexesTask());
	}

	public void runDownloadFiles(IndexItem... items) {
		if (getCurrentRunningTask() instanceof ReloadIndexesTask) {
			if(checkRunning(false)) {
				return;
			}	
		}
		if(uiActivity instanceof Activity) {
			app.logEvent((Activity) uiActivity, "download_files");
		}
		for(IndexItem item : items) {
			if (!item.equals(currentDownloadingItem) && !indexItemDownloading.contains(item)) {
				indexItemDownloading.add(item);
			}
		}
		if (currentDownloadingItem == null) {
			execute(new DownloadIndexesAsyncTask());
		} else {
			downloadInProgress();
		}
	}

	public void cancelDownload(IndexItem item) {
		if(currentDownloadingItem == item) {
			downloadFileHelper.setInterruptDownloading(true);
		} else {
			indexItemDownloading.remove(item);
			downloadInProgress();
		}
	}

	public IndexItem getCurrentDownloadingItem() {
		return currentDownloadingItem;
	}
	public int getCurrentDownloadingItemProgress() {
		return currentDownloadingItemProgress;
	}

	BasicProgressAsyncTask<?, ?, ?, ?> getCurrentRunningTask() {
		for (int i = 0; i < currentRunningTask.size(); ) {
			if (currentRunningTask.get(i).getStatus() == Status.FINISHED) {
				currentRunningTask.remove(i);
			} else {
				i++;
			}
		}
		if (currentRunningTask.size() > 0) {
			return currentRunningTask.get(0);
		}
		return null;
	}

	public double getAvailableSpace() {
		File dir = app.getAppPath("").getParentFile();
		double asz = -1;
		if (dir.canRead()) {
			StatFs fs = new StatFs(dir.getAbsolutePath());
			asz = (((long) fs.getAvailableBlocks()) * fs.getBlockSize()) / (1 << 20);
		}
		return asz;
	}
	
	/// PRIVATE IMPL

	private boolean checkRunning(boolean silent) {
		if (getCurrentRunningTask() != null) {
			if (!silent) {
				Toast.makeText(app, R.string.wait_current_task_finished, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return false;
	}

	private <P> void execute(BasicProgressAsyncTask<?, P, ?, ?> task, P... indexItems) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, indexItems);
	}

	private class ReloadIndexesTask
            extends BasicProgressAsyncTask<Void, Void, Void, DownloadResources> {
		ReloadIndexesTask() {
			super(app);
		}

		@Override
		protected void onPreExecute() {
			currentRunningTask.add(this);
			super.onPreExecute();
			this.message = ctx.getString(R.string.downloading_list_indexes);
			indexes.downloadFromInternetFailed = false;
		}

		@Override
		protected DownloadResources doInBackground(Void... params) {
			DownloadResources result = null;
			DownloadOsmandIndexesHelper.IndexFileList indexFileList = DownloadOsmandIndexesHelper.getIndexesList(ctx);
			if (indexFileList != null) {
				try {
					while (app.isApplicationInitializing()) {
						Thread.sleep(200);
					}
					result = new DownloadResources(app);
					result.isDownloadedFromInternet = indexFileList.isDownloadedFromInternet();
					result.mapVersionIsIncreased = indexFileList.isIncreasedMapVersion();
					app.getSettings().LAST_CHECKED_UPDATES.set(System.currentTimeMillis());
					result.prepareData(indexFileList.getIndexFiles());
				} catch (Exception e) {
				}
			}
			return result == null ? new DownloadResources(app) : result;
		}

		protected void onPostExecute(DownloadResources result) {
			indexes = result;
			result.downloadFromInternetFailed = !result.isDownloadedFromInternet;
			if (result.mapVersionIsIncreased) {
				showWarnDialog();
			}
			currentRunningTask.remove(this);
			newDownloadIndexes();
		}

		private void showWarnDialog() {
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setMessage(R.string.map_version_changed_info);
			builder.setPositiveButton(R.string.button_upgrade_osmandplus, (dialog, which) -> {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Version.getUrlWithUtmRef(app, "net.osmand.plus")));
				try {
					ctx.startActivity(intent);
				} catch (ActivityNotFoundException e) {
				}
			});
			builder.setNegativeButton(R.string.shared_string_cancel, (dialog, which) -> dialog.dismiss());
			builder.show();

		}

		@Override
		protected void updateProgress() {
			downloadInProgress();
		}
	}

	private class DownloadIndexesAsyncTask
			extends BasicProgressAsyncTask<IndexItem, IndexItem, Object, String>
            implements DownloadFileShowWarning {
		private final OsmandPreference<Integer> downloads;

		DownloadIndexesAsyncTask() {
			super(app);
			downloads = app.getSettings().NUMBER_OF_FREE_DOWNLOADS;
		}

        @Override
		protected void onProgressUpdate(Object... values) {
			for (Object o : values) {
				if (o instanceof IndexItem) {
					IndexItem item = (IndexItem) o;
					String name = item.getBasename();
					int count = dbHelper.getCount(name, DatabaseHelper.DOWNLOAD_ENTRY) + 1;
					item.setDownloaded(true);
					DatabaseHelper.HistoryDownloadEntry entry = new DatabaseHelper.HistoryDownloadEntry(name, count);
					if (count == 1) {
						dbHelper.add(entry, DatabaseHelper.DOWNLOAD_ENTRY);
					} else {
						dbHelper.update(entry, DatabaseHelper.DOWNLOAD_ENTRY);
					}
				} else if (o instanceof String) {
					String message = (String) o;
					if (!message.toLowerCase().contains("interrupted") && !message.equals(app.getString(R.string.shared_string_download_successful))) {
						app.showToastMessage(message);
					}
				}
			}
			downloadInProgress();
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPreExecute() {
			currentRunningTask.add(this);
			super.onPreExecute();
			downloadFileHelper.setInterruptDownloading(false);
			if (uiActivity instanceof Activity) {
				View mainView = ((Activity) uiActivity).findViewById(R.id.MainLayout);
				if (mainView != null) {
					mainView.setKeepScreenOn(true);
				}
			}
			startTask(ctx.getString(R.string.shared_string_downloading) + ctx.getString(R.string.shared_string_ellipsis), -1);
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null && result.length() > 0) {
				Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
			}
			if (uiActivity instanceof Activity) {
				View mainView = ((Activity) uiActivity).findViewById(R.id.MainLayout);
				if (mainView != null) {
					mainView.setKeepScreenOn(false);
				}
			}
			currentRunningTask.remove(this);
			indexes.updateFilesToUpdate();
			downloadHasFinished();
		}

		@Override
		protected String doInBackground(IndexItem... filesToDownload) {
			List<File> filesToReindex = new ArrayList<>();
			boolean forceWifi = downloadFileHelper.isWifiConnected();
			Set<IndexItem> currentDownloads = new HashSet<>();
			StringBuilder warn = new StringBuilder();
			try {
				while (!indexItemDownloading.isEmpty()) {
					IndexItem item = indexItemDownloading.poll();
					currentDownloadingItem = item;
					currentDownloadingItemProgress = 0;
					if (currentDownloads.contains(item)) {
						continue;
					}
					currentDownloads.add(item);
					if(!validateEnoughSpace(item)) {
						break;
					}
					if(!validateNotExceedsFreeLimit(item)) {
						break;
					}
					boolean result = downloadFile(item, filesToReindex, forceWifi);
					if (result) {
						if (DownloadActivityType.isCountedInDownloads(item)) {
							downloads.set(downloads.get() + 1);
						}
						File bf = item.getBackupFile(app);
						if (bf.exists()) {
							Algorithms.removeAllFiles(bf);
						}
						publishProgress(item);
						String wn = reindexFiles(filesToReindex);
						if(!Algorithms.isEmpty(wn)) {
							warn.append(" ").append(wn);
						}
						filesToReindex.clear();
						// slow down but let update all button work properly
						indexes.updateFilesToUpdate();
					}
				}
			} finally {
				currentDownloadingItem = null;
				currentDownloadingItemProgress = 0;
			}
			if(warn.toString().trim().length() == 0) {
				return null;
			}
			return warn.toString().trim();
		}

		private boolean validateEnoughSpace(IndexItem item) {
			double asz = getAvailableSpace();
			double cs =(item.contentSize / (1 << 20));
			// validate enough space
			if (asz != -1 && cs > asz) {
				String breakDownloadMessage = app.getString(R.string.download_files_not_enough_space, cs, asz);
				publishProgress(breakDownloadMessage);
				return false;
			}
			return true;
		}
		
		private boolean validateNotExceedsFreeLimit(IndexItem item) {
			boolean exceed = Version.isFreeVersion(app)
					&& !app.getSettings().LIVE_UPDATES_PURCHASED.get()
					&& !app.getSettings().FULL_VERSION_PURCHASED.get()
					&& DownloadActivityType.isCountedInDownloads(item)
					&& downloads.get() >= DownloadValidationManager.MAXIMUM_AVAILABLE_FREE_DOWNLOADS;
			if(exceed) {
				String breakDownloadMessage = app.getString(R.string.free_version_message,
						DownloadValidationManager.MAXIMUM_AVAILABLE_FREE_DOWNLOADS + "");
				publishProgress(breakDownloadMessage);
			}
			return !exceed;
		}

		private String reindexFiles(List<File> filesToReindex) {
			boolean vectorMapsToReindex = false;
			// reindex vector maps all at one time
			ResourceManager manager = app.getResourceManager();
			for (File f : filesToReindex) {
				if (f.getName().endsWith(IndexConstants.BINARY_MAP_INDEX_EXT)) {
					vectorMapsToReindex = true;
				}
			}
			List<String> warnings = new ArrayList<>();
			manager.indexVoiceFiles();
			manager.indexFontFiles();
			if (vectorMapsToReindex) {
				warnings = manager.indexingMaps(this);
			}
//			List<String> wns = manager.indexAdditionalMaps(this);
//			if (wns != null) {
//				warnings.addAll(wns);
//			}

			if (!warnings.isEmpty()) {
				return warnings.get(0);
			}
			return null;
		}

		@Override
		public void showWarning(String warning) {
			publishProgress(warning);
		}

		boolean downloadFile(IndexItem item, List<File> filesToReindex, boolean forceWifi) {
			downloadFileHelper.setInterruptDownloading(false);
			IndexItem.DownloadEntry de = item.createDownloadEntry(app);
			if(de == null) {
				return false;
			}

			boolean res = false;
			if (de.isAsset) {
				try {
					if (ctx != null) {
						ResourceManager.copyAssets(ctx.getAssets(), de.assetName, de.targetFile);
						boolean changedDate = de.targetFile.setLastModified(de.dateModified);
						if (!changedDate) {
							LOG.error("Set last timestamp is not supported");
						}
						res = true;
					}
				} catch (IOException e) {
					LOG.error("Copy exception", e);
				}
			} else {
				res = downloadFileHelper.downloadFile(de, this, filesToReindex, this, forceWifi);
			}
			return res;
		}

		@Override
		protected void updateProgress() {
			currentDownloadingItemProgress = getProgressPercentage();
			downloadInProgress();
		}
	}
}