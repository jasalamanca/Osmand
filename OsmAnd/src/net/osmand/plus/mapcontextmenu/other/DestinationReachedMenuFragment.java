package net.osmand.plus.mapcontextmenu.other;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.data.LatLon;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.TargetPointsHelper.TargetPoint;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.search.SearchActivity;
import net.osmand.plus.activities.search.SearchPOIActivity;
import net.osmand.plus.poi.PoiFiltersHelper;
import net.osmand.plus.poi.PoiUIFilter;

public class DestinationReachedMenuFragment extends Fragment {
	private static final String TAG = "DestinationReachedMenuFragment";
	private static boolean exists = false;
	private DestinationReachedMenu menu;


	public DestinationReachedMenuFragment() {
		exists = true;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (menu == null) {
			menu = new DestinationReachedMenu(getMapActivity());
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dest_reached_menu_fragment, container, false);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissMenu();
			}
		});

		IconsCache iconsCache = getMapActivity().getMyApplication().getIconsCache();

		ImageButton closeImageButton = view.findViewById(R.id.closeImageButton);
		closeImageButton.setImageDrawable(iconsCache.getIcon(R.drawable.ic_action_remove_dark, menu.isLight()));
		closeImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissMenu();
			}
		});

		Button removeDestButton = view.findViewById(R.id.removeDestButton);
		removeDestButton.setCompoundDrawablesWithIntrinsicBounds(
				iconsCache.getIcon(R.drawable.ic_action_done, menu.isLight()), null, null, null);
		AndroidUtils.setTextPrimaryColor(view.getContext(), removeDestButton, !menu.isLight());
		removeDestButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getMapActivity().getMyApplication().getTargetPointsHelper().removeWayPoint(true, -1);
				Object contextMenuObj = getMapActivity().getContextMenu().getObject();
				if (getMapActivity().getContextMenu().isActive()
                        && contextMenuObj instanceof TargetPoint) {
					TargetPoint targetPoint = (TargetPoint) contextMenuObj;
					if (!targetPoint.start && !targetPoint.intermediate) {
						getMapActivity().getContextMenu().close();
					}
				}
				OsmandSettings settings = getMapActivity().getMyApplication().getSettings();
				settings.APPLICATION_MODE.set(settings.DEFAULT_APPLICATION_MODE.get());
				getMapActivity().getMapActions().stopNavigationWithoutConfirm();
				dismissMenu();
			}
		});

		Button recalcDestButton = view.findViewById(R.id.recalcDestButton);
		recalcDestButton.setCompoundDrawablesWithIntrinsicBounds(
				iconsCache.getIcon(R.drawable.ic_action_gdirections_dark, menu.isLight()), null, null, null);
		AndroidUtils.setTextPrimaryColor(view.getContext(), recalcDestButton, !menu.isLight());
		recalcDestButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TargetPointsHelper helper = getMapActivity().getMyApplication().getTargetPointsHelper();
				TargetPoint target = helper.getPointToNavigate();

				dismissMenu();

				if (target != null) {
					helper.navigateToPoint(new LatLon(target.getLatitude(), target.getLongitude()),
							true, -1, target.getOriginalPointDescription());
					getMapActivity().getMapActions().recalculateRoute(false);
					getMapActivity().getMapLayers().getMapControlsLayer().startNavigation();
				}
			}
		});

		View mainView = view.findViewById(R.id.main_view);
		if (menu.isLandscapeLayout()) {
			AndroidUtils.setBackground(view.getContext(), mainView, !menu.isLight(),
					R.drawable.bg_left_menu_light, R.drawable.bg_left_menu_dark);
		} else {
			AndroidUtils.setBackground(view.getContext(), mainView, !menu.isLight(),
					R.drawable.bg_bottom_menu_light, R.drawable.bg_bottom_menu_dark);
		}
		TextView title = view.findViewById(R.id.titleTextView);
		AndroidUtils.setTextPrimaryColor(view.getContext(), title, !menu.isLight());

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		getMapActivity().getContextMenu().setBaseFragmentVisibility(false);
	}

	@Override
	public void onStop() {
		super.onStop();
		getMapActivity().getContextMenu().setBaseFragmentVisibility(true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		exists = false;
	}

	public static boolean isExists() {
		return exists;
	}

	public static void showInstance(DestinationReachedMenu menu) {
		int slideInAnim = menu.getSlideInAnimation();
		int slideOutAnim = menu.getSlideOutAnimation();

		DestinationReachedMenuFragment fragment = new DestinationReachedMenuFragment();
		fragment.menu = menu;
		menu.getMapActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(slideInAnim, slideOutAnim, slideInAnim, slideOutAnim)
				.add(R.id.fragmentContainer, fragment, TAG)
				.addToBackStack(TAG).commitAllowingStateLoss();
	}

	private void dismissMenu() {
		getMapActivity().getSupportFragmentManager().popBackStack();
	}

	private MapActivity getMapActivity() {
		Activity activity = getActivity();
		if (activity instanceof MapActivity) {
			return (MapActivity) activity;
		} else {
			return null;
		}
	}
}
