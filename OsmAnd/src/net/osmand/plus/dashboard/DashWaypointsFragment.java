package net.osmand.plus.dashboard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.AlertDialog;
import android.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.TargetPointsHelper.TargetPoint;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dashboard.DashboardOnMap.DashboardType;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.dialogs.DirectionsDialogs;
import net.osmand.plus.helpers.WaypointDialogHelper;
import net.osmand.plus.helpers.WaypointHelper;
import net.osmand.plus.helpers.WaypointHelper.LocationPointWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashWaypointsFragment extends DashLocationFragment {
	public static final String TAG = "DASH_WAYPOINTS_FRAGMENT";
	private static final int TITLE_ID = R.string.waypoints;
	private static boolean SHOW_ALL;
	public static final DashFragmentData.ShouldShowFunction SHOULD_SHOW_FUNCTION =
			new DashboardOnMap.DefaultShouldShow() {
				@Override
				public int getTitleId() {
					return TITLE_ID;
				}
			};

	@Override
	public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.dash_common_fragment, container, false);
		((TextView) view.findViewById(R.id.fav_text)).setText(getString(TITLE_ID));
		
		return view;
	}

	@Override
	public void onOpenDash() {
		setupView();
	}
	
	private void setupView() {
		if(getMyApplication().getWaypointHelper().isRouteCalculated()) {
			setupWaypoints();
		} else {
			setupTargets();
		}
	}

	private void setupWaypoints() {
		View mainView = getView();
		WaypointHelper wh = getMyApplication().getWaypointHelper();
		List<LocationPointWrapper> allPoints = wh.getAllPoints();
		if (allPoints.size() == 0) {
			(mainView.findViewById(R.id.main_fav)).setVisibility(View.GONE);
			return;
		} else {
			(mainView.findViewById(R.id.main_fav)).setVisibility(View.VISIBLE);
		}		
		((TextView) mainView.findViewById(R.id.fav_text)).setText(getString(R.string.waypoints));
		((Button) mainView.findViewById(R.id.show_all)).setText(getString(R.string.shared_string_show_all));
		mainView.findViewById(R.id.show_all).setVisibility(View.VISIBLE);
		mainView.findViewById(R.id.show_all).setOnClickListener(v -> dashboard.setDashboardVisibility(true, DashboardType.WAYPOINTS, AndroidUtils.getCenterViewCoordinates(v)));
		LinearLayout favorites = mainView.findViewById(R.id.items);
		favorites.removeAllViews();
		List<DashLocationView> distances = new ArrayList<>();
		for(int i = 0; i < 3 && i < allPoints.size(); i++) {
			LocationPointWrapper ps = allPoints.get(i);
			View dv = getActivity().getLayoutInflater().inflate(R.layout.divider, null);
			favorites.addView(dv);
			View v = WaypointDialogHelper.updateWaypointItemView(false, null, getMyApplication(),
					getActivity(), null, null, ps, null, !getMyApplication().getSettings().isLightContent(), true);
			favorites.addView(v);

		}
		this.distances = distances;
	}

	private void setupTargets() {
		View mainView = getView();
		final TargetPointsHelper helper = getMyApplication().getTargetPointsHelper();
		if (helper.getPointToNavigate() == null) {
			(mainView.findViewById(R.id.main_fav)).setVisibility(View.GONE);
			return;
		} else {
			(mainView.findViewById(R.id.main_fav)).setVisibility(View.VISIBLE);
		}
		List<TargetPoint> points = SHOW_ALL ? helper.getIntermediatePointsWithTarget() :
				Collections.singletonList(helper.getPointToNavigate());
		((Button) mainView.findViewById(R.id.show_all)).setText(SHOW_ALL? getString(R.string.shared_string_collapse) : 
			getString(R.string.shared_string_show_all));
		mainView.findViewById(R.id.show_all).setOnClickListener(view -> {
			SHOW_ALL = !SHOW_ALL;
			setupView();
		});
		mainView.findViewById(R.id.show_all).setVisibility(
				helper.getIntermediatePoints().size() == 0 ? View.INVISIBLE : View.VISIBLE);
		((TextView) mainView.findViewById(R.id.fav_text)).setText(getString(R.string.waypoints) + " (" + 
				helper.getIntermediatePointsWithTarget().size()+")");
		LinearLayout favorites = mainView.findViewById(R.id.items);
		favorites.removeAllViews();
		List<DashLocationView> distances = new ArrayList<>();
		for (final TargetPoint point : points) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.favorites_list_item, null, false);
			TextView name = view.findViewById(R.id.favourite_label);
			TextView label = view.findViewById(R.id.distance);
			ImageView direction = view.findViewById(R.id.direction);
			direction.setVisibility(View.VISIBLE);
			label.setVisibility(View.VISIBLE);
			view.findViewById(R.id.divider).setVisibility(View.VISIBLE);
			view.findViewById(R.id.group_image).setVisibility(View.GONE);

			boolean target = helper.getPointToNavigate() == point;
			int id;
			if (!target) {
				id = R.drawable.list_intermediate;
			} else {
				id = R.drawable.list_destination;
			}

			((ImageView) view.findViewById(R.id.favourite_icon)).setImageDrawable(getMyApplication().getIconsCache()
					.getIcon(id, 0));
			DashLocationView dv = new DashLocationView(direction, label, new LatLon(point.getLatitude(),
					point.getLongitude()));
			distances.add(dv);

			name.setText(PointDescription.getSimpleName(point, getActivity()));
			ImageButton options = view.findViewById(R.id.options);
			options.setVisibility(View.VISIBLE);
			final boolean optionsVisible = (SHOW_ALL && getMyApplication().getTargetPointsHelper().getIntermediatePoints().size() > 0); 
			
			options.setImageDrawable(getMyApplication().getIconsCache().
					getThemedIcon(optionsVisible ? R.drawable.ic_overflow_menu_white :
							R.drawable.ic_action_remove_dark));
			options.setOnClickListener(view13 -> {
				if(optionsVisible) {
					selectTargetModel(point, view13);
				} else {
					deletePointConfirm(point, view13);
				}
			});
			
			ImageButton navigate = view.findViewById(R.id.navigate_to);
			navigate.setImageDrawable(getMyApplication().getIconsCache().
					getThemedIcon(R.drawable.ic_action_gdirections_dark));
			navigate.setVisibility(target? View.VISIBLE : View.GONE);
			navigate.setOnClickListener(view12 -> dashboard.navigationAction());
			
			view.setOnClickListener(view1 -> {
				getMyApplication().getSettings().setMapLocationToShow(point.getLatitude(), point.getLongitude(),
						15, point.getPointDescription(getActivity()), false,
						point); //$NON-NLS-1$
				MapActivity.launchMapActivityMoveToTop(getActivity());
			});
			favorites.addView(view);
		}
		this.distances = distances;
	}
	
	private void deletePointConfirm(final TargetPoint point, View view) {
		final boolean target = point == getMyApplication().getTargetPointsHelper().getPointToNavigate();
		AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
		// Stop the navigation
		builder.setTitle(getString(R.string.delete_target_point));
		builder.setMessage(PointDescription.getSimpleName(point, getActivity()));
		builder.setPositiveButton(R.string.shared_string_yes, (dialog, which) -> {
			getMyApplication().getTargetPointsHelper().removeWayPoint(true, target ? -1 :  point.index);
			setupView();
		});
		builder.setNegativeButton(R.string.shared_string_no, null);
		builder.show();		
	}

	private void selectTargetModel(final TargetPoint point, final View view) {
		final PopupMenu optionsMenu = new PopupMenu(getActivity(), view);
		DirectionsDialogs.setupPopUpMenuIcon(optionsMenu);
		MenuItem item; 
		final boolean target = point == getMyApplication().getTargetPointsHelper().getPointToNavigate();
		if(SHOW_ALL && getMyApplication().getTargetPointsHelper().getIntermediatePoints().size() > 0) {
			final List<TargetPoint> allTargets = getMyApplication().getTargetPointsHelper().getIntermediatePointsWithTarget();
			if (point.index > 0 || target) {
				final int ind = target ? allTargets.size() - 1 : point.index;
				item = optionsMenu.getMenu().add(R.string.waypoint_visit_before)
						.setIcon(getMyApplication().getIconsCache().
								getThemedIcon(R.drawable.ic_action_up_dark));
				item.setOnMenuItemClickListener(item13 -> {
					TargetPoint remove = allTargets.remove(ind - 1);
					allTargets.add(ind, remove);
					getMyApplication().getTargetPointsHelper().reorderAllTargetPoints(allTargets, true);
					setupView();
					return true;
				});
			}
			if (!target) {
				item = optionsMenu.getMenu().add(R.string.waypoint_visit_after)
						.setIcon(getMyApplication().getIconsCache().
								getThemedIcon(R.drawable.ic_action_down_dark));
				item.setOnMenuItemClickListener(item12 -> {
					TargetPoint remove = allTargets.remove(point.index + 1);
					allTargets.add(point.index, remove);
					getMyApplication().getTargetPointsHelper().reorderAllTargetPoints(allTargets, true);
					setupView();
					return true;
				});
			}
		}
		item = optionsMenu.getMenu().add(
				R.string.shared_string_remove).setIcon(getMyApplication().getIconsCache().
				getThemedIcon(R.drawable.ic_action_remove_dark));
		item.setOnMenuItemClickListener(item1 -> {
			deletePointConfirm(point, view);
			return true;
		});
		optionsMenu.show();
	}
}
