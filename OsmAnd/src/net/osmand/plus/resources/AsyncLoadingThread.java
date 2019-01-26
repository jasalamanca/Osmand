package net.osmand.plus.resources;


import net.osmand.PlatformUtil;
import net.osmand.data.RotatedTileBox;
import net.osmand.map.MapTileDownloader;

import org.apache.commons.logging.Log;

import java.util.Stack;

/**
 * Thread to load map objects (POI, transport stops )async
 */
public class AsyncLoadingThread extends Thread {
	private static final Log log = PlatformUtil.getLog(AsyncLoadingThread.class);
	
	private final Stack<Object> requests = new Stack<Object>();
	private final ResourceManager resourceManger;

	public AsyncLoadingThread(ResourceManager resourceManger) {
		super("Loader map objects (synchronizer)"); //$NON-NLS-1$
		this.resourceManger = resourceManger;
	}

	@Override
	public void run() {
		while (true) {
			try {
				boolean mapLoaded = false;
				while (!requests.isEmpty()) {
					Object req = requests.pop();
					if (req instanceof MapLoadRequest) {
						if (!mapLoaded) {
							MapLoadRequest r = (MapLoadRequest) req;
							resourceManger.getRenderer().loadMap(r.tileBox);
							mapLoaded = !resourceManger.getRenderer().wasInterrupted();
							if (r.mapLoadedListener != null) {
								r.mapLoadedListener.onMapLoaded(!mapLoaded);
							}
						}
					}
				}
				if (mapLoaded) {
					// use downloader callback
					MapTileDownloader.getInstance().fireLoadCallback();
				}
				sleep(750);
			} catch (InterruptedException e) {
				log.error(e, e);
			} catch (RuntimeException e) {
				log.error(e, e);
			}
		}
	}

	public void requestToLoadMap(MapLoadRequest req) {
		requests.push(req);
	}

	public interface OnMapLoadedListener {
		void onMapLoaded(boolean interrupted);
	}

	static class MapLoadRequest {
		final RotatedTileBox tileBox;
		final OnMapLoadedListener mapLoadedListener;

		public MapLoadRequest(RotatedTileBox tileBox, OnMapLoadedListener mapLoadedListener) {
			super();
			this.tileBox = tileBox;
			this.mapLoadedListener = mapLoadedListener;
		}
	}
}