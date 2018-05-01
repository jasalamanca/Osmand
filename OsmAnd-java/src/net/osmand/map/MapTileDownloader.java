package net.osmand.map;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class MapTileDownloader {
	// Download manager tile settings
	private static MapTileDownloader downloader = null;

	private List<WeakReference<IMapDownloaderCallback>> callbacks = new LinkedList<WeakReference<IMapDownloaderCallback>>();

	public static MapTileDownloader getInstance() {
		if (downloader == null) {
			downloader = new MapTileDownloader();
		}
		return downloader;
	}

	/**
	 * Callback for map downloader
	 */
	public interface IMapDownloaderCallback {
		/**
		 * Sometimes null cold be passed as request
		 * That means that there were a lot of requests but
		 * once method is called
		 * (in order to not create a collection of request & reduce calling times)
		 */
		public void tileDownloaded();
	}

	public MapTileDownloader() {
	}

	public void addDownloaderCallback(IMapDownloaderCallback callback) {
		LinkedList<WeakReference<IMapDownloaderCallback>> ncall = new LinkedList<WeakReference<IMapDownloaderCallback>>(callbacks);
		ncall.add(new WeakReference<MapTileDownloader.IMapDownloaderCallback>(callback));
		callbacks = ncall;
		}

	public void removeDownloaderCallback(IMapDownloaderCallback callback) {
		LinkedList<WeakReference<IMapDownloaderCallback>> ncall = new LinkedList<WeakReference<IMapDownloaderCallback>>(callbacks);
		Iterator<WeakReference<IMapDownloaderCallback>> it = ncall.iterator();
		while (it.hasNext()) {
			IMapDownloaderCallback c = it.next().get();
			if (c == callback) {
				it.remove();
				}
			}
		callbacks = ncall;
	}

	public void fireLoadCallback() {
		Iterator<WeakReference<IMapDownloaderCallback>> it = callbacks.iterator();
		while (it.hasNext()) {
			IMapDownloaderCallback c = it.next().get();
			if (c != null) {
				c.tileDownloaded();
			}
		}
	}
}
