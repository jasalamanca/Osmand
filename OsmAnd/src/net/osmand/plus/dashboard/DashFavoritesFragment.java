package net.osmand.plus.dashboard;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.osmand.data.FavouritePoint;
import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.FavouritesDbHelper;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.FavoriteImageDrawable;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.dialogs.DirectionsDialogs;
import net.osmand.plus.myplaces.FavoritesActivity;
import net.osmand.util.MapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Denis
 * on 24.11.2014.
 */
public class DashFavoritesFragment extends DashLocationFragment {
	private static final String TAG = "DASH_FAVORITES_FRAGMENT";
	private static final int TITLE_ID = R.string.shared_string_my_favorites;
	private List<FavouritePoint> points = new ArrayList<>();

	private static final String ROW_NUMBER_TAG = TAG + "_row_number";
	private static final DashFragmentData.ShouldShowFunction SHOULD_SHOW_FUNCTION =
			new DashboardOnMap.DefaultShouldShow() {
				@Override
				public int getTitleId() {
					return TITLE_ID;
				}
			};
	public static final DashFragmentData FRAGMENT_DATA =
			new DashFragmentData(TAG, DashFavoritesFragment.class, SHOULD_SHOW_FUNCTION, 90, ROW_NUMBER_TAG);

	@Override
	public View initView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.dash_common_fragment, container, false);
		(view.findViewById(R.id.show_all)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startFavoritesActivity(FavoritesActivity.FAV_TAB);
				closeDashboard();
			}
		});
		return view;
	}

	@Override
	public void onOpenDash() {
		setupFavorites();
	}

	private void setupFavorites() {
		View mainView = getView();
		final FavouritesDbHelper helper = getMyApplication().getFavorites();
		points = new ArrayList<>(helper.getFavouritePoints());
		if (points.size() == 0) {
			(mainView.findViewById(R.id.main_fav)).setVisibility(View.GONE);
			return;
		} else {
			(mainView.findViewById(R.id.main_fav)).setVisibility(View.VISIBLE);
		}
		final LatLon loc = getDefaultLocation();
		if (loc != null) {
			Collections.sort(points, new Comparator<FavouritePoint>() {
				@Override
				public int compare(FavouritePoint point, FavouritePoint point2) {
					// LatLon lastKnownMapLocation = getMyApplication().getSettings().getLastKnownMapLocation();
					int dist = (int) (MapUtils.getDistance(point.getLatitude(), point.getLongitude(),
							loc.getLatitude(), loc.getLongitude()));
					int dist2 = (int) (MapUtils.getDistance(point2.getLatitude(), point2.getLongitude(),
							loc.getLatitude(), loc.getLongitude()));
					return (dist - dist2);
				}
			});
		}
		LinearLayout favorites = mainView.findViewById(R.id.items);
		favorites.removeAllViews();
		DashboardOnMap.handleNumberOfRows(points, getMyApplication().getSettings(), ROW_NUMBER_TAG);
		List<DashLocationView> distances = new ArrayList<>();
		for (final FavouritePoint point : points) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.favorites_list_item, null, false);
			TextView name = view.findViewById(R.id.favourite_label);
			TextView label = view.findViewById(R.id.distance);
			ImageView direction = view.findViewById(R.id.direction);
			direction.setVisibility(View.VISIBLE);
			label.setVisibility(View.VISIBLE);
			view.findViewById(R.id.divider).setVisibility(View.VISIBLE);
			ImageView groupImage = view.findViewById(R.id.group_image);
			if (point.getCategory().length() > 0) {
				((TextView) view.findViewById(R.id.group_name)).setText(point.getCategory());
				groupImage.setImageDrawable(getMyApplication().getIconsCache().getThemedIcon(R.drawable.ic_small_group));
			} else {
				groupImage.setVisibility(View.GONE);
			}

			((ImageView) view.findViewById(R.id.favourite_icon)).setImageDrawable(FavoriteImageDrawable.getOrCreate(
					getActivity(), point.getColor(), false));
			DashLocationView dv = new DashLocationView(direction, label, new LatLon(point.getLatitude(),
					point.getLongitude()));
			distances.add(dv);

			name.setText(point.getName());
			name.setTypeface(Typeface.DEFAULT, point.isVisible() ? Typeface.NORMAL : Typeface.ITALIC);
			view.findViewById(R.id.navigate_to).setVisibility(View.VISIBLE);

			((ImageView) view.findViewById(R.id.navigate_to)).setImageDrawable(getMyApplication().getIconsCache().getThemedIcon(R.drawable.ic_action_gdirections_dark));
			view.findViewById(R.id.navigate_to).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					DirectionsDialogs.directionsToDialogAndLaunchMap(getActivity(), point.getLatitude(),
							point.getLongitude(),
							new PointDescription(PointDescription.POINT_TYPE_FAVORITE, point.getName()));
				}
			});
			
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					getMyApplication().getSettings().setMapLocationToShow(point.getLatitude(), point.getLongitude(),
							15, new PointDescription(PointDescription.POINT_TYPE_FAVORITE, point.getName()), true,
							point); //$NON-NLS-1$
					MapActivity.launchMapActivityMoveToTop(getActivity());
				}
			});
			favorites.addView(view);
		}
		this.distances = distances;
	}
}
