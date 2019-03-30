package net.osmand.plus.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.Location;
import net.osmand.data.LatLon;
import net.osmand.plus.MapMarkersHelper.MapMarker;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.DirectionDrawable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapMarkerDialogHelper {
	interface MapMarkersDialogHelperCallbacks {
		void showMarkersRouteOnMap();
	}

	public static void updateMapMarkerInfo(final Context ctx, View localView, LatLon loc,
										   Float heading, boolean useCenter, boolean nightMode,
										   int screenOrientation, boolean selectionMode,
										   final MapMarkersDialogHelperCallbacks helperCallbacks,
										   final MapMarker marker, boolean showDateAndGroup) {
		TextView text = localView.findViewById(R.id.waypoint_text);
		TextView textShadow = localView.findViewById(R.id.waypoint_text_shadow);
		TextView textDist = localView.findViewById(R.id.waypoint_dist);
		ImageView arrow = localView.findViewById(R.id.direction);
		ImageView waypointIcon = localView.findViewById(R.id.waypoint_icon);
		TextView waypointDeviation = localView.findViewById(R.id.waypoint_deviation);
		TextView descText = localView.findViewById(R.id.waypoint_desc_text);
		final CheckBox checkBox = localView.findViewById(R.id.checkbox);
		TextView dateGroupText = localView.findViewById(R.id.date_group_text);

		if (text == null || textDist == null || arrow == null || waypointIcon == null
				|| waypointDeviation == null || descText == null) {
			return;
		}

		float[] mes = new float[2];
		if (loc != null && marker.point != null) {
			Location.distanceBetween(marker.getLatitude(), marker.getLongitude(), loc.getLatitude(), loc.getLongitude(), mes);
		}
		boolean newImage = false;
		int arrowResId = R.drawable.ic_direction_arrow;
		DirectionDrawable dd;
		if (!(arrow.getDrawable() instanceof DirectionDrawable)) {
			newImage = true;
			dd = new DirectionDrawable(ctx, arrow.getWidth(), arrow.getHeight());
		} else {
			dd = (DirectionDrawable) arrow.getDrawable();
		}
		if (!marker.history) {
			dd.setImage(arrowResId, useCenter ? R.color.color_distance : R.color.color_myloc_distance);
		} else {
			dd.setImage(arrowResId, nightMode ? R.color.secondary_text_dark : R.color.secondary_text_light);
		}
		if (loc == null || heading == null || marker.point == null) {
			dd.setAngle(0);
		} else {
			dd.setAngle(mes[1] - heading + 180 + screenOrientation);
		}
		if (newImage) {
			arrow.setImageDrawable(dd);
		}
		arrow.setVisibility(View.VISIBLE);
		arrow.invalidate();

		final OsmandApplication app = (OsmandApplication) ctx.getApplicationContext();

		if (!marker.history) {
			waypointIcon.setImageDrawable(getMapMarkerIcon(app, marker.colorIndex));
			AndroidUtils.setTextPrimaryColor(ctx, text, nightMode);
			textDist.setTextColor(ctx.getResources()
					.getColor(useCenter ? R.color.color_distance : R.color.color_myloc_distance));
		} else {
			waypointIcon.setImageDrawable(app.getIconsCache()
					.getIcon(R.drawable.ic_action_flag_dark, !nightMode));
			AndroidUtils.setTextSecondaryColor(ctx, text, nightMode);
			AndroidUtils.setTextSecondaryColor(ctx, textDist, nightMode);
		}

		int dist = (int) mes[0];
		textDist.setText(OsmAndFormatter.getFormattedDistance(dist, app));

		waypointDeviation.setVisibility(View.GONE);

		String descr = marker.getName(app);
		if (textShadow != null) {
			textShadow.setText(descr);
		}
		text.setText(descr);

		descText.setVisibility(View.GONE);

		if (showDateAndGroup) {
			Date date = new Date(marker.creationDate);
			String month = new SimpleDateFormat("MMM", Locale.getDefault()).format(date);
			if (month.length() > 1) {
				month = Character.toUpperCase(month.charAt(0)) + month.substring(1);
			}
			month = month.replaceAll("\\.", "");
			String day = new SimpleDateFormat("d", Locale.getDefault()).format(date);
			String desc = month + " " + day;
			String markerGroupName = marker.groupName;
			if (markerGroupName != null) {
				if (markerGroupName.equals("")) {
					markerGroupName = app.getString(R.string.shared_string_favorites);
				}
				desc += " • " + markerGroupName;
			}
			dateGroupText.setVisibility(View.VISIBLE);
			dateGroupText.setText(desc);
		}

		if (selectionMode) {
			checkBox.setChecked(marker.selected);
			checkBox.setVisibility(View.VISIBLE);
			checkBox.setOnClickListener(v -> {
				marker.selected = checkBox.isChecked();
				app.getMapMarkersHelper().updateMapMarker(marker, false);
				if (helperCallbacks != null) {
					helperCallbacks.showMarkersRouteOnMap();
				} else if (ctx instanceof MapActivity) {
					((MapActivity) ctx).refreshMap();
				}
			});
		} else {
			checkBox.setVisibility(View.GONE);
			checkBox.setOnClickListener(null);
		}
	}

	public static Drawable getMapMarkerIcon(OsmandApplication app, int colorIndex) {
		return app.getIconsCache().getIcon(R.drawable.ic_action_flag_dark, MapMarker.getColorId(colorIndex));
	}
}
