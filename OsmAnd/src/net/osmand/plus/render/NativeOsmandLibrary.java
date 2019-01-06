package net.osmand.plus.render;

import android.graphics.Bitmap;

import net.osmand.NativeLibrary;
import net.osmand.PlatformUtil;
import net.osmand.plus.render.OsmandRenderer.RenderingContext;
import net.osmand.render.RenderingRuleSearchRequest;
import net.osmand.render.RenderingRulesStorage;

import org.apache.commons.logging.Log;

public class NativeOsmandLibrary extends NativeLibrary {
	private static final Log log = PlatformUtil.getLog(NativeOsmandLibrary.class);
	
	private static NativeOsmandLibrary library;
	private static Boolean isNativeSupported = null;

    private NativeOsmandLibrary() {
        super();
    }

    public static NativeOsmandLibrary getLoadedLibrary(){
		synchronized (NativeOsmandLibrary.class) {
			return library;
		}
	}

    public static NativeOsmandLibrary getLibrary(RenderingRulesStorage storage) {
		if (!isLoaded()) {
			synchronized (NativeOsmandLibrary.class) {
				if (!isLoaded()) {
					isNativeSupported = false;
					try {
						log.debug("Loading native c++_shared..."); //$NON-NLS-1$
						System.loadLibrary("c++_shared");
						log.debug("Loading native libraries..."); //$NON-NLS-1$
                        System.loadLibrary("osmand");
						log.debug("Creating NativeOsmandLibrary instance..."); //$NON-NLS-1$
						library = new NativeOsmandLibrary();
						log.debug("Initializing rendering rules storage..."); //$NON-NLS-1$
						NativeOsmandLibrary.initRenderingRulesStorage(storage);
						isNativeSupported = true;
					} catch (Throwable e) {
						log.error("Failed to load native library", e); //$NON-NLS-1$
					}
				}
			}
		}
		return library;
	}

	public static boolean isSupported()
	{
		return isNativeSupported != null && isNativeSupported;
	}
	
	public static boolean isLoaded() {
		return isNativeSupported != null;  
	}
	
	RenderingGenerationResult generateRendering(RenderingContext rc, NativeSearchResult searchResultHandler,
			Bitmap bitmap, boolean isTransparent, RenderingRuleSearchRequest render) {
		if (searchResultHandler == null) {
			log.error("Error search result = null"); //$NON-NLS-1$
			return new RenderingGenerationResult(null);
		}
		
        return generateRenderingDirect(rc, searchResultHandler.nativeHandler, bitmap, render);
	}

	private static native RenderingGenerationResult generateRenderingDirect(RenderingContext rc, long searchResultHandler,
			Bitmap bitmap, RenderingRuleSearchRequest render);
}
