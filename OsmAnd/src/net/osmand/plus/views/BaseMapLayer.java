package net.osmand.plus.views;

public abstract class BaseMapLayer extends OsmandMapLayer {

	public static final int DEFAULT_MAX_ZOOM = 21;
	public static final int DEFAULT_MIN_ZOOM = 1;
	private int alpha = 255;

	public int getMaximumShownMapZoom(){
		return DEFAULT_MAX_ZOOM;
	}
	
	public int getMinimumShownMapZoom(){
		return DEFAULT_MIN_ZOOM;
	}
	
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	
	protected int getAlpha() {
		return alpha;
	}
	
	
}
