package net.osmand.plus.measurementtool;

import net.osmand.data.QuadRect;
import net.osmand.plus.GPXUtilities;

public class NewGpxData {

	public enum ActionType {
		ADD_SEGMENT,
		ADD_ROUTE_POINTS,
		EDIT_SEGMENT
	}

	private final GPXUtilities.GPXFile gpxFile;
	private final GPXUtilities.TrkSegment trkSegment;
	private final QuadRect rect;
	private final ActionType actionType;

	public NewGpxData(GPXUtilities.GPXFile gpxFile, QuadRect rect, ActionType actionType, GPXUtilities.TrkSegment trkSegment) {
		this.gpxFile = gpxFile;
		this.rect = rect;
		this.actionType = actionType;
		this.trkSegment = trkSegment;
	}

	public GPXUtilities.GPXFile getGpxFile() {
		return gpxFile;
	}

	public QuadRect getRect() {
		return rect;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public GPXUtilities.TrkSegment getTrkSegment() {
		return trkSegment;
	}
}
