package net.osmand.plus;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import gnu.trove.map.hash.TLongObjectHashMap;

public class IconsCache {

	private final TLongObjectHashMap<Drawable> drawable = new TLongObjectHashMap<>();
	private final OsmandApplication app;
	
	public IconsCache(OsmandApplication app) {
		this.app = app;
	}

	private Drawable getDrawable(@DrawableRes int resId, @ColorRes int clrId) {
		long hash = ((long)resId << 31L) + clrId;
		Drawable d = drawable.get(hash);
		if (d == null) {
			d = ContextCompat.getDrawable(app, resId);
			d = DrawableCompat.wrap(d);
			d.mutate();
			if (clrId != 0) {
				DrawableCompat.setTint(d, ContextCompat.getColor(app, clrId));
			}
			drawable.put(hash, d);
		}
		return d;
	}

	private Drawable getPaintedDrawable(@DrawableRes int resId, @ColorInt int color){
		long hash = ((long)resId << 31L) + color;
		Drawable d = drawable.get(hash);
		if(d == null) {
			d = ContextCompat.getDrawable(app, resId);
			d = DrawableCompat.wrap(d);
			d.mutate();
			DrawableCompat.setTint(d, color);

			drawable.put(hash, d);
		}
		return d;
	}

	public Drawable getPaintedIcon(@DrawableRes int id, @ColorInt int color){
		return getPaintedDrawable(id, color);
	}

	public Drawable getIcon(@DrawableRes int id, @ColorRes int colorId) {
		return getDrawable(id, colorId);
	}

	public Drawable getIcon(@DrawableRes int backgroundId, @DrawableRes int id, @ColorRes int colorId) {
		Drawable b = getDrawable(backgroundId, 0);
		Drawable f = getDrawable(id, colorId);
		Drawable[] layers = new Drawable[2];
		layers[0] = b;
		layers[1] = f;
		return new LayerDrawable(layers);
	}

	public Drawable getThemedIcon(@DrawableRes int id) {
		return getDrawable(id, app.getSettings().isLightContent() ? R.color.icon_color : 0);
	}

	public Drawable getIcon(@DrawableRes int id) {
		return getDrawable(id, 0);
	}

	public Drawable getIcon(@DrawableRes int id, boolean light) {
		return getDrawable(id, light ? R.color.icon_color : 0);
	}
}
