package net.osmand.plus.views;

import android.content.Intent;
import android.graphics.PointF;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.data.QuadRect;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.GPXUtilities.GPXFile;
import net.osmand.plus.IconsCache;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.TrackActivity;
import net.osmand.plus.mapcontextmenu.MapContextMenu;
import net.osmand.plus.mapcontextmenu.editors.RtePtEditor;
import net.osmand.plus.mapcontextmenu.editors.WptPtEditor;
import net.osmand.plus.mapcontextmenu.editors.WptPtEditor.OnDismissListener;

public class AddGpxPointBottomSheetHelper implements OnDismissListener {
	private final View view;
	private final TextView title;
	private final TextView description;
	private final ImageView icon;
	private final MapActivity mapActivity;
	private final MapContextMenu contextMenu;
	private final ContextMenuLayer contextMenuLayer;
	private final IconsCache iconsCache;
	private String titleText;
	private boolean applyingPositionMode;
	private NewGpxPoint newGpxPoint;
	private PointDescription pointDescription;

	AddGpxPointBottomSheetHelper(final MapActivity activity, ContextMenuLayer ctxMenuLayer) {
		this.contextMenuLayer = ctxMenuLayer;
		iconsCache = activity.getMyApplication().getIconsCache();
		mapActivity = activity;
		contextMenu = activity.getContextMenu();
		view = activity.findViewById(R.id.add_gpx_point_bottom_sheet);
		title = view.findViewById(R.id.add_gpx_point_bottom_sheet_title);
		description = view.findViewById(R.id.description);
		icon = view.findViewById(R.id.icon);

		view.findViewById(R.id.create_button).setOnClickListener(v -> {
            contextMenuLayer.createGpxPoint();
            GPXFile gpx = newGpxPoint.getGpx();
            LatLon latLon = contextMenu.getLatLon();
            if (pointDescription.isWpt()) {
                WptPtEditor editor = activity.getContextMenu().getWptPtPointEditor();
                editor.setOnDismissListener(AddGpxPointBottomSheetHelper.this);
                editor.setNewGpxPointProcessing(true);
                editor.add(gpx, latLon, titleText);
            } else if (pointDescription.isRte()) {
                RtePtEditor editor = activity.getContextMenu().getRtePtPointEditor();
                editor.setOnDismissListener(AddGpxPointBottomSheetHelper.this);
                editor.setNewGpxPointProcessing(true);
                editor.add(gpx, latLon, titleText);
            }
        });
		view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
            hide();
            contextMenuLayer.cancelAddGpxPoint();
            openTrackActivity();
        });
	}

	public void onDraw(RotatedTileBox rt) {
		PointF point = contextMenuLayer.getMovableCenterPoint(rt);
		double lat = rt.getLatFromPixel(point.x, point.y);
		double lon = rt.getLonFromPixel(point.x, point.y);
		description.setText(PointDescription.getLocationName(mapActivity, lat, lon, true));
	}

	public void setTitle(String title) {
		if (title.equals("")) {
			if (pointDescription.isWpt()) {
				title = mapActivity.getString(R.string.waypoint_one);
			} else if (pointDescription.isRte()) {
				title = mapActivity.getString(R.string.route_point_one);
			}
		}
		titleText = title;
		this.title.setText(titleText);
	}

	public void show(NewGpxPoint newPoint) {
		this.newGpxPoint = newPoint;
		pointDescription = newPoint.getPointDescription();
		if (pointDescription.isWpt()) {
			setTitle(mapActivity.getString(R.string.waypoint_one));
			icon.setImageDrawable(iconsCache.getThemedIcon(R.drawable.ic_action_marker_dark));
		} else if (pointDescription.isRte()) {
			setTitle(mapActivity.getString(R.string.route_point_one));
			icon.setImageDrawable(iconsCache.getThemedIcon(R.drawable.ic_action_markers_dark));
		}
		exitApplyPositionMode();
		view.setVisibility(View.VISIBLE);
	}

	public void hide() {
		exitApplyPositionMode();
		view.setVisibility(View.GONE);
	}

	void enterApplyPositionMode() {
		if (!applyingPositionMode) {
			applyingPositionMode = true;
			view.findViewById(R.id.create_button).setEnabled(false);
		}
	}

	void exitApplyPositionMode() {
		if (applyingPositionMode) {
			applyingPositionMode = false;
			view.findViewById(R.id.create_button).setEnabled(true);
		}
	}

	@Override
	public void onDismiss() {
		MapContextMenu contextMenu = mapActivity.getContextMenu();
		if (contextMenu.isVisible() && contextMenu.isClosable()) {
			contextMenu.close();
		}
		openTrackActivity();
	}

	private void openTrackActivity() {
		Intent newIntent = new Intent(mapActivity, mapActivity.getMyApplication().getAppCustomization().getTrackActivity());
		newIntent.putExtra(TrackActivity.TRACK_FILE_NAME, newGpxPoint.getGpx().path);
		newIntent.putExtra(TrackActivity.OPEN_POINTS_TAB, true);
		newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mapActivity.startActivity(newIntent);
	}

	public static class NewGpxPoint {
		private final PointDescription pointDescription;
		private final GPXFile gpx;
		private final QuadRect rect;

		public NewGpxPoint(GPXFile gpx, PointDescription pointDescription, QuadRect rect) {
			this.gpx = gpx;
			this.pointDescription = pointDescription;
			this.rect = rect;
		}

		GPXFile getGpx() {
			return gpx;
		}

		PointDescription getPointDescription() {
			return pointDescription;
		}

		public QuadRect getRect() {
			return rect;
		}
	}
}
