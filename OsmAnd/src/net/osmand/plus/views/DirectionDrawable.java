package net.osmand.plus.views;

import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by Denis
 * on 10.12.2014.
 */
public class DirectionDrawable extends Drawable {
	private final Paint paintRouteDirection;
	private float width;
	private float height;
	private final Context ctx;
	private float angle;
	private int resourceId = -1;
	private Drawable arrowImage ;

	public DirectionDrawable(Context ctx, float width, float height, int resourceId, int clrId) {
		this(ctx, width, height);
		IconsCache iconsCache = ((OsmandApplication) ctx.getApplicationContext()).getIconsCache();
		arrowImage = iconsCache.getIcon(resourceId, clrId);
		this.resourceId = resourceId;
	}
	
	public DirectionDrawable(Context ctx, float width, float height) {
		this.ctx = ctx;
		this.width = width;
		this.height = height;
		paintRouteDirection = new Paint();
		paintRouteDirection.setStyle(Paint.Style.FILL_AND_STROKE);
		paintRouteDirection.setColor(ctx.getResources().getColor(R.color.color_unknown));
		paintRouteDirection.setAntiAlias(true);
	}
	
	public void setImage(int resourceId, int clrId) {
		IconsCache iconsCache = ((OsmandApplication) ctx.getApplicationContext()).getIconsCache();
		arrowImage = iconsCache.getIcon(resourceId, clrId);
		this.resourceId = resourceId;
		onBoundsChange(getBounds());
	}

	public void setColorId(int clrId) {
		if(arrowImage != null) {
			IconsCache iconsCache = ((OsmandApplication) ctx.getApplicationContext()).getIconsCache();
			arrowImage = iconsCache.getIcon(resourceId, clrId);
		} else {
			paintRouteDirection.setColor(ctx.getResources().getColor(clrId));
		}
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	@Override
	public int getIntrinsicWidth() {
		if (arrowImage != null) {
			return arrowImage.getIntrinsicWidth();
		}
		return super.getIntrinsicWidth();
	}
	
	@Override
	public int getIntrinsicHeight() {
		if (arrowImage != null) {
			return arrowImage.getIntrinsicHeight();
		}
		return super.getIntrinsicHeight();
	}
	
	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		if (arrowImage != null) {
			Rect r = bounds;
			int w = arrowImage.getIntrinsicWidth();
			int h = arrowImage.getIntrinsicHeight();
			int dx = Math.max(0, r.width() - w);
			int dy = Math.max(0, r.height() - h);
			if(r.width() == 0 && r.height() == 0) {
				arrowImage.setBounds(0, 0, w, h);
			} else {
				arrowImage.setBounds(r.left + dx / 2, r.top + dy / 2, r.right - dx / 2, r.bottom - dy / 2);
			}
		}
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		canvas.save();
		if (arrowImage != null) {
			Rect r = getBounds();
			canvas.rotate(angle, r.centerX(), r.centerY());
			arrowImage.draw(canvas);
		} else {
			canvas.rotate(angle, canvas.getWidth() / 2, canvas.getHeight() / 2);
			Path directionPath = createDirectionPath();
			canvas.drawPath(directionPath, paintRouteDirection);
		}
		canvas.restore();
	}

	@Override
	public int getOpacity() {
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		paintRouteDirection.setAlpha(alpha);

	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paintRouteDirection.setColorFilter(cf);
	}

	private Path createDirectionPath() {
		int h = 15;
		int w = 4;
		float sarrowL = 8; // side of arrow
		float harrowL = (float) Math.sqrt(2) * sarrowL; // hypotenuse of arrow
		float hpartArrowL = (harrowL - w) / 2;
		Path path = new Path();
		path.moveTo(width / 2, height - (height - h) / 3);
		path.rMoveTo(w / 2, 0);
		path.rLineTo(0, -h);
		path.rLineTo(hpartArrowL, 0);
		path.rLineTo(-harrowL / 2, -harrowL / 2); // center
		path.rLineTo(-harrowL / 2, harrowL / 2);
		path.rLineTo(hpartArrowL, 0);
		path.rLineTo(0, h);

		DisplayMetrics dm = new DisplayMetrics();
		Matrix pathTransform = new Matrix();
		WindowManager mgr = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		mgr.getDefaultDisplay().getMetrics(dm);
		pathTransform.postScale(dm.density, dm.density);
		path.transform(pathTransform);
		width *= dm.density;
		height *= dm.density;
		return path;
	}
}
