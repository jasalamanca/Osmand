package net.osmand.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import net.osmand.aidl.calculateroute.CalculateRouteParams;
import net.osmand.aidl.favorite.AddFavoriteParams;
import net.osmand.aidl.favorite.RemoveFavoriteParams;
import net.osmand.aidl.favorite.UpdateFavoriteParams;
import net.osmand.aidl.favorite.group.AddFavoriteGroupParams;
import net.osmand.aidl.favorite.group.RemoveFavoriteGroupParams;
import net.osmand.aidl.favorite.group.UpdateFavoriteGroupParams;
import net.osmand.aidl.gpx.ASelectedGpxFile;
import net.osmand.aidl.gpx.HideGpxParams;
import net.osmand.aidl.gpx.ImportGpxParams;
import net.osmand.aidl.gpx.RemoveGpxParams;
import net.osmand.aidl.gpx.ShowGpxParams;
import net.osmand.aidl.map.SetMapLocationParams;
import net.osmand.aidl.maplayer.AddMapLayerParams;
import net.osmand.aidl.maplayer.RemoveMapLayerParams;
import net.osmand.aidl.maplayer.UpdateMapLayerParams;
import net.osmand.aidl.maplayer.point.AddMapPointParams;
import net.osmand.aidl.maplayer.point.RemoveMapPointParams;
import net.osmand.aidl.maplayer.point.UpdateMapPointParams;
import net.osmand.aidl.mapmarker.AddMapMarkerParams;
import net.osmand.aidl.mapmarker.RemoveMapMarkerParams;
import net.osmand.aidl.mapmarker.UpdateMapMarkerParams;
import net.osmand.aidl.mapwidget.AddMapWidgetParams;
import net.osmand.aidl.mapwidget.RemoveMapWidgetParams;
import net.osmand.aidl.mapwidget.UpdateMapWidgetParams;
import net.osmand.aidl.navigation.NavigateGpxParams;
import net.osmand.aidl.navigation.NavigateParams;
import net.osmand.plus.OsmandApplication;
import net.osmand.util.Algorithms;

import java.util.List;

public class OsmandAidlService extends Service {

	private OsmandApplication getApp() {
		return (OsmandApplication) getApplication();
	}

	private OsmandAidlApi getApi() {
		return getApp().getAidlApi();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// Return the interface
		return mBinder;
	}

	private final IOsmAndAidlInterface.Stub mBinder = new IOsmAndAidlInterface.Stub() {

		@Override
		public boolean refreshMap() {
			try {
				return getApi().reloadMap();
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean addFavoriteGroup(AddFavoriteGroupParams params) {
			try {
				return params != null && getApi().addFavoriteGroup(params.getFavoriteGroup());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean removeFavoriteGroup(RemoveFavoriteGroupParams params) {
			try {
				return params != null && getApi().removeFavoriteGroup(params.getFavoriteGroup());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean updateFavoriteGroup(UpdateFavoriteGroupParams params) {
			try {
				return params != null && getApi().updateFavoriteGroup(params.getFavoriteGroupPrev(), params.getFavoriteGroupNew());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean addFavorite(AddFavoriteParams params) {
			try {
				return params != null && getApi().addFavorite(params.getFavorite());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean removeFavorite(RemoveFavoriteParams params) {
			try {
				return params != null && getApi().removeFavorite(params.getFavorite());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean updateFavorite(UpdateFavoriteParams params) {
			try {
				return params != null && getApi().updateFavorite(params.getFavoritePrev(), params.getFavoriteNew());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean addMapMarker(AddMapMarkerParams params) {
			try {
				return params != null && getApi().addMapMarker(params.getMarker());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean removeMapMarker(RemoveMapMarkerParams params) {
			try {
				return params != null && getApi().removeMapMarker(params.getMarker());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean updateMapMarker(UpdateMapMarkerParams params) {
			try {
				return params != null && getApi().updateMapMarker(params.getMarkerPrev(), params.getMarkerNew());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean addMapWidget(AddMapWidgetParams params) {
			try {
				return params != null && getApi().addMapWidget(params.getWidget());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean removeMapWidget(RemoveMapWidgetParams params) {
			try {
				return params != null && getApi().removeMapWidget(params.getId());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean updateMapWidget(UpdateMapWidgetParams params) {
			try {
				return params != null && getApi().updateMapWidget(params.getWidget());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean addMapPoint(AddMapPointParams params) {
			try {
				return params != null && getApi().putMapPoint(params.getLayerId(), params.getPoint());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean removeMapPoint(RemoveMapPointParams params) {
			try {
				return params != null && getApi().removeMapPoint(params.getLayerId(), params.getPointId());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean updateMapPoint(UpdateMapPointParams params) {
			try {
				return params != null && getApi().putMapPoint(params.getLayerId(), params.getPoint());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean addMapLayer(AddMapLayerParams params) {
			try {
				return params != null && getApi().addMapLayer(params.getLayer());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean removeMapLayer(RemoveMapLayerParams params) {
			try {
				return params != null && getApi().removeMapLayer(params.getId());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean updateMapLayer(UpdateMapLayerParams params) {
			try {
				return params != null && getApi().updateMapLayer(params.getLayer());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean importGpx(ImportGpxParams params) {
			if (params != null && !Algorithms.isEmpty(params.getDestinationPath())) {
				if (params.getGpxFile() != null) {
					return getApi().importGpxFromFile(params.getGpxFile(), params.getDestinationPath(),
							params.getColor(), params.isShow());
				} else if (params.getGpxUri() != null) {
					return getApi().importGpxFromUri(params.getGpxUri(), params.getDestinationPath(),
							params.getColor(), params.isShow());
				} else if (params.getSourceRawData() != null) {
					return getApi().importGpxFromData(params.getSourceRawData(), params.getDestinationPath(),
							params.getColor(), params.isShow());
				}
			}
			return false;
		}

		@Override
		public boolean showGpx(ShowGpxParams params) {
			if (params != null && params.getFileName() != null) {
				return getApi().showGpx(params.getFileName());
			}
			return false;
		}

		@Override
		public boolean hideGpx(HideGpxParams params) {
			if (params != null && params.getFileName() != null) {
				return getApi().hideGpx(params.getFileName());
			}
			return false;
		}

		@Override
		public boolean getActiveGpx(List<ASelectedGpxFile> files) {
			return getApi().getActiveGpx(files);
		}

		@Override
		public boolean removeGpx(RemoveGpxParams params) {
			if (params != null && params.getFileName() != null) {
				return getApi().removeGpx(params.getFileName());
			}
			return false;
		}

		@Override
		public boolean setMapLocation(SetMapLocationParams params) {
			if (params != null) {
				return getApi().setMapLocation(params.getLatitude(), params.getLongitude(),
						params.getZoom(), params.isAnimated());
			}
			return false;
		}

		@Override
		public boolean calculateRoute(CalculateRouteParams params) {
			if (params == null || params.getEndPoint() == null) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public boolean startGpxRecording() {
			try {
				return getApi().startGpxRecording();
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean stopGpxRecording() {
			try {
				return getApi().stopGpxRecording();
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean navigate(NavigateParams params) {
			try {
				return params != null && getApi().navigate(params.getStartName(), params.getStartLat(), params.getStartLon(), params.getDestName(), params.getDestLat(), params.getDestLon(), params.getProfile(), params.isForce());
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean navigateGpx(NavigateGpxParams params) {
			try {
				return params != null && getApi().navigateGpx(params.getData(), params.getUri(), params.isForce());
			} catch (Exception e) {
				return false;
			}
		}
	};
}
