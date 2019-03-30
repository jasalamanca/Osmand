package net.osmand;

import net.osmand.render.RenderingRuleProperty;
import net.osmand.render.RenderingRuleSearchRequest;


public class RenderingContext {
	enum ShadowRenderingMode {
		// int shadowRenderingMode = 0; // no shadow (minumum CPU)
		// int shadowRenderingMode = 1; // classic shadow (the implementaton in master)
		// int shadowRenderingMode = 2; // blur shadow (most CPU, but still reasonable)
		// int shadowRenderingMode = 3; solid border (CPU use like classic version or even smaller)
		NO_SHADOW(0), ONE_STEP(1), BLUR_SHADOW(2), SOLID_SHADOW(3);
		final int value;

		ShadowRenderingMode(int v) {
			this.value = v;
		}
	}

	public int renderedState = 0; //NOTE jsala usado en C++

	// FIELDS OF THAT CLASS ARE USED IN C++
	public boolean interrupted = false;
	public String preferredLocale = "";
	public boolean transliterate = false;
	public int defaultColor = 0xf1eee8;

	protected RenderingContext() {
	}

	public double leftX;
	public double topY;
	public int width;
	public int height;

	public int zoom;
	public double tileDivisor;
	public float rotate;

	// debug purpose
	public final int pointCount = 0;
	public final int pointInsideCount = 0;
	public final int visible = 0;
	public final int allObjects = 0;
	public final int textRenderingTime = 0;
	public final int lastRenderedKey = 0;

	// be aware field is using in C++
	public float screenDensityRatio = 1;
	public float textScale = 1;
	public int shadowRenderingMode = ShadowRenderingMode.SOLID_SHADOW.value;
	public int shadowRenderingColor = 0xff969696;
	public String renderingDebugInfo;
	private long renderingContextHandle;
	private float density = 1;
	
	public void setDensityValue(float density) {
		this.density =  density ;
	}
	public float getDensityValue(float val) {
		return val * density;
	}
	public float getComplexValue(RenderingRuleSearchRequest req, RenderingRuleProperty prop) {
		return req.getFloatPropertyValue(prop, 0) * density + req.getIntPropertyValue(prop, 0);
	}

	//NOTE jsala se llama desde C++ pero como devuelve null hay que quitarlo.
	protected byte[] getIconRawData(String data) {
		return null;
	}
	
	protected void finalize() throws Throwable {
		super.finalize();
		if (renderingContextHandle != 0) {
			NativeLibrary.deleteRenderingContextHandle(renderingContextHandle);
			renderingContextHandle = 0;
		}
	}
}