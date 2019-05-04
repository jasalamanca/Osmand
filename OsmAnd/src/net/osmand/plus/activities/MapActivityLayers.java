package net.osmand.plus.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.osmand.CallbackWithObject;
import net.osmand.StateChangedListener;
import net.osmand.plus.ContextMenuAdapter;
import net.osmand.plus.ContextMenuItem;
import net.osmand.plus.GPXUtilities.GPXFile;
import net.osmand.plus.GPXUtilities.WptPt;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity.ShowQuickSearchMode;
import net.osmand.plus.helpers.GpxUiHelper;
import net.osmand.plus.measurementtool.MeasurementToolLayer;
import net.osmand.plus.poi.PoiFiltersHelper;
import net.osmand.plus.poi.PoiUIFilter;
import net.osmand.plus.quickaction.QuickActionRegistry;
import net.osmand.plus.render.MapVectorLayer;
import net.osmand.plus.render.RenderingIcons;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.plus.views.ContextMenuLayer;
import net.osmand.plus.views.DownloadedRegionsLayer;
import net.osmand.plus.views.FavouritesLayer;
import net.osmand.plus.views.GPXLayer;
import net.osmand.plus.views.ImpassableRoadsLayer;
import net.osmand.plus.views.MapControlsLayer;
import net.osmand.plus.views.MapInfoLayer;
import net.osmand.plus.views.MapMarkersLayer;
import net.osmand.plus.views.MapQuickActionLayer;
import net.osmand.plus.views.MapTextLayer;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.views.POIMapLayer;
import net.osmand.plus.views.PointLocationLayer;
import net.osmand.plus.views.PointNavigationLayer;
import net.osmand.plus.views.RouteLayer;
import net.osmand.plus.views.RulerControlLayer;
import net.osmand.plus.views.TransportStopsLayer;
import net.osmand.plus.views.mapwidgets.MapWidgetRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Object is responsible to maintain layers using by map activity
 */
public class MapActivityLayers {
	private final MapActivity activity;

	// the order of layer should be preserved ! when you are inserting new layer
	private MapVectorLayer mapVectorLayer;
	private GPXLayer gpxLayer;
    private POIMapLayer poiMapLayer;
	private FavouritesLayer mFavouritesLayer;
	private TransportStopsLayer transportStopsLayer;
	private RulerControlLayer rulerControlLayer;
	private MapMarkersLayer mapMarkersLayer;
	private MapInfoLayer mapInfoLayer;
	private ContextMenuLayer contextMenuLayer;
	private MapControlsLayer mapControlsLayer;
	private MapQuickActionLayer mapQuickActionLayer;
	private DownloadedRegionsLayer downloadedRegionsLayer;
	private final MapWidgetRegistry mapWidgetRegistry;
	private final QuickActionRegistry quickActionRegistry;
	private MeasurementToolLayer measurementToolLayer;

    MapActivityLayers(MapActivity activity) {
		this.activity = activity;
		this.mapWidgetRegistry = new MapWidgetRegistry(activity.getMyApplication().getSettings());
		this.quickActionRegistry = new QuickActionRegistry(activity.getMyApplication().getSettings());
	}

	public QuickActionRegistry getQuickActionRegistry() {
		return quickActionRegistry;
	}

	public MapWidgetRegistry getMapWidgetRegistry() {
		return mapWidgetRegistry;
	}

	private OsmandApplication getApplication() {
		return (OsmandApplication) activity.getApplication();
	}

	void createLayers(final OsmandMapTileView mapView) {
		OsmandApplication app = getApplication();
		RoutingHelper routingHelper = app.getRoutingHelper();
		// first create to make accessible
		MapTextLayer mapTextLayer = new MapTextLayer();
		// 5.95 all labels
		mapView.addLayer(mapTextLayer, 5.95f);
		// 8. context menu layer 
		contextMenuLayer = new ContextMenuLayer(activity);
		mapView.addLayer(contextMenuLayer, 8);

		// 0.5 layer
		mapVectorLayer = new MapVectorLayer(false);
		mapView.addLayer(mapVectorLayer, 0.5f);

		downloadedRegionsLayer = new DownloadedRegionsLayer();
		mapView.addLayer(downloadedRegionsLayer, 0.5f);

		// 0.9 gpx layer
		gpxLayer = new GPXLayer();
		mapView.addLayer(gpxLayer, 0.9f);

		// 1. route layer
        RouteLayer routeLayer = new RouteLayer(routingHelper);
		mapView.addLayer(routeLayer, 1);

		// 2. osm bugs layer
		// 3. poi layer
		poiMapLayer = new POIMapLayer(activity);
		mapView.addLayer(poiMapLayer, 3);
		// 4. favorites layer
		mFavouritesLayer = new FavouritesLayer();
		mapView.addLayer(mFavouritesLayer, 4);
		// 4.6 measurement tool layer
		measurementToolLayer = new MeasurementToolLayer();
		mapView.addLayer(measurementToolLayer, 4.6f);
		// 5. transport layer
		transportStopsLayer = new TransportStopsLayer(activity);
		mapView.addLayer(transportStopsLayer, 5);
		// 5.95 all text labels
		// 6. point location layer 
		PointLocationLayer locationLayer = new PointLocationLayer(activity.getMapViewTrackingUtilities());
		mapView.addLayer(locationLayer, 6);
		// 7. point navigation layer
		PointNavigationLayer navigationLayer = new PointNavigationLayer(activity);
		mapView.addLayer(navigationLayer, 7);
		// 7.3 map markers layer
		mapMarkersLayer = new MapMarkersLayer(activity);
		mapView.addLayer(mapMarkersLayer, 7.3f);
		// 7.5 Impassible roads
		ImpassableRoadsLayer impassableRoadsLayer = new ImpassableRoadsLayer(activity);
		mapView.addLayer(impassableRoadsLayer, 7.5f);
		// 7.8 ruler control layer
		rulerControlLayer = new RulerControlLayer(activity);
		mapView.addLayer(rulerControlLayer, 7.8f);
		// 8. context menu layer 
		// 9. map info layer
		mapInfoLayer = new MapInfoLayer(activity, routeLayer);
		mapView.addLayer(mapInfoLayer, 9);
		// 11. route info layer
		mapControlsLayer = new MapControlsLayer(activity);
		mapView.addLayer(mapControlsLayer, 11);
		// 12. quick actions layer
		mapQuickActionLayer = new MapQuickActionLayer(activity, contextMenuLayer);
		mapView.addLayer(mapQuickActionLayer, 12);
		contextMenuLayer.setMapQuickActionLayer(mapQuickActionLayer);
		mapControlsLayer.setMapQuickActionLayer(mapQuickActionLayer);

        StateChangedListener<Integer> transparencyListener = change -> {
			mapVectorLayer.setAlpha(change);
			mapView.refreshMap();
		};
		app.getSettings().MAP_TRANSPARENCY.addListener(transparencyListener);

		OsmandPlugin.createLayers(activity);
		app.getAidlApi().registerMapLayers(activity);
	}

	public void updateLayers(OsmandMapTileView mapView) {
		OsmandSettings settings = getApplication().getSettings();
		updateMapSource(mapView);
		boolean showStops = settings.getCustomRenderBooleanProperty(OsmandSettings.TRANSPORT_STOPS_OVER_MAP).get();
		transportStopsLayer.setShowTransportStops(showStops);
		OsmandPlugin.refreshLayers(mapView, activity);
	}

	private void updateMapSource(OsmandMapTileView mapView) {
		OsmandSettings settings = getApplication().getSettings();

		// update transparency
		int mapTransparency = settings.MAP_UNDERLAY.get() == null ? 255 : settings.MAP_TRANSPARENCY.get();
		mapVectorLayer.setAlpha(mapTransparency);

		mapVectorLayer.setVisible(true);
		mapView.setMainLayer(mapVectorLayer);
	}

	public AlertDialog showGPXFileLayer(List<String> files, final OsmandMapTileView mapView) {
		final OsmandSettings settings = getApplication().getSettings();
		CallbackWithObject<GPXFile[]> callbackWithObject = result -> {
			WptPt locToShow = null;
			for (GPXFile g : result) {
				if (g.showCurrentTrack) {
					if (!settings.SAVE_TRACK_TO_GPX.get() && !
							settings.SAVE_GLOBAL_TRACK_TO_GPX.get()) {
						Toast.makeText(activity,
								R.string.gpx_monitoring_disabled_warn, Toast.LENGTH_LONG).show();
					}
					break;
				} else {
					locToShow = g.findPointToShow();
				}
			}
			getApplication().getSelectedGpxHelper().setGpxFileToDisplay(result);
			if (locToShow != null) {
				mapView.getAnimatedDraggingThread().startMoving(locToShow.lat, locToShow.lon,
						mapView.getZoom(), true);
			}
			mapView.refreshMap();
			activity.getDashboard().refreshContent(true);
			return true;
		};
		return GpxUiHelper.selectGPXFiles(files, activity, callbackWithObject);
	}

	public void showMultichoicePoiFilterDialog(final OsmandMapTileView mapView, final DismissListener listener) {
		final OsmandApplication app = getApplication();
		final PoiFiltersHelper poiFilters = app.getPoiFilters();
		final ContextMenuAdapter adapter = new ContextMenuAdapter();
		final List<PoiUIFilter> list = new ArrayList<>();
		for (PoiUIFilter f : poiFilters.getTopDefinedPoiFilters()) {
			addFilterToList(adapter, list, f, true);
		}
		for (PoiUIFilter f : poiFilters.getSearchPoiFilters()) {
			addFilterToList(adapter, list, f, true);
		}
		list.add(poiFilters.getCustomPOIFilter());

		final ArrayAdapter<ContextMenuItem> listAdapter = adapter.createListAdapter(activity, app.getSettings().isLightContent());
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final ListView listView = new ListView(activity);
		listView.setDivider(null);
		listView.setClickable(true);
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			ContextMenuItem item = listAdapter.getItem(position);
			item.setSelected(!item.getSelected());
			item.getItemClickListener().onContextMenuClick(listAdapter, position, position, item.getSelected(), null);
			listAdapter.notifyDataSetChanged();
		});
		builder.setView(listView)
				.setTitle(R.string.show_poi_over_map)
				.setPositiveButton(R.string.shared_string_ok, (dialog, which) -> {
					for (int i = 0; i < listAdapter.getCount(); i++) {
						ContextMenuItem item = listAdapter.getItem(i);
							PoiUIFilter filter = list.get(i);
						if (item.getSelected()) {
							getApplication().getPoiFilters().addSelectedPoiFilter(filter);
						} else {
							getApplication().getPoiFilters().removeSelectedPoiFilter(filter);
						}
					}
					mapView.refreshMap();
				})
				.setNegativeButton(R.string.shared_string_cancel, null)
				// TODO go to single choice dialog
				.setNeutralButton(" ", (dialog, which) -> showSingleChoicePoiFilterDialog(mapView, listener));
		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(dialog -> {
			Button neutralButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
			Drawable drawable = app.getIconsCache().getThemedIcon(R.drawable.ic_action_singleselect);
			neutralButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			neutralButton.setContentDescription(app.getString(R.string.shared_string_filters));
		});
		alertDialog.setOnDismissListener(dialog -> listener.dismiss());
		alertDialog.show();
	}

	public void showSingleChoicePoiFilterDialog(final OsmandMapTileView mapView, final DismissListener listener) {
		final OsmandApplication app = getApplication();
		final PoiFiltersHelper poiFilters = app.getPoiFilters();
		final ContextMenuAdapter adapter = new ContextMenuAdapter();
		adapter.addItem(new ContextMenuItem.ItemBuilder()
				.setTitleId(R.string.shared_string_search, app)
				.setIcon(R.drawable.ic_action_search_dark).createItem());
		final List<PoiUIFilter> list = new ArrayList<>();
		list.add(poiFilters.getCustomPOIFilter());
		for (PoiUIFilter f : poiFilters.getTopDefinedPoiFilters()) {
			addFilterToList(adapter, list, f, false);
		}
		for (PoiUIFilter f : poiFilters.getSearchPoiFilters()) {
			addFilterToList(adapter, list, f, false);
		}

		final ArrayAdapter<ContextMenuItem> listAdapter =
				adapter.createListAdapter(activity, app.getSettings().isLightContent());
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setAdapter(listAdapter, (dialog, which) -> {
			PoiUIFilter pf = list.get(which);
			String filterId = pf.getFilterId();
			if (filterId.equals(PoiUIFilter.CUSTOM_FILTER_ID)) {
				if (activity.getDashboard().isVisible()) {
					activity.getDashboard().hideDashboard();
				}
				activity.showQuickSearch(ShowQuickSearchMode.NEW, true);
			} else {
				getApplication().getPoiFilters().clearSelectedPoiFilters();
				getApplication().getPoiFilters().addSelectedPoiFilter(pf);
				mapView.refreshMap();
			}
		});
		builder.setTitle(R.string.show_poi_over_map);
		builder.setNegativeButton(R.string.shared_string_dismiss, null);
		builder.setNeutralButton(" ", (dialog, which) -> showMultichoicePoiFilterDialog(mapView, listener));
		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(dialog -> {
			Button neutralButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
			Drawable drawable = app.getIconsCache().getThemedIcon(R.drawable.ic_action_multiselect);
			neutralButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			neutralButton.setContentDescription(app.getString(R.string.apply_filters));
		});
		alertDialog.setOnDismissListener(dialog -> listener.dismiss());
		alertDialog.show();
	}

	private void addFilterToList(final ContextMenuAdapter adapter,
								 final List<PoiUIFilter> list,
								 final PoiUIFilter f,
								 boolean multichoice) {
		list.add(f);
		ContextMenuItem.ItemBuilder builder = new ContextMenuItem.ItemBuilder();
		if (multichoice) {
			builder.setSelected(getApplication().getPoiFilters().isPoiFilterSelected(f));
			builder.setListener((adapter1, itemId, position, isChecked, viewCoordinates) -> {
				ContextMenuItem item = adapter1.getItem(position);
				item.setSelected(isChecked);
				return false;
			});
		}
		builder.setTitle(f.getName());
		if (RenderingIcons.containsBigIcon(f.getIconId())) {
			builder.setIcon(RenderingIcons.getBigIconResourceId(f.getIconId()));
		} else {
			builder.setIcon(R.drawable.mx_user_defined);
		}
		builder.setColor(ContextMenuItem.INVALID_ID);
		builder.setSkipPaintingWithoutColor(true);
		adapter.addItem(builder.createItem());
	}

	public GPXLayer getGpxLayer() {
		return gpxLayer;
	}
	public ContextMenuLayer getContextMenuLayer() {
		return contextMenuLayer;
	}
	public FavouritesLayer getFavouritesLayer() {
		return mFavouritesLayer;
	}
	public MeasurementToolLayer getMeasurementToolLayer() {
		return measurementToolLayer;
	}
	public RulerControlLayer getRulerControlLayer() {
		return rulerControlLayer;
	}
	public MapInfoLayer getMapInfoLayer() {
		return mapInfoLayer;
	}
	public MapControlsLayer getMapControlsLayer() {
		return mapControlsLayer;
	}
	public MapQuickActionLayer getMapQuickActionLayer() {
		return mapQuickActionLayer;
	}
	public MapMarkersLayer getMapMarkersLayer() {
		return mapMarkersLayer;
	}
	public POIMapLayer getPoiMapLayer() {
		return poiMapLayer;
	}
	public TransportStopsLayer getTransportStopsLayer() {
		return transportStopsLayer;
	}
	public DownloadedRegionsLayer getDownloadedRegionsLayer() {
		return downloadedRegionsLayer;
	}
	public interface DismissListener {
		void dismiss();
	}
}