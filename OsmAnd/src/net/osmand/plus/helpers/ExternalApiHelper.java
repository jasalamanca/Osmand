package net.osmand.plus.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import net.osmand.AndroidUtils;
import net.osmand.IndexConstants;
import net.osmand.Location;
import net.osmand.PlatformUtil;
import net.osmand.data.FavouritePoint;
import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.FavouritesDbHelper;
import net.osmand.plus.GPXUtilities;
import net.osmand.plus.GPXUtilities.GPXFile;
import net.osmand.plus.MapMarkersHelper;
import net.osmand.plus.MapMarkersHelper.MapMarker;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.mapcontextmenu.MapContextMenu;
import net.osmand.plus.monitoring.OsmandMonitoringPlugin;
import net.osmand.plus.routing.RouteCalculationResult.NextDirectionInfo;
import net.osmand.plus.routing.RouteDirectionInfo;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.router.TurnType;
import net.osmand.util.Algorithms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.Arrays;

public class ExternalApiHelper {
	private static final org.apache.commons.logging.Log LOG = PlatformUtil.getLog(ExternalApiHelper.class);

	private static final String API_CMD_SHOW_GPX = "show_gpx";
	private static final String API_CMD_NAVIGATE_GPX = "navigate_gpx";
	private static final String API_CMD_NAVIGATE = "navigate";
	private static final String API_CMD_GET_INFO = "get_info";
	private static final String API_CMD_ADD_FAVORITE = "add_favorite";
	private static final String API_CMD_ADD_MAP_MARKER = "add_map_marker";
	private static final String API_CMD_START_GPX_REC = "start_gpx_rec";
	private static final String API_CMD_STOP_GPX_REC = "stop_gpx_rec";
	private static final String API_CMD_SUBSCRIBE_VOICE_NOTIFICATIONS = "subscribe_voice_notifications";
	private static final int VERSION_CODE = 1;

	private static final String PARAM_NAME = "name";
	private static final String PARAM_DESC = "desc";
	private static final String PARAM_CATEGORY = "category";
	private static final String PARAM_LAT = "lat";
	private static final String PARAM_LON = "lon";
	private static final String PARAM_COLOR = "color";
	private static final String PARAM_VISIBLE = "visible";

	private static final String PARAM_PATH = "path";
	private static final String PARAM_URI = "uri";
	private static final String PARAM_DATA = "data";
	private static final String PARAM_FORCE = "force";

	private static final String PARAM_START_NAME = "start_name";
	private static final String PARAM_DEST_NAME = "dest_name";
	private static final String PARAM_START_LAT = "start_lat";
	private static final String PARAM_START_LON = "start_lon";
	private static final String PARAM_DEST_LAT = "dest_lat";
	private static final String PARAM_DEST_LON = "dest_lon";
	private static final String PARAM_PROFILE = "profile";

	private static final String PARAM_VERSION = "version";
	private static final String PARAM_ETA = "eta";
	private static final String PARAM_TIME_LEFT = "time_left";
	private static final String PARAM_DISTANCE_LEFT = "time_distance_left";
	private static final String PARAM_NT_DISTANCE = "turn_distance";
	private static final String PARAM_NT_IMMINENT = "turn_imminent";
	private static final String PARAM_NT_DIRECTION_NAME = "turn_name";
	private static final String PARAM_NT_DIRECTION_TURN = "turn_type";
	private static final String PARAM_NT_DIRECTION_LANES = "turn_lanes";

	private static final String PARAM_CLOSE_AFTER_COMMAND = "close_after_command";


	private static final ApplicationMode[] VALID_PROFILES = new ApplicationMode[]{
			ApplicationMode.CAR,
			ApplicationMode.BICYCLE,
			ApplicationMode.PEDESTRIAN
	};

	private static final ApplicationMode DEFAULT_PROFILE = ApplicationMode.CAR;

	// RESULT_OK == -1
	// RESULT_CANCELED == 0
	// RESULT_FIRST_USER == 1
	// from Activity
	private static final int RESULT_CODE_ERROR_UNKNOWN = 1001;
	private static final int RESULT_CODE_ERROR_NOT_IMPLEMENTED = 1002;
	private static final int RESULT_CODE_ERROR_PLUGIN_INACTIVE = 1003;
	private static final int RESULT_CODE_ERROR_GPX_NOT_FOUND = 1004;
	private static final int RESULT_CODE_ERROR_INVALID_PROFILE = 1005;

	private final MapActivity mapActivity;
	private int resultCode;
	private boolean finish;

	public int getResultCode() {
		return resultCode;
	}
	public boolean needFinish() {
		return finish;
	}
	public ExternalApiHelper(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
	}

	public Intent processApiRequest(Intent intent) {

		Intent result = new Intent();
		OsmandApplication app = (OsmandApplication) mapActivity.getApplication();

		try {
			Uri uri = intent.getData();
			String cmd = uri.getHost().toLowerCase();
			if (API_CMD_SHOW_GPX.equals(cmd) || API_CMD_NAVIGATE_GPX.equals(cmd)) {
				boolean navigate = API_CMD_NAVIGATE_GPX.equals(cmd);
				String path = uri.getQueryParameter(PARAM_PATH);
				boolean force = uri.getBooleanQueryParameter(PARAM_FORCE, false);

				GPXFile gpx = null;
				if (path != null) {
					File f = new File(path);
					if (f.exists()) {
						gpx = GPXUtilities.loadGPXFile(mapActivity, f);
					}
				} else if (intent.getStringExtra(PARAM_DATA) != null) {
					String gpxStr = intent.getStringExtra(PARAM_DATA);
					if (!Algorithms.isEmpty(gpxStr)) {
						gpx = GPXUtilities.loadGPXFile(mapActivity, new ByteArrayInputStream(gpxStr.getBytes()));
					}
				} else if (uri.getBooleanQueryParameter(PARAM_URI, false)) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						LOG.debug("uriString=" + intent.getClipData().getItemAt(0).getUri());
						Uri gpxUri = intent.getClipData().getItemAt(0).getUri();

						ParcelFileDescriptor gpxParcelDescriptor = mapActivity.getContentResolver()
								.openFileDescriptor(gpxUri, "r");
						if (gpxParcelDescriptor != null) {
							FileDescriptor fileDescriptor = gpxParcelDescriptor.getFileDescriptor();
							gpx = GPXUtilities.loadGPXFile(mapActivity, new FileInputStream(fileDescriptor));
						} else {
							finish = true;
							resultCode = RESULT_CODE_ERROR_GPX_NOT_FOUND;
						}
					} else {
						finish = true;
						resultCode = RESULT_CODE_ERROR_GPX_NOT_FOUND;
					}
				} else {
					finish = true;
					resultCode = RESULT_CODE_ERROR_GPX_NOT_FOUND;
				}

				if (gpx != null) {
					if (navigate) {
						final RoutingHelper routingHelper = app.getRoutingHelper();
						if (routingHelper.isFollowingMode() && !force) {
							final GPXFile gpxFile = gpx;
							AlertDialog dlg = mapActivity.getMapActions().stopNavigationActionConfirm();
							dlg.setOnDismissListener(dialog -> {
								if (!routingHelper.isFollowingMode()) {
									startNavigation(gpxFile, null, null, null, null, null);
								}
							});
						} else {
							startNavigation(gpx, null, null, null, null, null);
						}
					} else {
						app.getSelectedGpxHelper().setGpxFileToDisplay(gpx);
					}
					resultCode = Activity.RESULT_OK;
				} else {
					finish = true;
					resultCode = RESULT_CODE_ERROR_GPX_NOT_FOUND;
				}

			} else if (API_CMD_NAVIGATE.equals(cmd)) {
				String profileStr = uri.getQueryParameter(PARAM_PROFILE);
				final ApplicationMode profile = ApplicationMode.valueOfStringKey(profileStr, DEFAULT_PROFILE);
				boolean validProfile = false;
				for (ApplicationMode mode : VALID_PROFILES) {
					if (mode == profile) {
						validProfile = true;
						break;
					}
				}
				if (!validProfile) {
					resultCode = RESULT_CODE_ERROR_INVALID_PROFILE;
				} else {
					String startName = uri.getQueryParameter(PARAM_START_NAME);
					if (Algorithms.isEmpty(startName)) {
						startName = "";
					}
					String destName = uri.getQueryParameter(PARAM_DEST_NAME);
					if (Algorithms.isEmpty(destName)) {
						destName = "";
					}


					final LatLon start;
					final PointDescription startDesc;
					String startLatStr = uri.getQueryParameter(PARAM_START_LAT);
					String startLonStr = uri.getQueryParameter(PARAM_START_LON);
					if (!Algorithms.isEmpty(startLatStr) && !Algorithms.isEmpty(startLonStr)) {
						double lat = Double.parseDouble(uri.getQueryParameter(PARAM_START_LAT));
						double lon = Double.parseDouble(uri.getQueryParameter(PARAM_START_LON));
						start = new LatLon(lat, lon);
						startDesc = new PointDescription(PointDescription.POINT_TYPE_LOCATION, startName);
					} else {
						start = null;
						startDesc = null;
					}

					double destLat = Double.parseDouble(uri.getQueryParameter(PARAM_DEST_LAT));
					double destLon = Double.parseDouble(uri.getQueryParameter(PARAM_DEST_LON));
					final LatLon dest = new LatLon(destLat, destLon);
					final PointDescription destDesc = new PointDescription(PointDescription.POINT_TYPE_LOCATION, destName);

					boolean force = uri.getBooleanQueryParameter(PARAM_FORCE, false);

					final RoutingHelper routingHelper = app.getRoutingHelper();
					if (routingHelper.isFollowingMode() && !force) {
						AlertDialog dlg = mapActivity.getMapActions().stopNavigationActionConfirm();
						dlg.setOnDismissListener(dialog -> {
							if (!routingHelper.isFollowingMode()) {
								startNavigation(null, start, startDesc, dest, destDesc, profile);
							}
						});
					} else {
						startNavigation(null, start, startDesc, dest, destDesc, profile);
					}
				}

			} else if (API_CMD_GET_INFO.equals(cmd)) {

				Location location = mapActivity.getMyApplication().getLocationProvider().getLastKnownLocation();
				if (location != null) {
					result.putExtra(PARAM_LAT, location.getLatitude());
					result.putExtra(PARAM_LON, location.getLongitude());
				}

				final RoutingHelper routingHelper = app.getRoutingHelper();
				if (routingHelper.isRouteCalculated()) {
					int time = routingHelper.getLeftTime();
					result.putExtra(PARAM_TIME_LEFT, time);
					long eta = time + System.currentTimeMillis() / 1000;
					result.putExtra(PARAM_ETA, eta);
					result.putExtra(PARAM_DISTANCE_LEFT, routingHelper.getLeftDistance());

					NextDirectionInfo ni = routingHelper.getNextRouteDirectionInfo(new NextDirectionInfo(), true);
					if (ni.distanceTo > 0) {
						updateTurnInfo("next_", result, ni);
						ni = routingHelper.getNextRouteDirectionInfoAfter(ni, new NextDirectionInfo(), true);
						if (ni.distanceTo > 0) {
							updateTurnInfo("after_next", result, ni);
						}
					}
					routingHelper.getNextRouteDirectionInfo(new NextDirectionInfo(), false);
					if (ni.distanceTo > 0) {
						updateTurnInfo("no_speak_next_", result, ni);
					}
				}
				result.putExtra(PARAM_VERSION, VERSION_CODE);

				finish = true;
				resultCode = Activity.RESULT_OK;

			} else if (API_CMD_ADD_FAVORITE.equals(cmd)) {
				String name = uri.getQueryParameter(PARAM_NAME);
				String desc = uri.getQueryParameter(PARAM_DESC);
				String category = uri.getQueryParameter(PARAM_CATEGORY);
				double lat = Double.parseDouble(uri.getQueryParameter(PARAM_LAT));
				double lon = Double.parseDouble(uri.getQueryParameter(PARAM_LON));
				String colorTag = uri.getQueryParameter(PARAM_COLOR);
				boolean visible = uri.getBooleanQueryParameter(PARAM_VISIBLE, true);

				if (name == null) {
					name = "";
				}
				if (desc == null) {
					desc = "";
				}
				if (category == null) {
					category = "";
				}

				int color = 0;
				if (!Algorithms.isEmpty(colorTag)) {
					color = ColorDialogs.getColorByTag(colorTag);
					if (color == 0) {
						LOG.error("Wrong color tag: " + colorTag);
					}
				}

				FavouritePoint fav = new FavouritePoint(lat, lon, name, category);
				fav.setDescription(desc);
				fav.setColor(color);
				fav.setVisible(visible);

				FavouritesDbHelper helper = app.getFavorites();
				helper.addFavourite(fav);

				showOnMap(lat, lon, fav, mapActivity.getMapLayers().getFavouritesLayer().getObjectName(fav));
				resultCode = Activity.RESULT_OK;

			} else if (API_CMD_ADD_MAP_MARKER.equals(cmd)) {
				double lat = Double.parseDouble(uri.getQueryParameter(PARAM_LAT));
				double lon = Double.parseDouble(uri.getQueryParameter(PARAM_LON));
				String name = uri.getQueryParameter(PARAM_NAME);

				PointDescription pd = new PointDescription(
						PointDescription.POINT_TYPE_MAP_MARKER, name != null ? name : "");

				MapMarkersHelper markersHelper = app.getMapMarkersHelper();
				markersHelper.addMapMarker(new LatLon(lat, lon), pd);

				MapMarker marker = markersHelper.getFirstMapMarker();
				if (marker != null) {
					showOnMap(lat, lon, marker, mapActivity.getMapLayers().getMapMarkersLayer().getObjectName(marker));
				}
				resultCode = Activity.RESULT_OK;

			} else if (API_CMD_START_GPX_REC.equals(cmd)) {
				OsmandMonitoringPlugin plugin = OsmandPlugin.getEnabledPlugin(OsmandMonitoringPlugin.class);
				if (plugin == null) {
					resultCode = RESULT_CODE_ERROR_PLUGIN_INACTIVE;
					finish = true;
				} else {
					plugin.startGPXMonitoring(null);
				}

				if (uri.getBooleanQueryParameter(PARAM_CLOSE_AFTER_COMMAND, true)) {
					finish = true;
				}
				resultCode = Activity.RESULT_OK;
			} else if (API_CMD_STOP_GPX_REC.equals(cmd)) {
				OsmandMonitoringPlugin plugin = OsmandPlugin.getEnabledPlugin(OsmandMonitoringPlugin.class);
				if (plugin == null) {
					resultCode = RESULT_CODE_ERROR_PLUGIN_INACTIVE;
					finish = true;
				} else {
					plugin.stopRecording();
				}

				if (uri.getBooleanQueryParameter(PARAM_CLOSE_AFTER_COMMAND, true)) {
					finish = true;
				}
				resultCode = Activity.RESULT_OK;
			} else if (API_CMD_SUBSCRIBE_VOICE_NOTIFICATIONS.equals(cmd)) {
				// not implemented yet
				resultCode = RESULT_CODE_ERROR_NOT_IMPLEMENTED;
			}

		} catch (Exception e) {
			LOG.error("Error processApiRequest:", e);
			resultCode = RESULT_CODE_ERROR_UNKNOWN;
		}

		return result;
	}

	private void updateTurnInfo(String prefix, Intent result, NextDirectionInfo ni) {
		result.putExtra(prefix + PARAM_NT_DISTANCE, ni.distanceTo);
		result.putExtra(prefix + PARAM_NT_IMMINENT, ni.imminent);
		if (ni.directionInfo != null && ni.directionInfo.getTurnType() != null) {
			TurnType tt = ni.directionInfo.getTurnType();
			RouteDirectionInfo a = ni.directionInfo;
			result.putExtra(prefix + PARAM_NT_DIRECTION_NAME, RoutingHelper.formatStreetName(a.getStreetName(), a.getRef(), a.getDestinationName(), ""));
			result.putExtra(prefix + PARAM_NT_DIRECTION_TURN, tt.toXmlString());
			if (tt.getLanes() != null) {
				result.putExtra(prefix + PARAM_NT_DIRECTION_LANES, Arrays.toString(tt.getLanes()));
			}
		}
	}

	private void showOnMap(double lat, double lon, Object object, PointDescription pointDescription) {
		MapContextMenu mapContextMenu = mapActivity.getContextMenu();
		mapContextMenu.setMapCenter(new LatLon(lat, lon));
		mapContextMenu.setMapPosition(mapActivity.getMapView().getMapPosition());
		mapContextMenu.setCenterMarker(true);
		mapContextMenu.setMapZoom(15);
		mapContextMenu.show(new LatLon(lat, lon), pointDescription, object);
	}

	private void startNavigation(GPXFile gpx,
								 LatLon from, PointDescription fromDesc,
								 LatLon to, PointDescription toDesc,
								 ApplicationMode mode) {
		OsmandApplication app = mapActivity.getMyApplication();
		RoutingHelper routingHelper = app.getRoutingHelper();
		if (gpx == null) {
			app.getSettings().APPLICATION_MODE.set(mode);
			final TargetPointsHelper targets = mapActivity.getMyApplication().getTargetPointsHelper();
			targets.removeAllWayPoints(false, true);
			targets.navigateToPoint(to, true, -1, toDesc);
		}
		mapActivity.getMapActions().enterRoutePlanningModeGivenGpx(gpx, from, fromDesc, true, false);
		if (!app.getTargetPointsHelper().checkPointToNavigateShort()) {
			mapActivity.getMapLayers().getMapControlsLayer().getMapRouteInfoMenu().show();
		} else {
			if (app.getSettings().APPLICATION_MODE.get() != routingHelper.getAppMode()) {
				app.getSettings().APPLICATION_MODE.set(routingHelper.getAppMode());
			}
			mapActivity.getMapViewTrackingUtilities().backToLocationImpl();
			app.getSettings().FOLLOW_THE_ROUTE.set(true);
			routingHelper.setFollowingMode(true);
			routingHelper.setRoutePlanningMode(false);
			mapActivity.getMapViewTrackingUtilities().switchToRoutePlanningMode();
			app.getRoutingHelper().notifyIfRouteIsCalculated();
			routingHelper.setCurrentLocation(app.getLocationProvider().getLastKnownLocation(), false);
		}
	}

	//NOTE jsala es un test
	public void testApi(OsmandApplication app, String command) {
		Uri uri = null;
		Intent intent = null;

		String lat = "44.98062";
		String lon = "34.09258";
		String destLat = "44.97799";
		String destLon = "34.10286";
		String gpxName = "xxx.gpx";

		try {

			if (API_CMD_GET_INFO.equals(command)) {
				uri = Uri.parse("osmand.api://get_info");
			}

			if (API_CMD_NAVIGATE.equals(command)) {
				// test navigate
				uri = Uri.parse("osmand.api://navigate" +
						"?start_lat=" + lat + "&start_lon=" + lon + "&start_name=Start" +
						"&dest_lat=" + destLat + "&dest_lon=" + destLon + "&dest_name=Finish" +
						"&profile=bicycle");
			}

			if (API_CMD_ADD_MAP_MARKER.equals(command)) {
				// test marker
				uri = Uri.parse("osmand.api://add_map_marker?lat=" + lat + "&lon=" + lon + "&name=Marker");
			}

			if (API_CMD_ADD_FAVORITE.equals(command)) {
				// test favorite
				uri = Uri.parse("osmand.api://add_favorite?lat=" + lat + "&lon=" + lon + "&name=Favorite&desc=Description&category=test2&color=red&visible=true");
			}

			if (API_CMD_START_GPX_REC.equals(command)) {
				// test start gpx recording
				uri = Uri.parse("osmand.api://start_gpx_rec");
			}

			if (API_CMD_STOP_GPX_REC.equals(command)) {
				// test stop gpx recording
				uri = Uri.parse("osmand.api://stop_gpx_rec");
			}

			if (API_CMD_SHOW_GPX.equals(command)) {
				// test show gpx (path)
				//File gpx = new File(app.getAppPath(IndexConstants.GPX_INDEX_DIR), gpxName);
				//uri = Uri.parse("osmand.api://show_gpx?path=" + URLEncoder.encode(gpx.getAbsolutePath(), "UTF-8"));

				// test show gpx (data)
				uri = Uri.parse("osmand.api://show_gpx");
				intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.putExtra("data", AndroidUtils.getFileAsString(
						new File(app.getAppPath(IndexConstants.GPX_INDEX_DIR), gpxName)));
			}

			if (API_CMD_NAVIGATE_GPX.equals(command)) {
				// test navigate gpx (path)
				//File gpx = new File(app.getAppPath(IndexConstants.GPX_INDEX_DIR), gpxName);
				//uri = Uri.parse("osmand.api://navigate_gpx?force=true&path=" + URLEncoder.encode(gpx.getAbsolutePath(), "UTF-8"));

				// test navigate gpx (data)
				uri = Uri.parse("osmand.api://navigate_gpx?force=true");
				intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.putExtra("data", AndroidUtils.getFileAsString(
						new File(app.getAppPath(IndexConstants.GPX_INDEX_DIR), gpxName)));
			}

			if (intent == null && uri != null) {
				intent = new Intent(Intent.ACTION_VIEW, uri);
			}

			if (intent != null) {
				mapActivity.startActivity(intent);
			}

		} catch (Exception e) {
			LOG.error("Test failed", e);
		}
	}
}
