package net.osmand.plus.dashboard;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.osmand.IndexConstants;
import net.osmand.ValueHolder;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.ProgressImplementation;
import net.osmand.plus.R;
import net.osmand.plus.download.DownloadActivity;
import net.osmand.util.Algorithms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gnu.trove.list.array.TIntArrayList;

public class DashChooseAppDirFragment {
	public static class ChooseAppDirFragment {
		private TextView locationPath;
		private TextView locationDesc;
		final MessageFormat formatGb = new MessageFormat("{0, number,#.##} GB", Locale.US);
		private View copyMapsBtn;
		private ImageView editBtn;
		private View confirmBtn;
		private boolean mapsCopied = false;
		private TextView warningReadonly;
		private int type = -1;
		private File selectedFile = new File("/");
		private File currentAppFile;
		private OsmandSettings settings;
		private final Activity activity;
		private Dialog dlg;

		private static int typeTemp = -1;
		private static String selectePathTemp;

		protected ChooseAppDirFragment(Activity activity, Dialog dlg) {
			this.activity = activity;
			this.dlg = dlg;
		}

		public void setPermissionDenied() {
			typeTemp = -1;
			selectePathTemp = null;
		}

		private String getFreeSpace(File dir) {
			if (dir.canRead()) {
				StatFs fs = new StatFs(dir.getAbsolutePath());
				return formatGb
						.format(new Object[] { (float) (fs.getAvailableBlocks()) * fs.getBlockSize() / (1 << 30) });
			}
			return "";
		}

		void updateView() {
			if (type == OsmandSettings.EXTERNAL_STORAGE_TYPE_INTERNAL_FILE) {
				locationPath.setText(R.string.storage_directory_internal_app);
			} else if (type == OsmandSettings.EXTERNAL_STORAGE_TYPE_DEFAULT) {
				locationPath.setText(R.string.storage_directory_shared);
			} else if (type == OsmandSettings.EXTERNAL_STORAGE_TYPE_EXTERNAL_FILE) {
				locationPath.setText(R.string.storage_directory_external);
			} else if (type == OsmandSettings.EXTERNAL_STORAGE_TYPE_OBB) {
				locationPath.setText(R.string.storage_directory_multiuser);
			} else if (type == OsmandSettings.EXTERNAL_STORAGE_TYPE_SPECIFIED) {
				locationPath.setText(R.string.storage_directory_manual);
			}
			locationDesc.setText(selectedFile.getAbsolutePath() + " \u2022 " + getFreeSpace(selectedFile));
			boolean copyFiles = !currentAppFile.getAbsolutePath().equals(selectedFile.getAbsolutePath()) && !mapsCopied;
			if (copyFiles) {
				copyFiles = false;
				File[] lf = currentAppFile.listFiles();
				if (lf != null) {
					for (File f : lf) {
						if (f != null) {
							if (f.getName().endsWith(IndexConstants.BINARY_MAP_INDEX_EXT)) {
								copyFiles = true;
								break;
							}
						}
					}
				}
			}
			warningReadonly.setVisibility(copyFiles ? View.VISIBLE : View.GONE);
			if (copyFiles) {
				if (!OsmandSettings.isWritable(currentAppFile)) {
					warningReadonly.setText(activity.getString(R.string.android_19_location_disabled,
							currentAppFile.getAbsolutePath()));
				} else {
					warningReadonly.setText(getString(R.string.application_dir_change_warning3));
				}
			}

			copyMapsBtn.setVisibility(copyFiles ? View.VISIBLE : View.GONE);
		}

		public View initView(LayoutInflater inflater, ViewGroup container) {
			View view = inflater.inflate(R.layout.dash_storage_type_fragment, container, false);
			settings = getMyApplication().getSettings();
			locationPath = view.findViewById(R.id.location_path);
			locationDesc = view.findViewById(R.id.location_desc);
			warningReadonly = view.findViewById(R.id.android_19_location_changed);
			currentAppFile = settings.getExternalStorageDirectory();
			selectedFile = currentAppFile;
			if (settings.getExternalStorageDirectoryTypeV19() >= 0) {
				type = settings.getExternalStorageDirectoryTypeV19();
			} else {
				ValueHolder<Integer> vh = new ValueHolder<>();
				settings.getExternalStorageDirectory(vh);
				if (vh.value != null && vh.value >= 0) {
					type = vh.value;
				} else {
					type = 0;
				}
			}
			editBtn = view.findViewById(R.id.edit_icon);
			copyMapsBtn = view.findViewById(R.id.copy_maps);
			confirmBtn = view.findViewById(R.id.confirm);
			addListeners();
			processPermissionGranted();
			updateView();
			return view;
		}
		
		String getString(int string) {
			return activity.getString(string);
		}

		@TargetApi(Build.VERSION_CODES.KITKAT)
		void showSelectDialog19() {
			AlertDialog.Builder editalert = new AlertDialog.Builder(activity);
			editalert.setTitle(R.string.application_dir);
			final List<String> items = new ArrayList<>();
			final List<String> paths = new ArrayList<>();
			final TIntArrayList types = new TIntArrayList();
			int selected = -1;
			if (type == OsmandSettings.EXTERNAL_STORAGE_TYPE_SPECIFIED) {
				items.add(getString(R.string.storage_directory_manual));
				paths.add(selectedFile.getAbsolutePath());
				types.add(OsmandSettings.EXTERNAL_STORAGE_TYPE_SPECIFIED);
			}
			File df = settings.getDefaultInternalStorage();
			if (type == OsmandSettings.EXTERNAL_STORAGE_TYPE_DEFAULT) {
				selected = items.size();
			}
			items.add(getString(R.string.storage_directory_shared));
			paths.add(df.getAbsolutePath());
			types.add(OsmandSettings.EXTERNAL_STORAGE_TYPE_DEFAULT);

			File[] externals = getMyApplication().getExternalFilesDirs(null);
			if (externals != null) {
				int i = 1;
				for (File external : externals) {
					if (external != null) {
						if (selectedFile.getAbsolutePath().equals(external.getAbsolutePath())) {
							selected = items.size();
						}
						items.add(getString(R.string.storage_directory_external) + " " + (i++));
						paths.add(external.getAbsolutePath());
						types.add(OsmandSettings.EXTERNAL_STORAGE_TYPE_EXTERNAL_FILE);
					}
				}
			}

			File[] obbDirs = getMyApplication().getObbDirs();
			if (obbDirs != null) {
				int i = 1;
				for (File obb : obbDirs) {
					if (obb != null) {
						if (selectedFile.getAbsolutePath().equals(obb.getAbsolutePath())) {
							selected = items.size();
						}
						items.add(getString(R.string.storage_directory_multiuser) + " " + (i++));
						paths.add(obb.getAbsolutePath());
						types.add(OsmandSettings.EXTERNAL_STORAGE_TYPE_OBB);
					}
				}
			}

			String pth = settings.getInternalAppPath().getAbsolutePath();
			if (selectedFile.getAbsolutePath().equals(pth)) {
				selected = items.size();
			}
			items.add(getString(R.string.storage_directory_internal_app));
			paths.add(pth);
			types.add(OsmandSettings.EXTERNAL_STORAGE_TYPE_INTERNAL_FILE);

			items.add(getString(R.string.storage_directory_manual) + getString(R.string.shared_string_ellipsis));
			paths.add("");
			types.add(OsmandSettings.EXTERNAL_STORAGE_TYPE_SPECIFIED);

			editalert.setSingleChoiceItems(items.toArray(new String[0]), selected,
					(dialog, which) -> {
						if (which == items.size() - 1) {
							dialog.dismiss();
							showOtherDialog();
						} else {

							if (types.get(which) == OsmandSettings.EXTERNAL_STORAGE_TYPE_DEFAULT
									&& !DownloadActivity.hasPermissionToWriteExternalStorage(activity)) {

								typeTemp = types.get(which);
								selectePathTemp = paths.get(which);
								dialog.dismiss();
								if (dlg != null) {
									dlg.dismiss();
								}

								ActivityCompat.requestPermissions(activity,
										new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
										DownloadActivity.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

							} else {
								mapsCopied = false;
								type = types.get(which);
								selectedFile = new File(paths.get(which));
								dialog.dismiss();
								updateView();
							}
						}
					});
			editalert.setNegativeButton(R.string.shared_string_dismiss, null);
			editalert.show();
		}

		private void processPermissionGranted() {
			if (typeTemp != -1 && selectePathTemp != null) {
				mapsCopied = false;
				type = typeTemp;
				selectedFile = new File(selectePathTemp);

				typeTemp = -1;
				selectePathTemp = null;
			}
		}

		void showOtherDialog() {
			AlertDialog.Builder editalert = new AlertDialog.Builder(activity);
			editalert.setTitle(R.string.application_dir);
			final EditText input = new EditText(activity);
			input.setText(selectedFile.getAbsolutePath());
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			lp.leftMargin = lp.rightMargin = 5;
			lp.bottomMargin = lp.topMargin = 5;
			input.setLayoutParams(lp);
			settings.getExternalStorageDirectory().getAbsolutePath();
			editalert.setView(input);
			editalert.setNegativeButton(R.string.shared_string_cancel, null);
			editalert.setPositiveButton(R.string.shared_string_ok, (dialog, whichButton) -> {
				selectedFile = new File(input.getText().toString());
				mapsCopied = false;
				updateView();
			});
			editalert.show();
		}

		private void addListeners() {
			editBtn.setOnClickListener(v -> {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
					showOtherDialog();
				} else {
					showSelectDialog19();
				}
			});
			copyMapsBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MoveFilesToDifferentDirectory task = new MoveFilesToDifferentDirectory(activity, currentAppFile,
							selectedFile) {

						@Override
						protected void onPostExecute(Boolean result) {
							super.onPostExecute(result);
							if (result) {
								mapsCopied = true;
								getMyApplication().getResourceManager().resetStoreDirectory();
							} else {
								Toast.makeText(activity, R.string.copying_osmand_file_failed,
										Toast.LENGTH_SHORT).show();
							}
							updateView();
						}
					};
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			});
			confirmBtn.setOnClickListener(getConfirmListener());
		}

		OnClickListener getConfirmListener() {
			return v -> {
				boolean wr = OsmandSettings.isWritable(selectedFile);
				if (wr) {
					boolean changed = !currentAppFile.getAbsolutePath().equals(selectedFile.getAbsolutePath());
					getMyApplication().setExternalStorageDirectory(type, selectedFile.getAbsolutePath());
					if (changed) {
						successCallback();
						reloadData();
					}
				} else {
					Toast.makeText(activity, R.string.specified_directiory_not_writeable,
							Toast.LENGTH_LONG).show();
				}
				if(dlg != null) {
					dlg.dismiss();
				}
			};
		}

		// To be implemented by subclass
		protected void successCallback() {}

		void reloadData() {
			new ReloadData(activity, getMyApplication()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
		}

		OsmandApplication getMyApplication() {
			if (activity == null) {
				return null;
			}
			return (OsmandApplication) activity.getApplication();
		}

		public void setDialog(Dialog dlg) {
			this.dlg = dlg;
		}
	}
	
	public static class MoveFilesToDifferentDirectory extends AsyncTask<Void, Void, Boolean> {
		private final File to;
		private final Context ctx;
		private final File from;
		ProgressImplementation progress;
		private Runnable runOnSuccess;

		public MoveFilesToDifferentDirectory(Context ctx, File from, File to) {
			this.ctx = ctx;
			this.from = from;
			this.to = to;
		}
		
		public void setRunOnSuccess(Runnable runOnSuccess) {
			this.runOnSuccess = runOnSuccess;
		}
		
		@Override
		protected void onPreExecute() {
			progress = ProgressImplementation.createProgressDialog(
					ctx, ctx.getString(R.string.copying_osmand_files),
					ctx.getString(R.string.copying_osmand_files_descr, to.getPath()),
					ProgressDialog.STYLE_HORIZONTAL);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result != null) {
				if (result && runOnSuccess != null) {
					runOnSuccess.run();
				} else if (!result) {
					Toast.makeText(ctx, R.string.shared_string_io_error, Toast.LENGTH_LONG).show();
				}
			}
			try {
				if (progress.getDialog().isShowing()) {
					progress.getDialog().dismiss();
				}
			} catch (Exception e) {
				//ignored
			}
		}
		
		private void movingFiles(File f, File t, int depth) throws IOException {
			if(depth <= 2) {
				progress.startTask(ctx.getString(R.string.copying_osmand_one_file_descr, t.getName()), -1);
			}
			if (f.isDirectory()) {
				t.mkdirs();
				File[] lf = f.listFiles();
				if (lf != null) {
					for (File aLf : lf) {
						if (aLf != null) {
							movingFiles(aLf, new File(t, aLf.getName()), depth + 1);
						}
					}
				}
				f.delete();
			} else if (f.isFile()) {
				if(t.exists()) {
					Algorithms.removeAllFiles(t);
				}
				boolean rnm = false;
				try {
					rnm = f.renameTo(t);
				} catch(RuntimeException e) {
				}
				if (!rnm) {
					FileInputStream fin = new FileInputStream(f);
					FileOutputStream fout = new FileOutputStream(t);
					try {
						progress.startTask(ctx.getString(R.string.copying_osmand_one_file_descr, t.getName()), (int) (f.length() / 1024));
						Algorithms.streamCopy(fin, fout, progress, 1024);
					} finally {
						fin.close();
						fout.close();
					}
					f.delete();
				}
			}
			if(depth <= 2) {
				progress.finishTask();
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			to.mkdirs();
			try {
				movingFiles(from, to, 0);
			} catch (IOException e) {
				return false;
			}
			return true;
		}
		
	}
	
	public static class ReloadData extends AsyncTask<Void, Void, Boolean> {
		private final Context ctx;
		ProgressImplementation progress;
		private final OsmandApplication app;

		public ReloadData(Context ctx, OsmandApplication app) {
			this.ctx = ctx;
			this.app = app;
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressImplementation.createProgressDialog(ctx, ctx.getString(R.string.loading_data),
					ctx.getString(R.string.loading_data), ProgressDialog.STYLE_HORIZONTAL);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			try {
				if (progress.getDialog().isShowing()) {
					progress.getDialog().dismiss();
				}
			} catch (Exception e) {
				//ignored
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			app.getResourceManager().reloadIndexes(progress, new ArrayList<>());
			return true;
		}
	}
}