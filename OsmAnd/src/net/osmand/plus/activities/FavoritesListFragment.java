package net.osmand.plus.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.osmand.data.FavouritePoint;
import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.OsmAndLocationProvider.OsmAndCompassListener;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.search.SearchActivity;
import net.osmand.plus.activities.search.SearchActivity.SearchActivityChild;
import net.osmand.plus.base.FavoriteImageDrawable;
import net.osmand.plus.base.OsmAndListFragment;
import net.osmand.plus.dashboard.DashLocationFragment;
import net.osmand.util.MapUtils;

import java.util.Comparator;
import java.util.List;

public class FavoritesListFragment extends OsmAndListFragment implements SearchActivityChild, OsmAndCompassListener {

	public static final String SELECT_FAVORITE_POINT_INTENT_KEY = "SELECT_FAVORITE_POINT_INTENT_KEY";
	public static final int SELECT_FAVORITE_POINT_RESULT_OK = 1;

	private FavouritesAdapter favouritesAdapter;

	private boolean selectFavoriteMode;
	private OsmandSettings settings;
	private boolean compassRegistered;



	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Intent intent = activity.getIntent();
		settings = getApplication().getSettings();
		OsmandApplication app = getApplication();
		favouritesAdapter = new FavouritesAdapter(activity, app.getFavorites().getFavouritePoints(),
				false);
		setListAdapter(favouritesAdapter);
		setHasOptionsMenu(true);

		if (intent != null) {
			selectFavoriteMode = intent.hasExtra(SELECT_FAVORITE_POINT_INTENT_KEY);
			if (intent.hasExtra(SearchActivity.SEARCH_LAT) && intent.hasExtra(SearchActivity.SEARCH_LON)) {
				double lat = intent.getDoubleExtra(SearchActivity.SEARCH_LAT, 0);
				double lon = intent.getDoubleExtra(SearchActivity.SEARCH_LON, 0);
				if (lat != 0 || lon != 0) {
					favouritesAdapter.location = new LatLon(lat, lon);
				}
			}
		}
	}

	@Override
	public ArrayAdapter<?> getAdapter() {
		return favouritesAdapter;
	}
	
	private OsmandApplication getApplication() {
		return (OsmandApplication) getActivity().getApplication();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (favouritesAdapter.location == null && getActivity() instanceof SearchActivity) {
			favouritesAdapter.location = ((SearchActivity) getActivity()).getSearchPoint();
		}
		if (favouritesAdapter.location == null) {
			favouritesAdapter.location = settings.getLastKnownMapLocation();
		}
		favouritesAdapter.screenOrientation = DashLocationFragment.getScreenOrientation(getActivity());
		locationUpdate(favouritesAdapter.location);
	}

	@Override
	public void locationUpdate(LatLon l) {
		if (getActivity() instanceof SearchActivity) {
			if (((SearchActivity) getActivity()).isSearchAroundCurrentLocation() && l != null) {
				if (!compassRegistered) {
					OsmandApplication app = getMyApplication();
					app.getLocationProvider().removeCompassListener(app.getLocationProvider().getNavigationInfo());
					app.getLocationProvider().addCompassListener(this);
					compassRegistered = true;
				}
				favouritesAdapter.searchAroundLocation = true;
			} else {
				favouritesAdapter.searchAroundLocation = false;
			}
		}
		if (favouritesAdapter != null) {
			favouritesAdapter.updateLocation(l);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		if(getActivity() instanceof SearchActivity) {
			OsmandApplication app = getMyApplication();
			app.getLocationProvider().removeCompassListener(this);
			app.getLocationProvider().addCompassListener(app.getLocationProvider().getNavigationInfo());
			compassRegistered = false;
		}
	}

	private boolean isSelectFavoriteMode() {
		return selectFavoriteMode;
	}


	@Override
	public void onCreateOptionsMenu(Menu onCreate, MenuInflater inflater) {
		if (getActivity() instanceof SearchActivity) {
			((SearchActivity) getActivity()).getClearToolbar(false);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		if (!isSelectFavoriteMode()) {
			FavouritePoint point = favouritesAdapter.getItem(position);
			showOnMap(point, getActivity());
		} else {
			Intent intent = getActivity().getIntent();
			intent.putExtra(SELECT_FAVORITE_POINT_INTENT_KEY, favouritesAdapter.getItem(position));
			getActivity().setResult(SELECT_FAVORITE_POINT_RESULT_OK, intent);
			getActivity().finish();
		}
	}

	public static class FavouritesAdapter extends ArrayAdapter<FavouritePoint> {
		private final Activity activity;
		private LatLon location;
		private final OsmandApplication app;
		private boolean searchAroundLocation;
		private int screenOrientation;
		private Float heading;
		private final boolean shoudShowMenuButton;

		public FavouritesAdapter(Activity activity, List<FavouritePoint> list,
								 boolean shoudShowMenuButton) {
			super(activity, R.layout.favorites_list_item, list);
			this.activity = activity;
			this.app = ((OsmandApplication) activity.getApplication());
			this.shoudShowMenuButton = shoudShowMenuButton;
		}

		public LatLon getLocation() {
			return location;
		}

		public void updateLocation(LatLon l) {
			location = l;
			sort(new Comparator<FavouritePoint>() {
				@Override
				public int compare(FavouritePoint object1, FavouritePoint object2) {
					if (location != null) {
						double d1 = MapUtils.getDistance(location, object1.getLatitude(), object1.getLongitude());
						double d2 = MapUtils.getDistance(location, object2.getLatitude(), object2.getLongitude());
						if (d1 == d2) {
							return 0;
						} else if (d1 > d2) {
							return 1;
						}
						return -1;
					} else {
						return getName(object1).compareTo(getName(object2));
					}
				}
			});
		}

		String getName(FavouritePoint model) {
			return model.getName();
		}

		@NonNull
        @Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = activity.getLayoutInflater(); // favourite dank
				row = inflater.inflate(R.layout.favorites_list_item, parent, false);
			}

			TextView name = row.findViewById(R.id.favourite_label);
			TextView distanceText = row.findViewById(R.id.distance);
			ImageView icon = row.findViewById(R.id.favourite_icon);
			ImageView direction = row.findViewById(R.id.direction);
			ImageView giImage= row.findViewById(R.id.group_image);
			direction.setVisibility(View.VISIBLE);
			final FavouritePoint favorite = getItem(position);
			if (shoudShowMenuButton) {
				ImageButton options = row.findViewById(R.id.options);
				options.setFocusable(false);
				options.setImageDrawable(((OsmandApplication) activity.getApplication())
						.getIconsCache().getThemedIcon(R.drawable.ic_overflow_menu_white));
				options.setVisibility(View.VISIBLE);
				options.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showOnMap(favorite, activity);
					}
				});
			}
			if (!favorite.getCategory().isEmpty()) {
				giImage.setVisibility(View.VISIBLE);
				giImage.setImageDrawable(app.getIconsCache().getThemedIcon(R.drawable.ic_small_group));
			} else {
				giImage.setVisibility(View.GONE);
			}
			((TextView) row.findViewById(R.id.group_name)).setText(favorite.getCategory());

			icon.setImageDrawable(FavoriteImageDrawable.getOrCreate(activity, favorite.getColor(), false));
			DashLocationFragment.updateLocationView(!searchAroundLocation, location, heading, direction, distanceText,
					favorite.getLatitude(), favorite.getLongitude(), screenOrientation, app, activity);

			name.setText(getName(favorite));
			final CheckBox ch = row.findViewById(R.id.toggle_item);
			icon.setVisibility(View.VISIBLE);
			ch.setVisibility(View.GONE);
			if (activity instanceof SearchActivity)
				row.setAccessibilityDelegate(((SearchActivity)activity).getAccessibilityAssistant());
			return row;
		}

	}

	protected OsmandApplication getMyApplication() {
		return (OsmandApplication) getActivity().getApplication();
	}

	@Override
	public void updateCompassValue(float value) {
		// 99 in next line used to one-time initialize arrows (with reference vs. fixed-north direction) on non-compass
		// devices
		FragmentActivity activity = getActivity();
		float lastHeading = favouritesAdapter.heading != null ? favouritesAdapter.heading : 99;
		favouritesAdapter.heading = value;
		if (Math.abs(MapUtils.degreesDiff(lastHeading, favouritesAdapter.heading)) > 5) {
			if (activity instanceof SearchActivity) {
				((SearchActivity)activity).getAccessibilityAssistant().lockEvents();
				favouritesAdapter.notifyDataSetChanged();
				((SearchActivity)activity).getAccessibilityAssistant().unlockEvents();
			} else {
				favouritesAdapter.notifyDataSetChanged();
			}
		} else {
			favouritesAdapter.heading = lastHeading;
		}
		if (activity instanceof SearchActivity) {
			final View selected = ((SearchActivity)activity).getAccessibilityAssistant().getFocusedView();
			if (selected != null) {
				try {
					int position = getListView().getPositionForView(selected);
					if ((position != AdapterView.INVALID_POSITION) && (position >= getListView().getHeaderViewsCount()))  {
						FavouritePoint point = favouritesAdapter.getItem(position - getListView().getHeaderViewsCount());
						LatLon location = new LatLon(point.getLatitude(), point.getLongitude());
						((SearchActivity)activity).getNavigationInfo().updateTargetDirection(location, favouritesAdapter.heading);
					}
				} catch (Exception e) {
					return;
				}
			}
		}
	}

	private static void showOnMap(FavouritePoint point, Activity activity) {
		OsmandApplication app = (OsmandApplication) activity.getApplication();
		final OsmandSettings settings = app.getSettings();
		LatLon location = new LatLon(point.getLatitude(), point.getLongitude());

		settings.setMapLocationToShow(location.getLatitude(), location.getLongitude(),
				settings.getLastKnownMapZoom(),
				new PointDescription(PointDescription.POINT_TYPE_FAVORITE, point.getName()),
				true,
				point); //$NON-NLS-1$
		MapActivity.launchMapActivityMoveToTop(activity);

	}
}
