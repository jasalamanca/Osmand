package net.osmand.plus.views.mapwidgets;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.Location;
import net.osmand.binary.RouteDataObject;
import net.osmand.data.LatLon;
import net.osmand.plus.CurrentPositionHelper;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.OsmAndLocationProvider;
import net.osmand.plus.OsmAndLocationProvider.GPSInfo;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.OsmandSettings.RulerMode;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.actions.StartGPSStatus;
import net.osmand.plus.dashboard.DashboardOnMap.DashboardType;
import net.osmand.plus.helpers.AndroidUiHelper;
import net.osmand.plus.helpers.WaypointDialogHelper;
import net.osmand.plus.helpers.WaypointHelper;
import net.osmand.plus.helpers.WaypointHelper.LocationPointWrapper;
import net.osmand.plus.mapcontextmenu.other.MapRouteInfoMenu;
import net.osmand.plus.routing.RouteDirectionInfo;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.plus.views.OsmandMapLayer.DrawSettings;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.views.RulerControlLayer;
import net.osmand.plus.views.mapwidgets.NextTurnInfoWidget.TurnDrawable;
import net.osmand.router.TurnType;
import net.osmand.util.Algorithms;
import net.osmand.util.MapUtils;

import java.util.Iterator;
import java.util.LinkedList;

public class MapInfoWidgetsFactory {
	public enum TopToolbarControllerType {
		QUICK_SEARCH,
		CONTEXT_MENU,
		TRACK_DETAILS,
		DISCOUNT,
		MEASUREMENT_TOOL
	}

	public TextInfoWidget createAltitudeControl(final MapActivity map) {
		final TextInfoWidget altitudeControl = new TextInfoWidget(map) {
			private int cachedAlt = 0;

			@Override
			public boolean updateInfo(DrawSettings d) {
				// draw speed
				Location loc = map.getMyApplication().getLocationProvider().getLastKnownLocation();
				if (loc != null && loc.hasAltitude()) {
					double compAlt = loc.getAltitude();
					if (cachedAlt != (int) compAlt) {
						cachedAlt = (int) compAlt;
						String ds = OsmAndFormatter.getFormattedAlt(cachedAlt, map.getMyApplication());
						int ls = ds.lastIndexOf(' ');
						if (ls == -1) {
							setText(ds, null);
						} else {
							setText(ds.substring(0, ls), ds.substring(ls + 1));
						}
						return true;
					}
				} else if (cachedAlt != 0) {
					cachedAlt = 0;
					setText(null, null);
					return true;
				}
				return false;
			}
		};
		altitudeControl.setText(null, null);
		altitudeControl.setIcons(R.drawable.widget_altitude_day, R.drawable.widget_altitude_night);
		return altitudeControl;
	}

	public TextInfoWidget createGPSInfoControl(final MapActivity map) {
		final OsmandApplication app = map.getMyApplication();
		final OsmAndLocationProvider loc = app.getLocationProvider();
		final TextInfoWidget gpsInfoControl = new TextInfoWidget(map) {
			private int u = -1;
			private int f = -1;

			@Override
			public boolean updateInfo(DrawSettings d) {
				GPSInfo gpsInfo = loc.getGPSInfo();
				if (gpsInfo.usedSatellites != u || gpsInfo.foundSatellites != f) {
					u = gpsInfo.usedSatellites;
					f = gpsInfo.foundSatellites;
					setText(gpsInfo.usedSatellites + "/" + gpsInfo.foundSatellites, "");
					return true;
				}
				return false;
			}
		};
		gpsInfoControl.setIcons(R.drawable.widget_gps_info_day, R.drawable.widget_gps_info_night);
		gpsInfoControl.setText(null, null);
		gpsInfoControl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				new StartGPSStatus(map).run();
			}
		});
		return gpsInfoControl;
	}

	public TextInfoWidget createRulerControl(final MapActivity map) {
		final String title = "—";
		final TextInfoWidget rulerControl = new TextInfoWidget(map) {
			final RulerControlLayer rulerLayer = map.getMapLayers().getRulerControlLayer();
			LatLon cacheFirstTouchPoint = new LatLon(0, 0);
			LatLon cacheSecondTouchPoint = new LatLon(0, 0);
			LatLon cacheSingleTouchPoint = new LatLon(0, 0);
			boolean fingerAndLocDistWasShown;

			@Override
			public boolean updateInfo(DrawSettings drawSettings) {
				OsmandMapTileView view = map.getMapView();
				Location currentLoc = map.getMyApplication().getLocationProvider().getLastKnownLocation();

				if (rulerLayer.isShowDistBetweenFingerAndLocation() && currentLoc != null) {
					if (!cacheSingleTouchPoint.equals(rulerLayer.getTouchPointLatLon())) {
						cacheSingleTouchPoint = rulerLayer.getTouchPointLatLon();
						setDistanceText(cacheSingleTouchPoint.getLatitude(), cacheSingleTouchPoint.getLongitude(),
								currentLoc.getLatitude(), currentLoc.getLongitude());
						fingerAndLocDistWasShown = true;
					}
				} else if (rulerLayer.isShowTwoFingersDistance()) {
					if (!cacheFirstTouchPoint.equals(view.getFirstTouchPointLatLon()) ||
							!cacheSecondTouchPoint.equals(view.getSecondTouchPointLatLon()) ||
							fingerAndLocDistWasShown) {
						cacheFirstTouchPoint = view.getFirstTouchPointLatLon();
						cacheSecondTouchPoint = view.getSecondTouchPointLatLon();
						setDistanceText(cacheFirstTouchPoint.getLatitude(), cacheFirstTouchPoint.getLongitude(),
								cacheSecondTouchPoint.getLatitude(), cacheSecondTouchPoint.getLongitude());
						fingerAndLocDistWasShown = false;
					}
				} else {
					LatLon centerLoc = map.getMapLocation();

					if (currentLoc != null && centerLoc != null) {
						if (map.getMapViewTrackingUtilities().isMapLinkedToLocation()) {
							setDistanceText(0);
						} else {
							setDistanceText(currentLoc.getLatitude(), currentLoc.getLongitude(),
									centerLoc.getLatitude(), centerLoc.getLongitude());
						}
					} else {
						setText(title, null);
					}
				}
				return true;
			}

			private void setDistanceText(float dist) {
				calculateAndSetText(dist);
			}

			private void setDistanceText(double firstLat, double firstLon, double secondLat, double secondLon) {
				float dist = (float) MapUtils.getDistance(firstLat, firstLon, secondLat, secondLon);
				calculateAndSetText(dist);
			}

			private void calculateAndSetText(float dist) {
				String distance = OsmAndFormatter.getFormattedDistance(dist, map.getMyApplication());
				int ls = distance.lastIndexOf(' ');
				setText(distance.substring(0, ls), distance.substring(ls + 1));
			}
		};

		rulerControl.setText(title, null);
		setRulerControlIcon(rulerControl, map.getMyApplication().getSettings().RULER_MODE.get());
		rulerControl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				final RulerMode mode = map.getMyApplication().getSettings().RULER_MODE.get();
				RulerMode newMode = RulerMode.FIRST;
				if (mode == RulerMode.FIRST) {
					newMode = RulerMode.SECOND;
				} else if (mode == RulerMode.SECOND) {
					newMode = RulerMode.EMPTY;
				}
				setRulerControlIcon(rulerControl, newMode);
				map.getMyApplication().getSettings().RULER_MODE.set(newMode);
				map.refreshMap();
			}
		});

		return rulerControl;
	}

	private void setRulerControlIcon(TextInfoWidget rulerControl, RulerMode mode) {
		if (mode == RulerMode.FIRST || mode == RulerMode.SECOND) {
			rulerControl.setIcons(R.drawable.widget_ruler_circle_day, R.drawable.widget_ruler_circle_night);
		} else {
			rulerControl.setIcons(R.drawable.widget_hidden_day, R.drawable.widget_hidden_night);
		}
	}

	public static class TopToolbarController {
		private final TopToolbarControllerType type;

		int bgLightId = R.color.bg_color_light;
		int bgDarkId = R.color.bg_color_dark;
		int bgLightLandId = R.drawable.btn_round;
		int bgDarkLandId = R.drawable.btn_round_night;

		int backBtnIconLightId = R.drawable.ic_arrow_back;
		int backBtnIconDarkId = R.drawable.ic_arrow_back;
		int backBtnIconClrLightId = R.color.icon_color;
		int backBtnIconClrDarkId = 0;

		final int closeBtnIconLightId = R.drawable.ic_action_remove_dark;
		final int closeBtnIconDarkId = R.drawable.ic_action_remove_dark;
		int closeBtnIconClrLightId = R.color.icon_color;
		int closeBtnIconClrDarkId = 0;
		boolean closeBtnVisible = true;

		final int refreshBtnIconLightId = R.drawable.ic_action_refresh_dark;
		final int refreshBtnIconDarkId = R.drawable.ic_action_refresh_dark;
		int refreshBtnIconClrLightId = R.color.icon_color;
		int refreshBtnIconClrDarkId = 0;

		final boolean refreshBtnVisible = false;
		boolean saveViewVisible = false;
		boolean topBarSwitchVisible = false;
		boolean topBarSwitchChecked = false;

		int titleTextClrLightId = R.color.primary_text_light;
		int titleTextClrDarkId = R.color.primary_text_dark;
		int descrTextClrLightId = R.color.primary_text_light;
		int descrTextClrDarkId = R.color.primary_text_dark;
		boolean singleLineTitle = true;
		String title = "";
		String description = null;
		int saveViewTextId = -1;

		OnClickListener onBackButtonClickListener;
		OnClickListener onTitleClickListener;
		OnClickListener onCloseButtonClickListener;
		OnClickListener onSaveViewClickListener;
		OnCheckedChangeListener onSwitchCheckedChangeListener;

		public TopToolbarController(TopToolbarControllerType type) {
			this.type = type;
		}

		TopToolbarControllerType getType() {
			return type;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		public void setSingleLineTitle(boolean singleLineTitle) {
			this.singleLineTitle = singleLineTitle;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setBgIds(int bgLightId, int bgDarkId, int bgLightLandId, int bgDarkLandId) {
			this.bgLightId = bgLightId;
			this.bgDarkId = bgDarkId;
			this.bgLightLandId = bgLightLandId;
			this.bgDarkLandId = bgDarkLandId;
		}

		public void setBackBtnIconIds(int backBtnIconLightId, int backBtnIconDarkId) {
			this.backBtnIconLightId = backBtnIconLightId;
			this.backBtnIconDarkId = backBtnIconDarkId;
		}

		public void setBackBtnIconClrIds(int backBtnIconClrLightId, int backBtnIconClrDarkId) {
			this.backBtnIconClrLightId = backBtnIconClrLightId;
			this.backBtnIconClrDarkId = backBtnIconClrDarkId;
		}

		public void setCloseBtnIconClrIds(int closeBtnIconClrLightId, int closeBtnIconClrDarkId) {
			this.closeBtnIconClrLightId = closeBtnIconClrLightId;
			this.closeBtnIconClrDarkId = closeBtnIconClrDarkId;
		}

		public void setRefreshBtnIconClrIds(int refreshBtnIconClrLightId, int refreshBtnIconClrDarkId) {
			this.refreshBtnIconClrLightId = refreshBtnIconClrLightId;
			this.refreshBtnIconClrDarkId = refreshBtnIconClrDarkId;
		}

		public void setCloseBtnVisible(boolean closeBtnVisible) {
			this.closeBtnVisible = closeBtnVisible;
		}

		public void setSaveViewVisible(boolean visible) {
			this.saveViewVisible = visible;
		}

		public void setSaveViewTextId(int id) {
			this.saveViewTextId = id;
		}

		public void setTopBarSwitchVisible(boolean visible) {
			this.topBarSwitchVisible = visible;
		}

		public void setTopBarSwitchChecked(boolean checked) {
			this.topBarSwitchChecked = checked;
		}

		public void setTitleTextClrIds(int titleTextClrLightId, int titleTextClrDarkId) {
			this.titleTextClrLightId = titleTextClrLightId;
			this.titleTextClrDarkId = titleTextClrDarkId;
		}

		public void setDescrTextClrIds(int descrTextClrLightId, int descrTextClrDarkId) {
			this.descrTextClrLightId = descrTextClrLightId;
			this.descrTextClrDarkId = descrTextClrDarkId;
		}

		public void setOnBackButtonClickListener(OnClickListener onBackButtonClickListener) {
			this.onBackButtonClickListener = onBackButtonClickListener;
		}

		public void setOnTitleClickListener(OnClickListener onTitleClickListener) {
			this.onTitleClickListener = onTitleClickListener;
		}

		public void setOnCloseButtonClickListener(OnClickListener onCloseButtonClickListener) {
			this.onCloseButtonClickListener = onCloseButtonClickListener;
		}

		public void setOnSaveViewClickListener(OnClickListener onSaveViewClickListener) {
			this.onSaveViewClickListener = onSaveViewClickListener;
		}

		public void setOnSwitchCheckedChangeListener(OnCheckedChangeListener onSwitchCheckedChangeListener) {
			this.onSwitchCheckedChangeListener = onSwitchCheckedChangeListener;
		}

		public void updateToolbar(TopToolbarView view) {
			TextView titleView = view.getTitleView();
			TextView descrView = view.getDescrView();
			LinearLayout bottomViewLayout = view.getBottomViewLayout();
			Switch barSwitch = view.getTopBarSwitch();
			if (title != null) {
				titleView.setText(title);
				view.updateVisibility(titleView, true);
			} else {
				view.updateVisibility(titleView, false);
			}
			if (description != null) {
				descrView.setText(description);
				view.updateVisibility(descrView, true);
			} else {
				view.updateVisibility(descrView, false);
			}
			view.updateVisibility(bottomViewLayout, false);
			view.updateVisibility(barSwitch, topBarSwitchVisible);
			if (topBarSwitchVisible) {
				barSwitch.setChecked(topBarSwitchChecked);
			}
			if (view.getShadowView() != null) {
				view.getShadowView().setVisibility(View.VISIBLE);
			}
		}
	}

	public static class TopToolbarView {
		private final MapActivity map;
		private final LinkedList<TopToolbarController> controllers = new LinkedList<>();
		private final TopToolbarController defaultController = new TopToolbarController(TopToolbarControllerType.CONTEXT_MENU);
		private final View topbar;
		private final View topBarLayout;
		private final View topBarBottomView;
		private final View topBarTitleLayout;
		private final ImageButton backButton;
		private final TextView titleView;
		private final TextView descrView;
		private final ImageButton refreshButton;
		private final ImageButton closeButton;
		private final TextView saveView;
		private final Switch topBarSwitch;
		private final View shadowView;
		private boolean nightMode;

		public TopToolbarView(final MapActivity map) {
			this.map = map;

			topbar = map.findViewById(R.id.widget_top_bar);
			topBarLayout = map.findViewById(R.id.widget_top_bar_layout);
			topBarBottomView = map.findViewById(R.id.widget_top_bar_bottom_view);
			topBarTitleLayout = map.findViewById(R.id.widget_top_bar_title_layout);
			backButton = map.findViewById(R.id.widget_top_bar_back_button);
			refreshButton = map.findViewById(R.id.widget_top_bar_refresh_button);
			closeButton = map.findViewById(R.id.widget_top_bar_close_button);
			titleView = map.findViewById(R.id.widget_top_bar_title);
			saveView = map.findViewById(R.id.widget_top_bar_save);
			descrView = map.findViewById(R.id.widget_top_bar_description);
			topBarSwitch = map.findViewById(R.id.widget_top_bar_switch);
			shadowView = map.findViewById(R.id.widget_top_bar_shadow);
			updateVisibility(false);
		}

		TextView getTitleView() {
			return titleView;
		}

		LinearLayout getBottomViewLayout() {
			return (LinearLayout) topBarBottomView;
		}

		TextView getDescrView() {
			return descrView;
		}

		Switch getTopBarSwitch() {
			return topBarSwitch;
		}

		public View getShadowView() {
			return shadowView;
		}

		public TopToolbarController getTopController() {
			if (controllers.size() > 0) {
				return controllers.get(controllers.size() - 1);
			} else {
				return null;
			}
		}

		public TopToolbarController getController(TopToolbarControllerType type) {
			for (TopToolbarController controller : controllers) {
				if (controller.getType() == type) {
					return controller;
				}
			}
			return null;
		}

		public void addController(TopToolbarController controller) {
			for (Iterator ctrlIter = controllers.iterator(); ctrlIter.hasNext(); ) {
				TopToolbarController ctrl = (TopToolbarController) ctrlIter.next();
				if (ctrl.getType() == controller.getType()) {
					ctrlIter.remove();
				}
			}
			controllers.add(controller);
			updateColors();
			updateInfo();
		}

		public void removeController(TopToolbarController controller) {
			controllers.remove(controller);
			updateColors();
			updateInfo();
		}

		void updateVisibility(boolean visible) {
			updateVisibility(topbar, visible);
		}

		void updateVisibility(View v, boolean visible) {
			if (visible != (v.getVisibility() == View.VISIBLE)) {
				if (visible) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.GONE);
				}
				v.invalidate();
			}
		}

		private void initToolbar(TopToolbarController controller) {
			backButton.setOnClickListener(controller.onBackButtonClickListener);
			topBarTitleLayout.setOnClickListener(controller.onTitleClickListener);
			closeButton.setOnClickListener(controller.onCloseButtonClickListener);
			saveView.setOnClickListener(controller.onSaveViewClickListener);
			topBarSwitch.setOnCheckedChangeListener(controller.onSwitchCheckedChangeListener);
		}

		public void updateInfo() {
			TopToolbarController controller = getTopController();
			if (controller != null) {
				initToolbar(controller);
				controller.updateToolbar(this);
			} else {
				initToolbar(defaultController);
				defaultController.updateToolbar(this);
			}
			updateVisibility(controller != null && (!map.getContextMenu().isVisible() || controller.getType() == TopToolbarControllerType.CONTEXT_MENU));
		}

		void updateColors(TopToolbarController controller) {
			OsmandApplication app = map.getMyApplication();
			if (nightMode) {
				topBarLayout.setBackgroundResource(AndroidUiHelper.isOrientationPortrait(map) ? controller.bgDarkId : controller.bgDarkLandId);
				if (controller.backBtnIconDarkId == 0) {
					backButton.setImageDrawable(null);
				} else {
					backButton.setImageDrawable(app.getIconsCache().getIcon(controller.backBtnIconDarkId, controller.backBtnIconClrDarkId));
				}
				closeButton.setImageDrawable(app.getIconsCache().getIcon(controller.closeBtnIconDarkId, controller.closeBtnIconClrDarkId));
				refreshButton.setImageDrawable(app.getIconsCache().getIcon(controller.refreshBtnIconDarkId, controller.refreshBtnIconClrDarkId));
				int titleColor = map.getResources().getColor(controller.titleTextClrDarkId);
				int descrColor = map.getResources().getColor(controller.descrTextClrDarkId);
				titleView.setTextColor(titleColor);
				descrView.setTextColor(descrColor);
				saveView.setTextColor(titleColor);
			} else {
				topBarLayout.setBackgroundResource(AndroidUiHelper.isOrientationPortrait(map) ? controller.bgLightId : controller.bgLightLandId);
				if (controller.backBtnIconLightId == 0) {
					backButton.setImageDrawable(null);
				} else {
					backButton.setImageDrawable(app.getIconsCache().getIcon(controller.backBtnIconLightId, controller.backBtnIconClrLightId));
				}
				closeButton.setImageDrawable(app.getIconsCache().getIcon(controller.closeBtnIconLightId, controller.closeBtnIconClrLightId));
				refreshButton.setImageDrawable(app.getIconsCache().getIcon(controller.refreshBtnIconLightId, controller.refreshBtnIconClrLightId));
				int titleColor = map.getResources().getColor(controller.titleTextClrLightId);
				int descrColor = map.getResources().getColor(controller.descrTextClrLightId);
				titleView.setTextColor(titleColor);
				descrView.setTextColor(descrColor);
				saveView.setTextColor(titleColor);
			}
			if (controller.singleLineTitle) {
				titleView.setSingleLine(true);
			} else {
				titleView.setSingleLine(false);
			}

			if (controller.closeBtnVisible) {
				if (closeButton.getVisibility() == View.GONE) {
					closeButton.setVisibility(View.VISIBLE);
				}
			} else if (closeButton.getVisibility() == View.VISIBLE) {
				closeButton.setVisibility(View.GONE);
			}
			if (controller.refreshBtnVisible) {
				if (refreshButton.getVisibility() == View.GONE) {
					refreshButton.setVisibility(View.VISIBLE);
				}
			} else if (refreshButton.getVisibility() == View.VISIBLE) {
				refreshButton.setVisibility(View.GONE);
			}
			if (controller.saveViewVisible) {
				if (controller.saveViewTextId != -1) {
					saveView.setText(map.getString(controller.saveViewTextId));
					saveView.setContentDescription(map.getString(controller.saveViewTextId));
				}
				if (saveView.getVisibility() == View.GONE) {
					saveView.setVisibility(View.VISIBLE);
				}
			} else if (saveView.getVisibility() == View.VISIBLE) {
				saveView.setVisibility(View.GONE);
			}
		}

		void updateColors() {
			TopToolbarController controller = getTopController();
			if (controller != null) {
				updateColors(controller);
			} else {
				updateColors(defaultController);
			}
		}

		public void updateColors(boolean nightMode) {
			this.nightMode = nightMode;
			updateColors();
		}
	}

	public static class TopTextView {
		private final RoutingHelper routingHelper;
		private final MapActivity map;
		private final View topBar;
		private final TextView addressText;
		private final TextView addressTextShadow;
		private final OsmAndLocationProvider locationProvider;
		private final WaypointHelper waypointHelper;
		private final OsmandSettings settings;
		private final View waypointInfoBar;
		private LocationPointWrapper lastPoint;
		private final TurnDrawable turnDrawable;
		private boolean showMarker;
		private int shadowRad;

		public TopTextView(OsmandApplication app, MapActivity map) {
			topBar = map.findViewById(R.id.map_top_bar);
			addressText = map.findViewById(R.id.map_address_text);
			addressTextShadow = map.findViewById(R.id.map_address_text_shadow);
			waypointInfoBar = map.findViewById(R.id.waypoint_info_bar);
			this.routingHelper = app.getRoutingHelper();
			locationProvider = app.getLocationProvider();
			this.map = map;
			settings = app.getSettings();
			waypointHelper = app.getWaypointHelper();
			updateVisibility(false);
			turnDrawable = new NextTurnInfoWidget.TurnDrawable(map, true);
		}

		void updateVisibility(boolean visible) {
			if (updateVisibility(topBar, visible)) {
				map.updateStatusBarColor();
			}
		}

		boolean updateVisibility(View v, boolean visible) {
			if (visible != (v.getVisibility() == View.VISIBLE)) {
				if (visible) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.GONE);
				}
				v.invalidate();
				return true;
			}
			return false;
		}

		public void updateTextColor(boolean nightMode, int textColor, int textShadowColor, boolean bold, int rad) {
			this.shadowRad = rad;
			TextInfoWidget.updateTextColor(addressText, addressTextShadow, textColor, textShadowColor, bold, rad);
			TextInfoWidget.updateTextColor((TextView) waypointInfoBar.findViewById(R.id.waypoint_text),
					(TextView) waypointInfoBar.findViewById(R.id.waypoint_text_shadow),
					textColor, textShadowColor, bold, rad / 2);

			ImageView all = waypointInfoBar.findViewById(R.id.waypoint_more);
			ImageView remove = waypointInfoBar.findViewById(R.id.waypoint_close);
			all.setImageDrawable(map.getMyApplication().getIconsCache()
					.getIcon(R.drawable.ic_overflow_menu_white, !nightMode));
			remove.setImageDrawable(map.getMyApplication().getIconsCache()
					.getIcon(R.drawable.ic_action_remove_dark, !nightMode));
		}


		public boolean updateInfo(DrawSettings d) {
			String text = null;
			TurnType[] type = new TurnType[1];
			boolean showNextTurn = false;
			boolean showMarker = this.showMarker;
			if (routingHelper != null && routingHelper.isRouteCalculated() && !routingHelper.isDeviatedFromRoute()) {
				if (routingHelper.isFollowingMode()) {
					if (settings.SHOW_STREET_NAME.get()) {
						text = routingHelper.getCurrentName(type);
						if (text == null) {
							text = "";
						} else {
							if (type[0] == null) {
								showMarker = true;
							} else {
								turnDrawable.setColor(R.color.nav_arrow);
							}
						}
					}
				} else {
					int di = MapRouteInfoMenu.getDirectionInfo();
					if (di >= 0 && MapRouteInfoMenu.isVisible() &&
							di < routingHelper.getRouteDirections().size()) {
						showNextTurn = true;
						RouteDirectionInfo next = routingHelper.getRouteDirections().get(di);
						type[0] = next.getTurnType();
						turnDrawable.setColor(R.color.nav_arrow_distant);
						text = RoutingHelper.formatStreetName(next.getStreetName(), next.getRef(), next.getDestinationName(), "»");
					} else {
						text = null;
					}
				}
			} else if (map.getMapViewTrackingUtilities().isMapLinkedToLocation() &&
					settings.SHOW_STREET_NAME.get()) {
				RouteDataObject rt = locationProvider.getLastKnownRouteSegment();
				if (rt != null) {
					Location lastKnownLocation = locationProvider.getLastKnownLocation();
					text = RoutingHelper.formatStreetName(
							rt.getName(settings.MAP_PREFERRED_LOCALE.get(), settings.MAP_TRANSLITERATE_NAMES.get()),
							rt.getRef(settings.MAP_PREFERRED_LOCALE.get(), settings.MAP_TRANSLITERATE_NAMES.get(), rt.bearingVsRouteDirection(lastKnownLocation)),
							rt.getDestinationName(settings.MAP_PREFERRED_LOCALE.get(), settings.MAP_TRANSLITERATE_NAMES.get(), rt.bearingVsRouteDirection(lastKnownLocation)),
							"»");
				}
				if (text == null) {
					text = "";
				} else {
					Location lastKnownLocation = locationProvider.getLastKnownLocation();
					if (!Algorithms.isEmpty(text) && lastKnownLocation != null) {
						double dist =
								CurrentPositionHelper.getOrthogonalDistance(rt, lastKnownLocation);
						if (dist < 50) {
							showMarker = true;
						} else {
							text = map.getResources().getString(R.string.shared_string_near) + " " + text;
						}
					}
				}
			}
			if (map.isTopToolbarActive() || !map.getContextMenu().shouldShowTopControls()) {
				updateVisibility(false);
			} else if (!showNextTurn && updateWaypoint()) {
				updateVisibility(true);
				updateVisibility(addressText, false);
				updateVisibility(addressTextShadow, false);
			} else if (text == null) {
				updateVisibility(false);
			} else {
				updateVisibility(true);
				updateVisibility(waypointInfoBar, false);
				updateVisibility(addressText, true);
				updateVisibility(addressTextShadow, shadowRad > 0);
				boolean update = turnDrawable.setTurnType(type[0]) || showMarker != this.showMarker;
				this.showMarker = showMarker;
				int h = addressText.getHeight() / 4 * 3;
				if (h != turnDrawable.getBounds().bottom) {
					turnDrawable.setBounds(0, 0, h, h);
				}
				if (update) {
					if (type[0] != null) {
						addressTextShadow.setCompoundDrawables(turnDrawable, null, null, null);
						addressTextShadow.setCompoundDrawablePadding(4);
						addressText.setCompoundDrawables(turnDrawable, null, null, null);
						addressText.setCompoundDrawablePadding(4);
					} else if (showMarker) {
						Drawable marker = map.getMyApplication().getIconsCache().getIcon(R.drawable.ic_action_start_navigation, R.color.color_myloc_distance);
						addressTextShadow.setCompoundDrawablesWithIntrinsicBounds(marker, null, null, null);
						addressTextShadow.setCompoundDrawablePadding(4);
						addressText.setCompoundDrawablesWithIntrinsicBounds(marker, null, null, null);
						addressText.setCompoundDrawablePadding(4);
					} else {
						addressTextShadow.setCompoundDrawables(null, null, null, null);
						addressText.setCompoundDrawables(null, null, null, null);
					}
				}
				if (!text.equals(addressText.getText().toString())) {
					addressTextShadow.setText(text);
					addressText.setText(text);
					return true;
				}
			}
			return false;
		}

		boolean updateWaypoint() {
			final LocationPointWrapper pnt = waypointHelper.getMostImportantLocationPoint(null);
			boolean changed = this.lastPoint != pnt;
			this.lastPoint = pnt;
			if (pnt == null) {
				topBar.setOnClickListener(null);
				updateVisibility(waypointInfoBar, false);
				return false;
			} else {
				updateVisibility(addressText, false);
				updateVisibility(addressTextShadow, false);
				boolean updated = updateVisibility(waypointInfoBar, true);
				// pass top bar to make it clickable
				WaypointDialogHelper.updatePointInfoView(map.getMyApplication(), map, topBar, pnt, true,
						map.getMyApplication().getDaynightHelper().isNightModeForMapControls(), false, true);
				if (updated || changed) {
					ImageView all = waypointInfoBar.findViewById(R.id.waypoint_more);
					ImageView remove = waypointInfoBar.findViewById(R.id.waypoint_close);
					all.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							map.getDashboard().setDashboardVisibility(true, DashboardType.WAYPOINTS, AndroidUtils.getCenterViewCoordinates(view));
						}
					});
					remove.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							waypointHelper.removeVisibleLocationPoint(pnt);
							map.refreshMap();
						}
					});
				}
				return true;
			}
		}

		public void setBackgroundResource(int boxTop) {
			topBar.setBackgroundResource(boxTop);
		}
	}
}
