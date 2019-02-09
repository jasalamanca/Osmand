package net.osmand.plus.activities;

import android.support.annotation.NonNull;

import net.osmand.plus.activities.LocalIndexHelper.LocalIndexType;

import java.io.File;

public class LocalIndexInfo {
	private final LocalIndexType type;
	private String description = "";
	private String name;

	private boolean backupedData;
	private String subfolder;
	private String pathToData;
	private String fileName;
	private int kbSize = -1;

	public LocalIndexInfo(@NonNull LocalIndexType type, @NonNull File f, boolean backuped) {
        pathToData = f.getAbsolutePath();
        fileName = f.getName();
        name = formatName(f.getName());
        this.type = type;
		boolean singleFile = !f.isDirectory();
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

	public void setBackupedData(boolean backupedData) {
		this.backupedData = backupedData;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSubfolder() {
		return subfolder;
	}
	public int getSize() {
		return kbSize;
	}
	public boolean isNotSupported() {
		return false;
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
		return false;
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