package net.osmand.plus;


import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Version {
	
	private final String appVersion; 
	private final String appName;
	private final static String FREE_VERSION_NAME = "net.osmand";
	private final static String FREE_DEV_VERSION_NAME = "net.osmand.dev";
	private final static String UTM_REF = "&referrer=utm_source%3Dosmand";

	public static boolean isBlackberry(OsmandApplication ctx) {
		return ctx.getString(R.string.versionFeatures).contains("+blackberry");
	}
	
	public static boolean isMarketEnabled(OsmandApplication ctx) {
		return isGooglePlayEnabled(ctx) || isAmazonEnabled(ctx);
	}

	private static boolean isGooglePlayInstalled(OsmandApplication ctx) {
		try {
			ctx.getPackageManager().getPackageInfo("com.android.vending", 0);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
		return true;
	}
	
	private static String marketPrefix(OsmandApplication ctx) {
		if (isAmazonEnabled(ctx)) {
			return "amzn://apps/android?p=";
		} else if (isGooglePlayEnabled(ctx) && isGooglePlayInstalled(ctx)) {
			return "market://details?id=";
		} 
		return "https://osmand.net/apps?id=";
	}

	public static String getUrlWithUtmRef(OsmandApplication ctx, String appName) {
		return marketPrefix(ctx) + appName + UTM_REF;
	}
	
	private static boolean isAmazonEnabled(OsmandApplication ctx) {
		return ctx.getString(R.string.versionFeatures).contains("+amazon");
	}
	
	public static boolean isGooglePlayEnabled(OsmandApplication ctx) {
		return ctx.getString(R.string.versionFeatures).contains("+play_market");
	}

	private Version(OsmandApplication ctx) {
		String appVersion = "";
		try {
			PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			appVersion = packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		this.appVersion = appVersion;
		appName = ctx.getString(R.string.app_name);
	}

	private static Version ver = null;
	private static Version getVersion(OsmandApplication ctx){
		if (ver == null) {
			ver = new Version(ctx);
		}
		return ver;
	}
	
	public static String getFullVersion(OsmandApplication ctx){
		Version v = getVersion(ctx);
		return v.appName + " " + v.appVersion;
	}
	
	public static String getAppVersion(OsmandApplication ctx){
		Version v = getVersion(ctx);
		return v.appVersion;
	}

	public static String getAppName(OsmandApplication ctx){
		Version v = getVersion(ctx);
		return v.appName;
	}
	
	private static boolean isProductionVersion(OsmandApplication ctx){
		Version v = getVersion(ctx);
		return !v.appVersion.contains("#");
	}

	public static String getVersionAsURLParam(OsmandApplication ctx) {
		try {
			return "osmandver=" + URLEncoder.encode(getVersionForTracker(ctx), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static boolean isFreeVersion(OsmandApplication ctx){
		return ctx.getPackageName().equals(FREE_VERSION_NAME) || ctx.getPackageName().equals(FREE_DEV_VERSION_NAME);
	}

	public static boolean isPaidVersion(OsmandApplication ctx) {
		return !isFreeVersion(ctx)
				|| ctx.getSettings().FULL_VERSION_PURCHASED.get()
				|| ctx.getSettings().LIVE_UPDATES_PURCHASED.get();
	}
	
	public static boolean isDeveloperVersion(OsmandApplication ctx){
		return getAppName(ctx).contains("~") || ctx.getPackageName().equals(FREE_DEV_VERSION_NAME);
	}
	
	private static String getVersionForTracker(OsmandApplication ctx) {
		String v = Version.getAppName(ctx);
		if(Version.isProductionVersion(ctx)){
			v = Version.getFullVersion(ctx);
		} else {
			v +=" test";
		}
		return v;
	}
}