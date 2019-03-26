package net.osmand.plus;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import net.osmand.IProgress;
import net.osmand.Location;
import net.osmand.PlatformUtil;
import net.osmand.access.AccessibilityPlugin;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.TabActivity.TabItem;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.development.OsmandDevelopmentPlugin;
import net.osmand.plus.mapcontextmenu.MenuBuilder;
import net.osmand.plus.mapcontextmenu.MenuController;
import net.osmand.plus.monitoring.OsmandMonitoringPlugin;
import net.osmand.plus.myplaces.FavoritesActivity;
import net.osmand.plus.osmedit.OsmEditingPlugin;
import net.osmand.plus.views.OsmandMapTileView;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class OsmandPlugin {
	private static final List<OsmandPlugin> allPlugins = new ArrayList<>();
	private static final Log LOG = PlatformUtil.getLog(OsmandPlugin.class);

	private boolean active;
	private final String installURL = null;

	public abstract String getId();
	public abstract String getDescription();
	public abstract String getName();
	public abstract int getAssetResourceName();
	@DrawableRes
	public int getLogoResourceId() {
		return R.drawable.ic_extension_dark;
	}
	public abstract Class<? extends Activity> getSettingsActivity();

	/**
	 * Initialize plugin runs just after creation
	 */
	protected boolean init(OsmandApplication app, Activity activity) {
		return true;
	}
	private void setActive(boolean active) {
		this.active = active;
	}
	public boolean isActive() {
		return active;
	}
	private boolean isVisible() {
		return true;
	}
	public boolean needsInstallation() {
		return installURL != null;
	}
	public String getInstallURL() {
		return installURL;
	}
	public void disable(OsmandApplication app) {
	}
	public String getHelpFileName() {
		return null;
	}

	/*
	 * Return true in case if plugin should fill the map context menu with buildContextMenuRows method.
	 */
	public boolean isMenuControllerSupported(Class<? extends MenuController> menuControllerClass) {
		return false;
	}

	/*
	 * Add menu rows to the map context menu.
	 */
	public void buildContextMenuRows(@NonNull MenuBuilder menuBuilder, @NonNull View view) {
	}

	/*
	 * Clear resources after menu was closed
	 */
	public void clearContextMenuRows() {
	}

	static void initPlugins(OsmandApplication app) {
		OsmandSettings settings = app.getSettings();
		Set<String> enabledPlugins = settings.getEnabledPlugins();

		// plugins with additional actions for context menu in right order:
		allPlugins.add(new OsmEditingPlugin(app));
		allPlugins.add(new OsmandMonitoringPlugin(app));
		allPlugins.add(new AccessibilityPlugin(app));
		allPlugins.add(new OsmandDevelopmentPlugin(app));

		activatePlugins(app, enabledPlugins);
	}

	private static void activatePlugins(OsmandApplication app, Set<String> enabledPlugins) {
		for (OsmandPlugin plugin : allPlugins) {
			if (enabledPlugins.contains(plugin.getId()) || plugin.isActive()) {
				try {
					if (plugin.init(app, null)) {
						plugin.setActive(true);
					}
				} catch (Exception e) {
					LOG.error("Plugin initialization failed " + plugin.getId(), e);
				}
			}
		}
	}

	public static boolean enablePlugin(Activity activity, OsmandApplication app, OsmandPlugin plugin, boolean enable) {
		if (enable) {
			if (!plugin.init(app, activity)) {
				plugin.setActive(false);
				return false;
			} else {
				plugin.setActive(true);
			}
		} else {
			plugin.disable(app);
			plugin.setActive(false);
		}
		app.getSettings().enablePlugin(plugin.getId(), enable);
		if (activity instanceof MapActivity) {
			final MapActivity mapActivity = (MapActivity) activity;
			plugin.updateLayers(mapActivity.getMapView(), mapActivity);
			mapActivity.getDashboard().refreshDashboardFragments();
			if (!enable && plugin.getCardFragment() != null) {
				Fragment fragment = mapActivity.getSupportFragmentManager()
						.findFragmentByTag(plugin.getCardFragment().tag);
				LOG.debug("fragment=" + fragment);
				mapActivity.getSupportFragmentManager().beginTransaction()
						.remove(fragment).commit();
			}
		}
		return true;
	}

	protected void updateLayers(OsmandMapTileView mapView, MapActivity activity) {
	}

    /**
	 * Register layers calls when activity is created and before @mapActivityCreate
	 *
	 * @param activity
	 */
	protected void registerLayers(MapActivity activity) {
	}

	private void mapActivityCreate(MapActivity activity) {
	}

	private void mapActivityResume(MapActivity activity) {
	}

	private void mapActivityPause(MapActivity activity) {
	}

	private void mapActivityDestroy(MapActivity activity) {
	}

	private void mapActivityScreenOff(MapActivity activity) {
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void handleRequestPermissionsResult(int requestCode, String[] permissions,
												int[] grantResults) {
	}

	public static void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                  int[] grantResults) {
		for (OsmandPlugin plugin : getAvailablePlugins()) {
			plugin.handleRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	private boolean destinationReached() {
		return true;
	}
	protected void registerLayerContextMenuActions(OsmandMapTileView mapView, ContextMenuAdapter adapter, MapActivity mapActivity) {
	}
	protected void registerMapContextMenuActions(MapActivity mapActivity, double latitude, double longitude, ContextMenuAdapter adapter, Object selectedObj) {
	}
	protected void registerOptionsMenuItems(MapActivity mapActivity, ContextMenuAdapter helper) {
	}
	protected DashFragmentData getCardFragment() {
		return null;
	}
	protected void updateLocation(Location location) {
	}
	protected void addMyPlacesTab(FavoritesActivity favoritesActivity, List<TabItem> mTabs, Intent intent) {
	}
	protected void contextMenuFragment(Activity activity, Fragment fragment, Object info, ContextMenuAdapter adapter) {
	}
	protected void optionsMenuFragment(Activity activity, Fragment fragment, ContextMenuAdapter optionsMenuAdapter) {
	}
	private List<String> indexingFiles(IProgress progress) {
		return null;
	}
	private boolean mapActivityKeyUp(MapActivity mapActivity, int keyCode) {
		return false;
	}
	private void onMapActivityExternalResult(int requestCode, int resultCode, Intent data) {
	}

	public static void refreshLayers(OsmandMapTileView mapView, MapActivity activity) {
		for (OsmandPlugin plugin : getAvailablePlugins()) {
			plugin.updateLayers(mapView, activity);
		}
	}

	public static List<OsmandPlugin> getAvailablePlugins() {
		return allPlugins;
	}

	public static List<OsmandPlugin> getVisiblePlugins() {
		List<OsmandPlugin> list = new ArrayList<>(allPlugins.size());
		for (OsmandPlugin p : allPlugins) {
			if (p.isVisible()) {
				list.add(p);
			}
		}
		return list;
	}

	public static List<OsmandPlugin> getEnabledPlugins() {
		ArrayList<OsmandPlugin> lst = new ArrayList<>(allPlugins.size());
		for (OsmandPlugin p : allPlugins) {
			if (p.isActive()) {
				lst.add(p);
			}
		}
		return lst;
	}

	public static List<OsmandPlugin> getEnabledVisiblePlugins() {
		ArrayList<OsmandPlugin> lst = new ArrayList<>(allPlugins.size());
		for (OsmandPlugin p : allPlugins) {
			if (p.isActive() && p.isVisible()) {
				lst.add(p);
			}
		}
		return lst;
	}

	public static List<OsmandPlugin> getNotEnabledVisiblePlugins() {
		ArrayList<OsmandPlugin> lst = new ArrayList<>(allPlugins.size());
		for (OsmandPlugin p : allPlugins) {
			if (!p.isActive() && p.isVisible()) {
				lst.add(p);
			}
		}
		return lst;
	}

	public static <T extends OsmandPlugin> T getEnabledPlugin(Class<T> clz) {
		for (OsmandPlugin lr : getEnabledPlugins()) {
			if (clz.isInstance(lr)) {
				return (T) lr;
			}
		}
		return null;
	}

	public static <T extends OsmandPlugin> T getPlugin(Class<T> clz) {
		for (OsmandPlugin lr : getAvailablePlugins()) {
			if (clz.isInstance(lr)) {
				return (T) lr;
			}
		}
		return null;
	}

	public static List<String> onIndexingFiles(IProgress progress) {
		List<String> l = new ArrayList<>();
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			List<String> ls = plugin.indexingFiles(progress);
			if (ls != null && ls.size() > 0) {
				l.addAll(ls);
			}
		}
		return l;
	}

	public static void onMapActivityCreate(MapActivity activity) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.mapActivityCreate(activity);
		}
	}

	public static void onMapActivityResume(MapActivity activity) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.mapActivityResume(activity);
		}
	}

	public static void onMapActivityPause(MapActivity activity) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.mapActivityPause(activity);
		}
	}

	public static void onMapActivityDestroy(MapActivity activity) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.mapActivityDestroy(activity);
		}
	}

	public static void onMapActivityResult(int requestCode, int resultCode, Intent data) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.onMapActivityExternalResult(requestCode, resultCode, data);
		}
	}

	public static void onMapActivityScreenOff(MapActivity activity) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.mapActivityScreenOff(activity);
		}
	}

	public static boolean onDestinationReached() {
		boolean b = true;
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			if (!plugin.destinationReached()) {
				b = false;
			}
		}
		return b;
	}

	public static void createLayers(MapActivity activity) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.registerLayers(activity);
		}
	}

	public static void registerMapContextMenu(MapActivity map, double latitude, double longitude, ContextMenuAdapter adapter, Object selectedObj) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.registerMapContextMenuActions(map, latitude, longitude, adapter, selectedObj);
		}
	}

	public static void registerLayerContextMenu(OsmandMapTileView mapView, ContextMenuAdapter adapter, MapActivity mapActivity) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.registerLayerContextMenuActions(mapView, adapter, mapActivity);
		}
	}

	public static void registerOptionsMenu(MapActivity map, ContextMenuAdapter helper) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.registerOptionsMenuItems(map, helper);
		}
	}

	public static void onContextMenuActivity(Activity activity, Fragment fragment, Object info, ContextMenuAdapter adapter) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.contextMenuFragment(activity, fragment, info, adapter);
		}
	}

	public static void onOptionsMenuActivity(Activity activity, Fragment fragment, ContextMenuAdapter optionsMenuAdapter) {
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			plugin.optionsMenuFragment(activity, fragment, optionsMenuAdapter);
		}
	}

	public static Collection<DashFragmentData> getPluginsCardsList() {
		HashSet<DashFragmentData> collection = new HashSet<>();
		for (OsmandPlugin plugin : getEnabledPlugins()) {
			final DashFragmentData fragmentData = plugin.getCardFragment();
			if (fragmentData != null) collection.add(fragmentData);
		}
		return collection;
	}

	public static boolean onMapActivityKeyUp(MapActivity mapActivity, int keyCode) {
		for (OsmandPlugin p : getEnabledPlugins()) {
			if (p.mapActivityKeyUp(mapActivity, keyCode))
				return true;
		}
		return false;
	}

	static void updateLocationPlugins(net.osmand.Location location) {
		for (OsmandPlugin p : getEnabledPlugins()) {
			p.updateLocation(location);
		}
	}

	public static boolean isDevelopment() {
		return getEnabledPlugin(OsmandDevelopmentPlugin.class) != null;
	}

	public static void addMyPlacesTabPlugins(FavoritesActivity favoritesActivity, List<TabItem> mTabs, Intent intent) {
		for (OsmandPlugin p : getEnabledPlugins()) {
			p.addMyPlacesTab(favoritesActivity, mTabs, intent);
		}
	}
}