package net.osmand.plus.activities;


import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import net.osmand.IndexConstants;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.download.ui.AbstractLoadLocalIndexTask;
import net.osmand.plus.voice.MediaCommandPlayerImpl;
import net.osmand.plus.voice.TTSCommandPlayerImpl;
import net.osmand.util.Algorithms;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class LocalIndexHelper {

	private final OsmandApplication app;

	public LocalIndexHelper(OsmandApplication app) {
		this.app = app;
	}


	private String getInstalledDate(File f) {
		return android.text.format.DateFormat.getMediumDateFormat(app).format(getInstalationDate(f));
	}

	private Date getInstalationDate(File f) {
		final long t = f.lastModified();
		return new Date(t);
	}

	private String getInstalledDate(long t, TimeZone timeZone) {
		return android.text.format.DateFormat.getMediumDateFormat(app).format(new Date(t));
	}

	private void updateDescription(LocalIndexInfo info) {
		File f = new File(info.getPathToData());
		if (info.getType() == LocalIndexType.MAP_DATA) {
			Map<String, String> ifns = app.getResourceManager().getIndexFileNames();
			if (ifns.containsKey(info.getFileName())) {
				try {
					Date dt = app.getResourceManager().getDateFormat().parse(ifns.get(info.getFileName()));
					info.setDescription(getInstalledDate(dt.getTime(), null));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else {
				info.setDescription(getInstalledDate(f));
			}
		} else if (info.getType() == LocalIndexType.WIKI_DATA) {
			info.setDescription(getInstalledDate(f));
		} else if (info.getType() == LocalIndexType.TTS_VOICE_DATA) {
			info.setDescription(getInstalledDate(f));
		} else if (info.getType() == LocalIndexType.DEACTIVATED) {
			info.setDescription(getInstalledDate(f));
		} else if (info.getType() == LocalIndexType.VOICE_DATA) {
			info.setDescription(getInstalledDate(f));
		} else if (info.getType() == LocalIndexType.FONT_DATA) {
			info.setDescription(getInstalledDate(f));
		}
	}

	private LocalIndexInfo getLocalIndexInfo(LocalIndexType type, String downloadName, boolean roadMap, boolean backuped) {

		File fileDir = null;
		String fileName = null;

		if (type == LocalIndexType.MAP_DATA) {
			if (!roadMap) {
				fileDir = app.getAppPath(IndexConstants.MAPS_PATH);
				fileName = Algorithms.capitalizeFirstLetterAndLowercase(downloadName)
						+ IndexConstants.BINARY_MAP_INDEX_EXT;
			} else {
				fileDir = app.getAppPath(IndexConstants.ROADS_INDEX_DIR);
				fileName = Algorithms.capitalizeFirstLetterAndLowercase(downloadName)
						+ IndexConstants.BINARY_ROAD_MAP_INDEX_EXT;
			}
		} else if (type == LocalIndexType.WIKI_DATA) {
			fileDir = app.getAppPath(IndexConstants.WIKI_INDEX_DIR);
			fileName = Algorithms.capitalizeFirstLetterAndLowercase(downloadName)
					+ IndexConstants.BINARY_WIKI_MAP_INDEX_EXT;
		}

		if (backuped) {
			fileDir = app.getAppPath(IndexConstants.BACKUP_INDEX_DIR);
		}

		if (fileDir != null && fileName != null) {
			File f = new File(fileDir, fileName);
			if (f.exists()) {
				LocalIndexInfo info = new LocalIndexInfo(type, f, backuped);
				updateDescription(info);
				return info;
			}
		}

		return null;
	}

	public List<LocalIndexInfo> getLocalIndexInfos(String downloadName) {
		List<LocalIndexInfo> list = new ArrayList<>();
		LocalIndexInfo info = getLocalIndexInfo(LocalIndexType.MAP_DATA, downloadName, false, false);
		if (info != null) {
			list.add(info);
		}
		info = getLocalIndexInfo(LocalIndexType.MAP_DATA, downloadName, true, false);
		if (info != null) {
			list.add(info);
		}
		info = getLocalIndexInfo(LocalIndexType.WIKI_DATA, downloadName, false, false);
		if (info != null) {
			list.add(info);
		}
		info = getLocalIndexInfo(LocalIndexType.MAP_DATA, downloadName, false, true);
		if (info != null) {
			list.add(info);
		}
		info = getLocalIndexInfo(LocalIndexType.MAP_DATA, downloadName, true, true);
		if (info != null) {
			list.add(info);
		}
		info = getLocalIndexInfo(LocalIndexType.WIKI_DATA, downloadName, false, true);
		if (info != null) {
			list.add(info);
		}

		return list;
	}

	public List<LocalIndexInfo> getLocalIndexData(AbstractLoadLocalIndexTask loadTask) {
		Map<String, String> loadedMaps = app.getResourceManager().getIndexFileNames();
		List<LocalIndexInfo> result = new ArrayList<>();

		loadObfData(app.getAppPath(IndexConstants.MAPS_PATH), result, false, loadTask, loadedMaps);
		loadObfData(app.getAppPath(IndexConstants.ROADS_INDEX_DIR), result, false, loadTask, loadedMaps);
		loadWikiData(app.getAppPath(IndexConstants.WIKI_INDEX_DIR), result, loadTask);
		//loadVoiceData(app.getAppPath(IndexConstants.TTSVOICE_INDEX_EXT_ZIP), result, true, loadTask);
		loadVoiceData(app.getAppPath(IndexConstants.VOICE_INDEX_DIR), result, false, loadTask);
		loadFontData(app.getAppPath(IndexConstants.FONT_INDEX_DIR), result, false, loadTask);
		loadObfData(app.getAppPath(IndexConstants.BACKUP_INDEX_DIR), result, true, loadTask, loadedMaps);

		return result;
	}

	public List<LocalIndexInfo> getLocalFullMaps(AbstractLoadLocalIndexTask loadTask) {
		Map<String, String> loadedMaps = app.getResourceManager().getIndexFileNames();
		List<LocalIndexInfo> result = new ArrayList<>();
		loadObfData(app.getAppPath(IndexConstants.MAPS_PATH), result, false, loadTask, loadedMaps);

		return result;
	}

	private void loadVoiceData(File voiceDir, List<LocalIndexInfo> result, boolean backup, AbstractLoadLocalIndexTask loadTask) {
		if (voiceDir.canRead()) {
			//First list TTS files, they are preferred
			for (File voiceF : listFilesSorted(voiceDir)) {
				if (voiceF.isDirectory() && !MediaCommandPlayerImpl.isMyData(voiceF)) {
					LocalIndexInfo info = null;
					if (TTSCommandPlayerImpl.isMyData(voiceF)) {
						info = new LocalIndexInfo(LocalIndexType.TTS_VOICE_DATA, voiceF, backup);
					}
					if (info != null) {
						updateDescription(info);
						result.add(info);
						loadTask.loadFile(info);
					}
				}
			}

			//Now list recorded voices
			for (File voiceF : listFilesSorted(voiceDir)) {
				if (voiceF.isDirectory() && MediaCommandPlayerImpl.isMyData(voiceF)) {
					LocalIndexInfo info = null;
					info = new LocalIndexInfo(LocalIndexType.VOICE_DATA, voiceF, backup);
					if (info != null) {
						updateDescription(info);
						result.add(info);
						loadTask.loadFile(info);
					}
				}
			}
		}
	}

	private void loadFontData(File fontDir, List<LocalIndexInfo> result, boolean backup, AbstractLoadLocalIndexTask loadTask) {
		if (fontDir.canRead()) {
			for (File fontFile : listFilesSorted(fontDir)) {
				if (fontFile.isFile() && fontFile.getName().endsWith(IndexConstants.FONT_INDEX_EXT)) {
					LocalIndexType lt = LocalIndexType.FONT_DATA;
					LocalIndexInfo info = new LocalIndexInfo(lt, fontFile, backup);
					updateDescription(info);
					result.add(info);
					loadTask.loadFile(info);
				}
			}
		}
	}

	private File[] listFilesSorted(File dir) {
		File[] listFiles = dir.listFiles();
		if (listFiles == null) {
			return new File[0];
		}
		Arrays.sort(listFiles);
		return listFiles;
	}

	private void loadWikiData(File mapPath, List<LocalIndexInfo> result, AbstractLoadLocalIndexTask loadTask) {
		if (mapPath.canRead()) {
			for (File mapFile : listFilesSorted(mapPath)) {
				if (mapFile.isFile() && mapFile.getName().endsWith(IndexConstants.BINARY_MAP_INDEX_EXT)) {
					LocalIndexInfo info = new LocalIndexInfo(LocalIndexType.WIKI_DATA, mapFile, false);
					updateDescription(info);
					result.add(info);
					loadTask.loadFile(info);
				}
			}
		}
	}

	private void loadObfData(File mapPath, List<LocalIndexInfo> result, boolean backup, AbstractLoadLocalIndexTask loadTask, Map<String, String> loadedMaps) {
		if (mapPath.canRead()) {
			for (File mapFile : listFilesSorted(mapPath)) {
				if (mapFile.isFile() && mapFile.getName().endsWith(IndexConstants.BINARY_MAP_INDEX_EXT)) {
					LocalIndexType lt = LocalIndexType.MAP_DATA;
					if (mapFile.getName().endsWith(IndexConstants.BINARY_WIKI_MAP_INDEX_EXT)) {
						lt = LocalIndexType.WIKI_DATA;
					}
					LocalIndexInfo info = new LocalIndexInfo(lt, mapFile, backup);
					if (loadedMaps.containsKey(mapFile.getName()) && !backup) {
						info.setLoaded(true);
					}
					updateDescription(info);
					result.add(info);
					loadTask.loadFile(info);
				}
			}
		}
	}

	public enum LocalIndexType {
		MAP_DATA(R.string.local_indexes_cat_map, R.drawable.ic_map, 10),
		WIKI_DATA(R.string.local_indexes_cat_wiki, R.drawable.ic_plugin_wikipedia, 50),
		TTS_VOICE_DATA(R.string.local_indexes_cat_tts, R.drawable.ic_action_volume_up, 20),
		VOICE_DATA(R.string.local_indexes_cat_voice, R.drawable.ic_action_volume_up, 30),
		FONT_DATA(R.string.fonts_header, R.drawable.ic_action_map_language, 35),
		DEACTIVATED(R.string.local_indexes_cat_backup, R.drawable.ic_type_archive, 1000);

		@StringRes
		private final int resId;
		@DrawableRes
		private final int iconResource;
		private final int orderIndex;

		LocalIndexType(@StringRes int resId, @DrawableRes int iconResource, int orderIndex) {
			this.resId = resId;
			this.iconResource = iconResource;
			this.orderIndex = orderIndex;
		}

		public String getHumanString(Context ctx) {
			return ctx.getString(resId);
		}

		public int getIconResource() {
			return iconResource;
		}

		public int getOrderIndex(LocalIndexInfo info) {
			String fileName = info.getFileName();
			int index = info.getOriginalType().orderIndex;
			if (info.getType() == DEACTIVATED) {
				index += DEACTIVATED.orderIndex;
			}
			if (fileName.endsWith(IndexConstants.BINARY_ROAD_MAP_INDEX_EXT)) {
				index++;
			}
			return index;
		}

		public String getBasename(LocalIndexInfo localIndexInfo) {
			String fileName = localIndexInfo.getFileName();
			if (fileName.endsWith(IndexConstants.EXTRA_ZIP_EXT)) {
				return fileName.substring(0, fileName.length() - IndexConstants.EXTRA_ZIP_EXT.length());
			}
			if (fileName.endsWith(IndexConstants.SQLITE_EXT)) {
				return fileName.substring(0, fileName.length() - IndexConstants.SQLITE_EXT.length());
			}
			if (this == VOICE_DATA) {
				int l = fileName.lastIndexOf('_');
				if (l == -1) {
					l = fileName.length();
				}
				return fileName.substring(0, l);
			}
			if (this == FONT_DATA) {
				int l = fileName.indexOf('.');
				if (l == -1) {
					l = fileName.length();
				}
				return fileName.substring(0, l).replace('_', ' ').replace('-', ' ');
			}
			int ls = fileName.lastIndexOf('_');
			if (ls >= 0) {
				return fileName.substring(0, ls);
			} else if (fileName.indexOf('.') > 0) {
				return fileName.substring(0, fileName.indexOf('.'));
			}
			return fileName;
		}
	}
}
