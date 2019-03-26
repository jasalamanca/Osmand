package net.osmand.plus;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.osmand.CallbackWithObject;
import net.osmand.Location;
import net.osmand.plus.helpers.GpxUiHelper;
import net.osmand.plus.routing.RouteProvider.GPXRouteParamsBuilder;

import java.util.ArrayList;
import java.util.List;

public class OsmAndLocationSimulation {

	private Thread routeAnimation;
	private final OsmAndLocationProvider provider;
	private final OsmandApplication app;
	
	public OsmAndLocationSimulation(OsmandApplication app, OsmAndLocationProvider provider){
		this.app = app;
		this.provider = provider;
	}

	public boolean isRouteAnimating() {
		return routeAnimation != null;
	}
	
	public void startStopRouteAnimation(final Activity ma, final Runnable runnable) {
		if (!isRouteAnimating()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(ma);
			builder.setTitle(R.string.animate_route);

			final View view = ma.getLayoutInflater().inflate(R.layout.animate_route, null);
			final View gpxView = view.findViewById(R.id.layout_animate_gpx);
			final RadioButton radioGPX = view.findViewById(R.id.radio_gpx);
			radioGPX.setOnCheckedChangeListener((buttonView, isChecked) -> gpxView.setVisibility(isChecked ? View.VISIBLE : View.GONE));

			((TextView) view.findViewById(R.id.MinSpeedup)).setText("1"); //$NON-NLS-1$
			((TextView) view.findViewById(R.id.MaxSpeedup)).setText("4"); //$NON-NLS-1$
			final SeekBar speedup = view.findViewById(R.id.Speedup);
			speedup.setMax(3);
			builder.setView(view);
			builder.setPositiveButton(R.string.shared_string_ok, (dialog, which) -> {
				boolean gpxNavigation = radioGPX.isChecked();
				if (gpxNavigation) {
					GpxUiHelper.selectGPXFile(ma, false, false, result -> {
						GPXRouteParamsBuilder builder1 = new GPXRouteParamsBuilder(result[0], app.getSettings());
						startAnimationThread(app, builder1.getPoints(), true, speedup.getProgress() + 1);
						if (runnable != null) {
							runnable.run();
						}
						return true;
					});
				} else {
					List<Location> currentRoute = app.getRoutingHelper().getCurrentCalculatedRoute();
					if (currentRoute.isEmpty()) {
						Toast.makeText(app, R.string.animate_routing_route_not_calculated,
								Toast.LENGTH_LONG).show();
					} else {
						startAnimationThread(app, new ArrayList<>(currentRoute), false, 1);
						if (runnable != null) {
							runnable.run();
						}
					}
				}

			});
			builder.setNegativeButton(R.string.shared_string_cancel, null);
			builder.show();
		} else {
			stop();
		}
	}
	
	public void startStopRouteAnimation(final Activity ma)  {
		startStopRouteAnimation(ma, null);
	}

	private void startAnimationThread(final OsmandApplication app, final List<Location> directions, final boolean useLocationTime, final float coeff) {
		final float time = 1.5f;
		routeAnimation = new Thread() {
			@Override
			public void run() {
				Location current = directions.isEmpty() ? null : new Location(directions.remove(0));
				
				Location prev = current;
				long prevTime = current == null ? 0 : current.getTime();
				float meters = metersToGoInFiveSteps(directions, current);
				if(current != null) {
					current.setProvider(OsmAndLocationProvider.SIMULATED_PROVIDER);
				}
				while (!directions.isEmpty() && routeAnimation != null) {
					int timeout = (int) (time  * 1000);
					float intervalTime = time;
					if(useLocationTime) {
						current = directions.remove(0);
						meters = current.distanceTo(prev);
						if (!directions.isEmpty()) {
							timeout = (int) (directions.get(0).getTime() - current.getTime());
							intervalTime = (current.getTime() - prevTime)  / 1000f;
							prevTime = current.getTime();
						}
						
					} else {
						if (current.distanceTo(directions.get(0)) > meters) {
							current = middleLocation(current, directions.get(0), meters);
						} else {
							current = new Location(directions.remove(0));
							meters = metersToGoInFiveSteps(directions, current);
						}
					}
					if(intervalTime != 0) {
						current.setSpeed(meters / intervalTime * coeff);	
					}
					current.setTime(System.currentTimeMillis());
					if(!current.hasAccuracy() || Double.isNaN(current.getAccuracy())) {
						current.setAccuracy(5);
					}
					if (prev != null && prev.distanceTo(current) > 3) {
						current.setBearing(prev.bearingTo(current));
					}
					final Location toset = current;
					app.runInUIThread(() -> provider.setLocationFromSimulation(toset));
					try {
						Thread.sleep((long)(timeout / coeff));
					} catch (InterruptedException e) {
						// do nothing
					}
					prev = current;
				}
				OsmAndLocationSimulation.this.stop();
			}

		};
		routeAnimation.start();
	}
	
	private float metersToGoInFiveSteps(
			final List<Location> directions, Location current) {
		return directions.isEmpty() ? 20.0f : Math.max(20.0f, current.distanceTo(directions.get(0)) / 2 );
	}

	public void stop() {
		routeAnimation = null;
	}

	private static Location middleLocation(Location start, Location end,
										   float meters) {
		double lat1 = toRad(start.getLatitude());
		double lon1 = toRad(start.getLongitude());
		double R = 6371; // radius of earth in km
		double d = meters / 1000; // in km
		float brng = (float) (toRad(start.bearingTo(end)));
		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R)
				+ Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));
		double lon2 = lon1
				+ Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
						Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));
		Location nl = new Location(start);
		nl.setLatitude(toDegree(lat2));
		nl.setLongitude(toDegree(lon2));
		nl.setBearing(brng);
		return nl;
	}

	private static double toDegree(double radians) {
		return radians * 180 / Math.PI;
	}
	private static double toRad(double degree) {
		return degree * Math.PI / 180;
	}
}
