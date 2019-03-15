package net.osmand.plus.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import net.osmand.NativeLibrary;
import net.osmand.NativeLibrary.NativeSearchResult;
import net.osmand.plus.render.TextRenderer.TextDrawInfo;
import net.osmand.render.RenderingRuleProperty;
import net.osmand.render.RenderingRuleSearchRequest;
import net.osmand.util.Algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OsmandRenderer {
	private final Map<float[], PathEffect> dashEffect = new LinkedHashMap<>();
	private final Map<String, float[]> parsedDashEffects = new LinkedHashMap<>();
	private final Map<String, Shader> shaders = new LinkedHashMap<>();
	private final Context context;

	/* package */
	public static class RenderingContext extends net.osmand.RenderingContext {
		final List<TextDrawInfo> textToDraw = new ArrayList<>();
		final Context ctx;

		public RenderingContext(Context ctx) {
			this.ctx = ctx;
		}

		@Override
		protected byte[] getIconRawData(String data) {
			return RenderingIcons.getIconRawData(ctx, data);
		}
	}

	OsmandRenderer(Context context) {
		this.context = context;

		Paint paintIcon = new Paint();
		paintIcon.setStyle(Style.STROKE);

        Paint paint = new Paint();
		paint.setAntiAlias(true);

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wmgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wmgr.getDefaultDisplay().getMetrics(dm);
	}

	private PathEffect getDashEffect(RenderingContext rc, float[] cachedValues, float st){
		float[] dashes = new float[cachedValues.length / 2];
		for (int i = 0; i < dashes.length; i++) {
			dashes[i] = rc.getDensityValue(cachedValues[i * 2]) + cachedValues[i * 2 + 1];
		}
		if(!dashEffect.containsKey(dashes)){
			dashEffect.put(dashes, new DashPathEffect(dashes, st));
		}
		return dashEffect.get(dashes);
	}

	private Shader getShader(String resId){
		if(shaders.get(resId) == null){
			Bitmap bmp = RenderingIcons.getIcon(context, resId, true);
			if(bmp != null){
				Shader sh = new BitmapShader(bmp, TileMode.REPEAT, TileMode.REPEAT);
				shaders.put(resId, sh);
			} else {
				shaders.put(resId, null);
			}
		}	
		return shaders.get(resId);
	}
	
	void generateNewBitmapNative(RenderingContext rc, NativeOsmandLibrary library,
								 NativeSearchResult searchResultHandler,
								 Bitmap bmp, RenderingRuleSearchRequest render) {
		long now = System.currentTimeMillis();
		if (rc.width > 0 && rc.height > 0 && searchResultHandler != null) {
			try {
				final NativeLibrary.RenderingGenerationResult res = library.generateRendering(
					rc, searchResultHandler, bmp, bmp.hasAlpha(), render);
				long time = System.currentTimeMillis() - now;
				rc.renderingDebugInfo = String.format("Rendering: %s ms  (%s text)\n"
						+ "(%s points, %s points inside, %s of %s objects visible)\n",//$NON-NLS-1$
						time, rc.textRenderingTime, rc.pointCount, rc.pointInsideCount, rc.visible, rc.allObjects);
				
				// See upper note
				if(res.bitmapBuffer != null) {
					bmp.copyPixelsFromBuffer(res.bitmapBuffer);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean updatePaint(RenderingRuleSearchRequest req, Paint p, int ind, boolean area, RenderingContext rc){
		RenderingRuleProperty rColor;
		RenderingRuleProperty rStrokeW;
		RenderingRuleProperty rCap;
		RenderingRuleProperty rPathEff;
		
		if (ind == 0) {
			rColor = req.ALL.R_COLOR;
			rStrokeW = req.ALL.R_STROKE_WIDTH;
			rCap = req.ALL.R_CAP;
			rPathEff = req.ALL.R_PATH_EFFECT;
		} else if(ind == 1){
			rColor = req.ALL.R_COLOR_2;
			rStrokeW = req.ALL.R_STROKE_WIDTH_2;
			rCap = req.ALL.R_CAP_2;
			rPathEff = req.ALL.R_PATH_EFFECT_2;
		} else if(ind == -1){
			rColor = req.ALL.R_COLOR_0;
			rStrokeW = req.ALL.R_STROKE_WIDTH_0;
			rCap = req.ALL.R_CAP_0;
			rPathEff = req.ALL.R_PATH_EFFECT_0;
		} else if(ind == -2){
			rColor = req.ALL.R_COLOR__1;
			rStrokeW = req.ALL.R_STROKE_WIDTH__1;
			rCap = req.ALL.R_CAP__1;
			rPathEff = req.ALL.R_PATH_EFFECT__1;
		} else if(ind == 2){
			rColor = req.ALL.R_COLOR_3;
			rStrokeW = req.ALL.R_STROKE_WIDTH_3;
			rCap = req.ALL.R_CAP_3;
			rPathEff = req.ALL.R_PATH_EFFECT_3;
		} else if(ind == -3){
			rColor = req.ALL.R_COLOR__2;
			rStrokeW = req.ALL.R_STROKE_WIDTH__2;
			rCap = req.ALL.R_CAP__2;
			rPathEff = req.ALL.R_PATH_EFFECT__2;
		} else if(ind == 3){
			rColor = req.ALL.R_COLOR_4;
			rStrokeW = req.ALL.R_STROKE_WIDTH_4;
			rCap = req.ALL.R_CAP_4;
			rPathEff = req.ALL.R_PATH_EFFECT_4;
		} else {
			rColor = req.ALL.R_COLOR_5;
			rStrokeW = req.ALL.R_STROKE_WIDTH_5;
			rCap = req.ALL.R_CAP_5;
			rPathEff = req.ALL.R_PATH_EFFECT_5;
		}
		if(area){
			if(!req.isSpecified(rColor) && !req.isSpecified(req.ALL.R_SHADER)){
				return false;
			}
			p.setShader(null);
			p.setColorFilter(null);
			p.clearShadowLayer();
			p.setStyle(Style.FILL_AND_STROKE);
			p.setStrokeWidth(0);
		} else {
			if(!req.isSpecified(rStrokeW)){
				return false;
			}
			p.setShader(null);
			p.setColorFilter(null);
			p.clearShadowLayer();
			p.setStyle(Style.STROKE);
			p.setStrokeWidth(rc.getComplexValue(req, rStrokeW));
			String cap = req.getStringPropertyValue(rCap);
			if(!Algorithms.isEmpty(cap)){
				p.setStrokeCap(Cap.valueOf(cap.toUpperCase()));
			} else {
				p.setStrokeCap(Cap.BUTT);
			}
			String pathEffect = req.getStringPropertyValue(rPathEff);
			if (!Algorithms.isEmpty(pathEffect)) {
				if(!parsedDashEffects.containsKey(pathEffect)) {
					String[] vls = pathEffect.split("_");
					float[] vs = new float[vls.length * 2];
					for(int i = 0; i < vls.length; i++) {
						int s = vls[i].indexOf(':');
						String pre = vls[i];
						String post = "";
						if(s != -1) {
							pre = vls[i].substring(0, i);
							post = vls[i].substring(i + 1);
						}
						if(pre.length() > 0) {
							vs[i*2 ] = Float.parseFloat(pre);
						}
						if(post.length() > 0) {
							vs[i*2 +1] = Float.parseFloat(post);
						}
					}
					parsedDashEffects.put(pathEffect, vs);
				}
				float[] cachedValues = parsedDashEffects.get(pathEffect);
				
				p.setPathEffect(getDashEffect(rc, cachedValues, 0));
			} else {
				p.setPathEffect(null);
			}
		}
		p.setColor(req.getIntPropertyValue(rColor));
		if(ind == 0){
			String resId = req.getStringPropertyValue(req.ALL.R_SHADER);
			if(resId != null){
				if(req.getIntPropertyValue(rColor) == 0) {
					p.setColor(Color.WHITE); // set color required by skia
				}
				p.setShader(getShader(resId));
			}
			// do not check shadow color here
			if(rc.shadowRenderingMode == 1) {
				int shadowColor = req.getIntPropertyValue(req.ALL.R_SHADOW_COLOR);
				if(shadowColor == 0) {
					shadowColor = rc.shadowRenderingColor;
				}
				int shadowRadius = (int) rc.getComplexValue(req, req.ALL.R_SHADOW_RADIUS);
				if (shadowColor == 0) {
					shadowRadius = 0;
				}
				p.setShadowLayer(shadowRadius, 0, 0, shadowColor);
			}
		}
		return true;
	}
}