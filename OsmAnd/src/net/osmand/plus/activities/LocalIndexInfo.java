package net.osmand.plus.activities;

import android.support.annotation.NonNull;

import net.osmand.plus.GPXUtilities.GPXFile;
import net.osmand.plus.activities.LocalIndexHelper.LocalIndexType;

import java.io.File;

public class LocalIndexInfo {

	private final LocalIndexType type;
	private String description = "";
	private String name;

	private boolean backupedData;
	private boolean corrupted = false;
	private boolean notSupported = false;
	private boolean loaded;
	private String subfolder;
	private String pathToData;
	private String fileName;
	private boolean singleFile;
	private int kbSize = -1;
	private Object attachedObject;

	// UI state expanded
	private boolean expanded;
	private GPXFile gpxFile;

    public LocalIndexInfo(@NonNull LocalIndexType type, @NonNull File f, boolean backuped) {
        pathToData = f.getAbsolutePath();
        fileName = f.getName();
        name = formatName(f.getName());
        this.type = type;
        singleFile = !f.isDirectory();
        if (singleFile) {
            kbSize = (int) ((f.length() + 512) >> 10);
        }
        this.backupedData = backuped;
    }

    private String formatName(String name) {
		int ext = name.indexOf('.');
		if (ext != -1) {
			name = name.substring(0, ext);
		}
		return name.replace('_', ' ');
	}

	// Special domain object represents category
    public LocalIndexInfo(@NonNull LocalIndexType type, boolean backup, @NonNull String subfolder) {
        this.type = type;
        backupedData = backup;
        this.subfolder = subfolder;
    }

	public void setCorrupted(boolean corrupted) {
		this.corrupted = corrupted;
		if (corrupted) {
			this.loaded = false;
		}
	}

	public void setBackupedData(boolean backupedData) {
		this.backupedData = backupedData;
	}
	public void setSize(int size) {
		this.kbSize = size;
	}
	public void setGpxFile(GPXFile gpxFile) {
		this.gpxFile = gpxFile;
	}
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	public String getSubfolder() {
		return subfolder;
	}
	public int getSize() {
		return kbSize;
	}
	public boolean isNotSupported() {
		return notSupported;
	}
	public String getName() {
		return name;
	}
	public LocalIndexType getType() {
		return backupedData ? LocalIndexType.DEACTIVATED : type;
	}
	public LocalIndexType getOriginalType() {
		return type;
	}
	public boolean isCorrupted() {
		return corrupted;
	}
	public boolean isBackupedData() {
		return backupedData;
	}
	public String getPathToData() {
		return pathToData;
	}
	public String getDescription() {
		return description;
	}
	public String getFileName() {
		return fileName;
	}
}