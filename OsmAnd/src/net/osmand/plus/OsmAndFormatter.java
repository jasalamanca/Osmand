package net.osmand.plus;

import android.content.Context;

import net.osmand.data.Amenity;
import net.osmand.data.City.CityType;
import net.osmand.osm.PoiCategory;
import net.osmand.osm.PoiType;
import net.osmand.plus.OsmandSettings.MetricsConstants;
import net.osmand.plus.OsmandSettings.SpeedConstants;
import net.osmand.util.Algorithms;

import java.text.DecimalFormat;
import java.text.MessageFormat;

public class OsmAndFormatter {
	public final static float METERS_IN_KILOMETER = 1000f;
	public final static float METERS_IN_ONE_MILE = 1609.344f; // 1609.344
	public final static float METERS_IN_ONE_NAUTICALMILE = 1852f; // 1852
	
	public final static float YARDS_IN_ONE_METER = 1.0936f;
	public final static float FEET_IN_ONE_METER = YARDS_IN_ONE_METER * 3f;
	private static final DecimalFormat fixed2 = new DecimalFormat("0.00");
	private static final DecimalFormat fixed1 = new DecimalFormat("0.0");
	static {
		fixed2.setMinimumFractionDigits(2);
		fixed1.setMinimumFractionDigits(1);
		fixed1.setMinimumIntegerDigits(1);
		fixed2.setMinimumIntegerDigits(1);
	}

	public static String getFormattedDuration(int seconds, OsmandApplication ctx) {
		int hours = seconds / (60 * 60);
		int minutes = (seconds / 60) % 60;
		if (hours > 0) {
			return hours + " "
					+ ctx.getString(R.string.osmand_parking_hour)
					+ (minutes > 0 ? " " + minutes + " "
					+ ctx.getString(R.string.osmand_parking_minute) : "");
		} else {
			return minutes + " " + ctx.getString(R.string.osmand_parking_minute);
		}
	}

	public static String getFormattedDurationShort(int seconds) {
		int hours = seconds / (60 * 60);
		int minutes = (seconds / 60) % 60;
		int sec = seconds % 60;
		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (sec < 10 ? "0" + sec : sec);
	}

	public static double calculateRoundedDist(double distInMeters, OsmandApplication ctx) {
		OsmandSettings settings = ctx.getSettings();
		MetricsConstants mc = settings.METRIC_SYSTEM.get();
		double mainUnitInMeter = 1;
		double metersInSecondUnit = METERS_IN_KILOMETER;
		if (mc == MetricsConstants.MILES_AND_FEET) {
			mainUnitInMeter = FEET_IN_ONE_METER;
			metersInSecondUnit = METERS_IN_ONE_MILE;
		} else if (mc == MetricsConstants.MILES_AND_METERS) {
			mainUnitInMeter = 1;
			metersInSecondUnit = METERS_IN_ONE_MILE;
		} else if (mc == MetricsConstants.NAUTICAL_MILES) {
			mainUnitInMeter = 1;
			metersInSecondUnit = METERS_IN_ONE_NAUTICALMILE;
		} else if (mc == MetricsConstants.MILES_AND_YARDS) {
			mainUnitInMeter = YARDS_IN_ONE_METER;
			metersInSecondUnit = METERS_IN_ONE_MILE;
		}

		// 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000 ...
		int generator = 1;
		byte pointer = 1;
		double point = mainUnitInMeter;
		double roundDist = 1;
		while (distInMeters * point > generator) {
			roundDist = (generator / point);
			if (pointer++ % 3 == 2) {
				generator = generator * 5 / 2;
			} else {
				generator *= 2;
			}
			
			if (point == mainUnitInMeter && metersInSecondUnit * mainUnitInMeter * 0.9f <= generator) {
				point = 1 / metersInSecondUnit;
				generator = 1;
				pointer = 1;
			}
		}
		//Miles exceptions: 2000ft->0.5mi, 1000ft->0.25mi, 1000yd->0.5mi, 500yd->0.25mi, 1000m ->0.5mi, 500m -> 0.25mi
		if (mc == MetricsConstants.MILES_AND_METERS && roundDist == 1000) {
			roundDist = 0.5f * METERS_IN_ONE_MILE;
		} else if (mc == MetricsConstants.MILES_AND_METERS && roundDist == 500) {
			roundDist = 0.25f * METERS_IN_ONE_MILE;
		} else if (mc == MetricsConstants.MILES_AND_FEET && roundDist == 2000 / (double) FEET_IN_ONE_METER) {
			roundDist = 0.5f * METERS_IN_ONE_MILE;
		} else if (mc == MetricsConstants.MILES_AND_FEET && roundDist == 1000 / (double) FEET_IN_ONE_METER) {
			roundDist = 0.25f * METERS_IN_ONE_MILE;
		} else if (mc == MetricsConstants.MILES_AND_YARDS && roundDist == 1000 / (double) YARDS_IN_ONE_METER) {
			roundDist = 0.5f * METERS_IN_ONE_MILE;
		} else if (mc == MetricsConstants.MILES_AND_YARDS && roundDist == 500 / (double) YARDS_IN_ONE_METER) {
			roundDist = 0.25f * METERS_IN_ONE_MILE;
		}
		return roundDist;
	}
	
	static String getFormattedRoundDistanceKm(float meters, int digits, OsmandApplication ctx) {
		int mainUnitStr = R.string.km;
		float mainUnitInMeters = METERS_IN_KILOMETER;
		if (digits == 0) {
			return (int) (meters / mainUnitInMeters + 0.5) + " " + ctx.getString(mainUnitStr); //$NON-NLS-1$
		} else if (digits == 1) {
			return fixed1.format(meters / mainUnitInMeters) + " " + ctx.getString(mainUnitStr);
		} else {
			return fixed2.format(meters / mainUnitInMeters) + " " + ctx.getString(mainUnitStr);
		}
	}

	public static String getFormattedAlarmInfoDistance(OsmandApplication app, float meters) {
		boolean kmAndMeters = app.getSettings().METRIC_SYSTEM.get() == MetricsConstants.KILOMETERS_AND_METERS;
		int mainUnitStr = kmAndMeters ? R.string.km : R.string.mile;
		float mainUnitInMeters = kmAndMeters ? METERS_IN_KILOMETER : METERS_IN_ONE_MILE;
		DecimalFormat df = new DecimalFormat("#.#");

		return df.format(meters / mainUnitInMeters) + " " + app.getString(mainUnitStr);
	}
	
	public static String getFormattedDistance(float meters, OsmandApplication ctx) {
		return getFormattedDistance(meters, ctx, true);
	}

	public static String getFormattedDistance(float meters, OsmandApplication ctx, boolean forceTrailingZeros) {
		String format1 = forceTrailingZeros ? "{0,number,0.0} " : "{0,number,0.#} ";
		String format2 = forceTrailingZeros ? "{0,number,0.00} " : "{0,number,0.##} ";

		OsmandSettings settings = ctx.getSettings();
		MetricsConstants mc = settings.METRIC_SYSTEM.get();
		int mainUnitStr;
		float mainUnitInMeters;
		if (mc == MetricsConstants.KILOMETERS_AND_METERS) {
			mainUnitStr = R.string.km;
			mainUnitInMeters = METERS_IN_KILOMETER;
		} else if (mc == MetricsConstants.NAUTICAL_MILES) {
			mainUnitStr = R.string.nm;
			mainUnitInMeters = METERS_IN_ONE_NAUTICALMILE;
		} else {
			mainUnitStr = R.string.mile;
			mainUnitInMeters = METERS_IN_ONE_MILE;
		}

		if (meters >= 100 * mainUnitInMeters) {
			return (int) (meters / mainUnitInMeters + 0.5) + " " + ctx.getString(mainUnitStr); //$NON-NLS-1$
		} else if (meters > 9.99f * mainUnitInMeters) {
			return MessageFormat.format(format1 + ctx.getString(mainUnitStr), meters / mainUnitInMeters).replace('\n', ' '); //$NON-NLS-1$
		} else if (meters > 0.999f * mainUnitInMeters) {
			return MessageFormat.format(format2 + ctx.getString(mainUnitStr), meters / mainUnitInMeters).replace('\n', ' '); //$NON-NLS-1$
		} else if (mc == MetricsConstants.MILES_AND_FEET && meters > 0.249f * mainUnitInMeters) {
			return MessageFormat.format(format2 + ctx.getString(mainUnitStr), meters / mainUnitInMeters).replace('\n', ' '); //$NON-NLS-1$
		} else if (mc == MetricsConstants.MILES_AND_METERS && meters > 0.249f * mainUnitInMeters) {
			return MessageFormat.format(format2 + ctx.getString(mainUnitStr), meters / mainUnitInMeters).replace('\n', ' '); //$NON-NLS-1$
		} else if (mc == MetricsConstants.MILES_AND_YARDS && meters > 0.249f * mainUnitInMeters) {
			return MessageFormat.format(format2 + ctx.getString(mainUnitStr), meters / mainUnitInMeters).replace('\n', ' '); //$NON-NLS-1$
		} else if (mc == MetricsConstants.NAUTICAL_MILES && meters > 0.99f * mainUnitInMeters) {
			return MessageFormat.format(format2 + ctx.getString(mainUnitStr), meters / mainUnitInMeters).replace('\n', ' '); //$NON-NLS-1$
		} else {
			if (mc == MetricsConstants.KILOMETERS_AND_METERS || mc == MetricsConstants.MILES_AND_METERS) {
				return ((int) (meters + 0.5)) + " " + ctx.getString(R.string.m); //$NON-NLS-1$
			} else if (mc == MetricsConstants.MILES_AND_FEET) {
				int feet = (int) (meters * FEET_IN_ONE_METER + 0.5);
				return feet + " " + ctx.getString(R.string.foot); //$NON-NLS-1$
			} else if (mc == MetricsConstants.MILES_AND_YARDS) {
				int yards = (int) (meters * YARDS_IN_ONE_METER + 0.5);
				return yards + " " + ctx.getString(R.string.yard); //$NON-NLS-1$
			}
			return ((int) (meters + 0.5)) + " " + ctx.getString(R.string.m); //$NON-NLS-1$
		}
	}

	public static String getFormattedAlt(double alt, OsmandApplication ctx) {
		OsmandSettings settings = ctx.getSettings();
		MetricsConstants mc = settings.METRIC_SYSTEM.get();
		boolean useFeet = (mc == MetricsConstants.MILES_AND_FEET) || (mc == MetricsConstants.MILES_AND_YARDS);
		if (!useFeet) {
			return ((int) (alt + 0.5)) + " " + ctx.getString(R.string.m);
		} else {
			return ((int) (alt * FEET_IN_ONE_METER + 0.5)) + " " + ctx.getString(R.string.foot);
		}
	}
	
	public static String getFormattedSpeed(float metersperseconds, OsmandApplication ctx) {
		OsmandSettings settings = ctx.getSettings();
		SpeedConstants mc = settings.SPEED_SYSTEM.get();
		ApplicationMode am = settings.getApplicationMode();
		float kmh = metersperseconds * 3.6f;
		if (mc == SpeedConstants.KILOMETERS_PER_HOUR) {
			// e.g. car case and for high-speeds: Display rounded to 1 km/h (5% precision at 20 km/h)
			if (kmh >= 20 || am.hasFastSpeed()) {
				return Math.round(kmh) + " " + mc.toShortString(ctx);
			}
			// for smaller values display 1 decimal digit x.y km/h, (0.5% precision at 20 km/h)
			int kmh10 = Math.round(kmh * 10f);
			return (kmh10 / 10f) + " " + mc.toShortString(ctx);
		} else if (mc == SpeedConstants.MILES_PER_HOUR) {
			float mph = kmh * METERS_IN_KILOMETER / METERS_IN_ONE_MILE;
			if (mph >= 20 || am.hasFastSpeed()) {
				return Math.round(mph) + " " + mc.toShortString(ctx);
			} else {
				int mph10 = Math.round(mph * 10f);
				return (mph10 / 10f) + " " + mc.toShortString(ctx);
			}
		} else if (mc == SpeedConstants.NAUTICALMILES_PER_HOUR) {
			float mph = kmh * METERS_IN_KILOMETER / METERS_IN_ONE_NAUTICALMILE;
			if (mph >= 20 || am.hasFastSpeed()) {
				return Math.round(mph) + " " + mc.toShortString(ctx);
			} else {
				int mph10 = Math.round(mph * 10f);
				return (mph10 / 10f) + " " + mc.toShortString(ctx);
			}
		} else if (mc == SpeedConstants.MINUTES_PER_KILOMETER) {
			if (metersperseconds < 0.111111111) {
				return "-" + mc.toShortString(ctx);
			}
			float minperkm = METERS_IN_KILOMETER / (metersperseconds * 60);
			if (minperkm >= 10) {
				return Math.round(minperkm) + " " + mc.toShortString(ctx);
			} else {
				int mph10 = Math.round(minperkm * 10f);
				return (mph10 / 10f) + " " + mc.toShortString(ctx);
			}
		} else if (mc == SpeedConstants.MINUTES_PER_MILE) {
			if (metersperseconds < 0.111111111) {
				return "-" + mc.toShortString(ctx);
			}
			float minperm = (METERS_IN_ONE_MILE) / (metersperseconds * 60);
			if (minperm >= 10) {
				return Math.round(minperm) + " " + mc.toShortString(ctx);
			} else {
				int mph10 = Math.round(minperm * 10f);
				return (mph10 / 10f) + " " + mc.toShortString(ctx);
			}
		} else /*if (mc == SpeedConstants.METERS_PER_SECOND) */ {
			if (metersperseconds >= 10) {
				return Math.round(metersperseconds) + " " + SpeedConstants.METERS_PER_SECOND.toShortString(ctx);
			}
			// for smaller values display 1 decimal digit x.y km/h, (0.5% precision at 20 km/h)
			int kmh10 = Math.round(metersperseconds * 10f);
			return (kmh10 / 10f) + " " + SpeedConstants.METERS_PER_SECOND.toShortString(ctx);
		}
	}

	public static String toPublicString(CityType t, Context ctx) {
		switch (t) {
		case CITY:
			return ctx.getString(R.string.city_type_city);
		case HAMLET:
			return ctx.getString(R.string.city_type_hamlet);
		case TOWN:
			return ctx.getString(R.string.city_type_town);
		case VILLAGE:
			return ctx.getString(R.string.city_type_village);
		case SUBURB:
			return ctx.getString(R.string.city_type_suburb);
		default:
			break;
		}
		return "";
	}

	public static String getPoiStringWithoutType(Amenity amenity, String locale, boolean transliterate) {
		PoiCategory pc = amenity.getType();
		PoiType pt = pc.getPoiTypeByKeyName(amenity.getSubType());
		String nm = amenity.getSubType();
		if (pt != null) {
			nm = pt.getTranslation();
		} else if(nm != null){
			nm = Algorithms.capitalizeFirstLetterAndLowercase(nm.replace('_', ' '));
		}
		String n = amenity.getName(locale, transliterate);
		if (n.contains(nm)) {
			// type is contained in name e.g.
			// n = "Bakery the Corner"
			// type = "Bakery"
			// no need to repeat this
			return n;
		}
		if (n.length() == 0) {
			return nm;
		}
		return nm + " " + n; //$NON-NLS-1$
	}
}