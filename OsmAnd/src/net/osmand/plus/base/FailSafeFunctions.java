package net.osmand.plus.base;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.app.AlertDialog;
import android.widget.TextView;

import net.osmand.PlatformUtil;
import net.osmand.data.LatLon;
import net.osmand.plus.GPXUtilities;
import net.osmand.plus.GPXUtilities.GPXFile;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.TargetPointsHelper.TargetPoint;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.routing.RouteProvider.GPXRouteParamsBuilder;
import net.osmand.plus.routing.RoutingHelper;

import org.apache.commons.logging.Log;

import java.io.File;
import java.util.ArrayList;

public class FailSafeFunctions {
	private static boolean quitRouteRestoreDialog = false;
	private static final Log log = PlatformUtil.getLog(FailSafeFunctions.class);
	
	public static void restoreRoutingMode(final MapActivity ma) {
		final OsmandApplication app = ma.getMyApplication();
		final OsmandSettings settings = app.getSettings();
		final Handler uiHandler = new Handler();
		final String gpxPath = settings.FOLLOW_THE_GPX_ROUTE.get();
		final TargetPointsHelper targetPoints = app.getTargetPointsHelper();
		final TargetPoint pointToNavigate = targetPoints.getPointToNavigate();
		if (pointToNavigate == null && gpxPath == null) {
			notRestoreRoutingMode(ma, app);
		} else {
			quitRouteRestoreDialog = false;
			Runnable encapsulate = new Runnable() {
				int delay = 7;
				Runnable delayDisplay = null;

				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(ma);
					final TextView tv = new TextView(ma);
					tv.setText(ma.getString(R.string.continue_follow_previous_route_auto, delay + ""));
					tv.setPadding(7, 5, 7, 5);
					builder.setView(tv);
					builder.setPositiveButton(R.string.shared_string_yes, (dialog, which) -> {
                        quitRouteRestoreDialog = true;
                        restoreRoutingModeInner();

                    });
					builder.setNegativeButton(R.string.shared_string_no, (dialog, which) -> {
                        quitRouteRestoreDialog = true;
                        notRestoreRoutingMode(ma, app);
                    });
					final AlertDialog dlg = builder.show();
					dlg.setOnDismissListener(dialog -> quitRouteRestoreDialog = true);
					dlg.setOnCancelListener(dialog -> quitRouteRestoreDialog = true);
					delayDisplay = () -> {
                        if(!quitRouteRestoreDialog) {
                            delay --;
                            tv.setText(ma.getString(R.string.continue_follow_previous_route_auto, delay + ""));
                            if(delay <= 0) {
                                try {
                                    if (dlg.isShowing() && !quitRouteRestoreDialog) {
                                        dlg.dismiss();
                                    }
                                    quitRouteRestoreDialog = true;
                                    restoreRoutingModeInner();
                                } catch(Exception e) {
                                    // swallow view not attached exception
                                    log.error(e.getMessage()+"", e);
                                }
                            } else {
                                uiHandler.postDelayed(delayDisplay, 1000);
                            }
                        }
                    };
					delayDisplay.run();
				}

				private void restoreRoutingModeInner() {
					AsyncTask<String, Void, GPXFile> task = new AsyncTask<String, Void, GPXFile>() {
						@Override
						protected GPXFile doInBackground(String... params) {
							if (gpxPath != null) {
								// Reverse also should be stored ?
								GPXFile f = GPXUtilities.loadGPXFile(app, new File(gpxPath));
								if (f.warning != null) {
									return null;
								}
								return f;
							} else {
								return null;
							}
						}

						@Override
						protected void onPostExecute(GPXFile result) {
							final GPXRouteParamsBuilder gpxRoute;
							if (result != null) {
								gpxRoute = new GPXRouteParamsBuilder(result, settings);
								if (settings.GPX_ROUTE_CALC_OSMAND_PARTS.get()) {
									gpxRoute.setCalculateOsmAndRouteParts(true);
								}
								if (settings.GPX_CALCULATE_RTEPT.get()) {
									gpxRoute.setUseIntermediatePointsRTE(true);
								}
								if(settings.GPX_ROUTE_CALC.get()) {
									gpxRoute.setCalculateOsmAndRoute(true);
								}
							} else {
								gpxRoute = null;
							}
							if (pointToNavigate == null) {
								notRestoreRoutingMode(ma, app);
							} else {
								enterRoutingMode(ma, gpxRoute);
							}
						}

						
					};
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gpxPath);

				}
			};
			encapsulate.run();
		}

	}
	
	private static void enterRoutingMode(MapActivity ma,
										 GPXRouteParamsBuilder gpxRoute) {
		OsmandApplication app = ma.getMyApplication();
		ma.getMapViewTrackingUtilities().backToLocationImpl();
		RoutingHelper routingHelper = app.getRoutingHelper();
		if(gpxRoute == null) {
			app.getSettings().FOLLOW_THE_GPX_ROUTE.set(null);
		}
		routingHelper.setGpxParams(gpxRoute);
		if (app.getTargetPointsHelper().getPointToStart() == null) {
			app.getTargetPointsHelper().setStartPoint(null, false, null);
		}
		app.getSettings().FOLLOW_THE_ROUTE.set(true);
		routingHelper.setFollowingMode(true);
		app.getTargetPointsHelper().updateRouteAndRefresh(true);
		app.initVoiceCommandPlayer(ma, routingHelper.getAppMode(), true, null, false, false);
		if(ma.getDashboard().isVisible()) {
			ma.getDashboard().hideDashboard();
		}
	}
	
	private static void notRestoreRoutingMode(MapActivity ma, OsmandApplication app){
		ma.updateApplicationModeSettings();
		app.getRoutingHelper().clearCurrentRoute(null, new ArrayList<LatLon>());
		if (app.getSettings().USE_MAP_MARKERS.get()) {
			app.getTargetPointsHelper().removeAllWayPoints(false, false);
		}
		ma.refreshMap();
	}

	public static void quitRouteRestoreDialog() {
		quitRouteRestoreDialog = true;
	}
}
