package net.osmand.router;

public class RouteCalculationProgress {
	public int segmentNotFound = -1;
	public float distanceFromBegin;
	public float directDistance;
	public int directSegmentQueueSize;
	public float distanceFromEnd;
	public int reverseSegmentQueueSize;
	public float reverseDistance;
	public float totalEstimatedDistance = 0;
	
	public final float routingCalculatedTime = 0;
	public final int loadedTiles = 0;
	public final int visitedSegments = 0;
	
	public boolean isCancelled;
	public boolean requestPrivateAccessRouting;
}
