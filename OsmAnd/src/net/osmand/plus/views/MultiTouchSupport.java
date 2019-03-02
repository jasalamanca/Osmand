package net.osmand.plus.views;

import android.graphics.PointF;
import android.view.MotionEvent;

import net.osmand.PlatformUtil;
import net.osmand.util.MapUtils;

import org.apache.commons.logging.Log;

import java.lang.reflect.Method;


class MultiTouchSupport {
	private static final Log log = PlatformUtil.getLog(MultiTouchSupport.class);
	
	private static final int ACTION_MASK = 255;
	private static final int ACTION_POINTER_DOWN     = 5;
	private static final int ACTION_POINTER_UP     = 6;
	private float angleStarted;
	private float angleRelative;

	public interface MultiTouchZoomListener {
    		void onZoomStarted(PointF centerPoint);
    		void onZoomingOrRotating(double relativeToStart, float angle);
    		void onZoomOrRotationEnded(double relativeToStart, float angleRelative);
    		void onGestureInit(float x1, float y1, float x2, float y2);
			void onActionPointerUp();
			void onActionCancel();
	}

	private boolean multiTouchAPISupported = false;
	private final MultiTouchZoomListener listener;

	private Method getPointerCount;
	private Method getX;
	private Method getY;
//	private Method getPointerId;

	MultiTouchSupport(MultiTouchZoomListener listener){
		this.listener = listener;
		initMethods();
	}

	private boolean isMultiTouchSupported(){
		return multiTouchAPISupported;
	}
	boolean isInZoomMode(){
		return inZoomMode;
	}

	private void initMethods(){
		try {
			getPointerCount = MotionEvent.class.getMethod("getPointerCount"); //$NON-NLS-1$
//			getPointerId = MotionEvent.class.getMethod("getPointerId", Integer.TYPE); //$NON-NLS-1$
			getX = MotionEvent.class.getMethod("getX", Integer.TYPE); //$NON-NLS-1$
			getY = MotionEvent.class.getMethod("getY", Integer.TYPE); //$NON-NLS-1$	
			multiTouchAPISupported = true;
		} catch (Exception e) {
			multiTouchAPISupported = false;
			log.info("Multi touch not supported", e); //$NON-NLS-1$
		}
	}

	private boolean inZoomMode = false;
	private double zoomStartedDistance = 100;
	private double zoomRelative = 1;
	private PointF centerPoint = new PointF();

	public boolean onTouchEvent(MotionEvent event){
		if(!isMultiTouchSupported()){
			return false;
		}
		int actionCode = event.getAction() & ACTION_MASK;
		try {
			if (actionCode == MotionEvent.ACTION_CANCEL) {
				listener.onActionCancel();
			}
			Integer pointCount = (Integer) getPointerCount.invoke(event);
			if(pointCount < 2){
				if(inZoomMode){
					listener.onZoomOrRotationEnded(zoomRelative, angleRelative);
					inZoomMode = false;
					return true;
				}
				return false;
			}
			float x1 = (Float) getX.invoke(event, 0);
			float x2 = (Float) getX.invoke(event, 1);
			float y1 = (Float) getY.invoke(event, 0);
			float y2 = (Float) getY.invoke(event, 1);
			float distance = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
			float angle = 0;
			boolean angleDefined = false;
			if(x1 != x2 || y1 != y2) {
				angleDefined = true;
				angle = (float) (Math.atan2(y2 - y1, x2 -x1) * 180 / Math.PI);
			}
			if (actionCode == MotionEvent.ACTION_UP || actionCode == MotionEvent.ACTION_POINTER_UP) {
				listener.onActionPointerUp();
			}
			if (actionCode == ACTION_POINTER_DOWN) {
				centerPoint = new PointF((x1 + x2) / 2, (y1 + y2) / 2);
				listener.onGestureInit(x1, y1, x2, y2);
				listener.onZoomStarted(centerPoint);
				zoomStartedDistance = distance;
				angleStarted = angle;
				inZoomMode = true;
				return true;
			} else if(actionCode == ACTION_POINTER_UP){
				if(inZoomMode){
					listener.onZoomOrRotationEnded(zoomRelative, angleRelative);
					inZoomMode = false;
				}
				return true;
			} else if(inZoomMode && actionCode == MotionEvent.ACTION_MOVE){
				// Keep zoom center fixed or flexible
				centerPoint = new PointF((x1 + x2) / 2, (y1 + y2) / 2);

				if(angleDefined) {
					angleRelative = MapUtils.unifyRotationTo360(angle - angleStarted);
				}
				zoomRelative = distance / zoomStartedDistance;
				listener.onZoomingOrRotating(zoomRelative, angleRelative);
				return true;
			}
		} catch (Exception e) {
			log.debug("Multi touch exception" , e); //$NON-NLS-1$
		}
		return false;
	}

	PointF getCenterPoint() {
		return centerPoint;
	}
}