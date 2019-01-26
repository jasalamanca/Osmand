package net.osmand.router;

import java.util.Map;

import net.osmand.binary.RouteDataObject;
import net.osmand.router.BinaryRoutePlanner.RouteSegment;

public interface VehicleRouter {

	
	boolean containsAttribute(String attribute);
	
	String getAttribute(String attribute);
	
	/**
	 * return if the road is accepted for routing
	 */
    boolean acceptLine(RouteDataObject way);
	
	/**
	 * return oneway +/- 1 if it is oneway and 0 if both ways
	 */
    int isOneWay(RouteDataObject road);
	
	/**
	 * return penalty transition in seconds
	 */
    float getPenaltyTransition(RouteDataObject road);
	
	/**
	 * return delay in seconds (0 no obstacles)
	 */
    float defineObstacle(RouteDataObject road, int point);
	
	/**
	 * return delay in seconds for height obstacles
	 */
    double defineHeightObstacle(RouteDataObject road, short startIndex, short endIndex);
	
	/**
	 * return delay in seconds (0 no obstacles)
	 */
    float defineRoutingObstacle(RouteDataObject road, int point);

	/**
	 * return routing speed in m/s for vehicle for specified road
	 */
    float defineRoutingSpeed(RouteDataObject road);
	
	/**
	 * return real speed in m/s for vehicle for specified road
	 */
    float defineVehicleSpeed(RouteDataObject road);
	
	/**
	 * define priority to multiply the speed for g(x) A* 
	 */
    float defineSpeedPriority(RouteDataObject road);

	/**
	 * Used for A* routing to calculate g(x)
	 * 
	 * @return minimal speed at road in m/s
	 */
    float getMinDefaultSpeed();

	/**
	 * Used for A* routing to predict h(x) : it should be great any g(x)
	 * 
	 * @return maximum speed to calculate shortest distance
	 */
    float getMaxDefaultSpeed();
	
	/**
	 * aware of road restrictions
	 */
    boolean restrictionsAware();
	
	/**
	 * Calculate turn time 
	 */
    double calculateTurnTime(RouteSegment segment, int segmentEnd, RouteSegment prev, int prevSegmentEnd);
	
		
	VehicleRouter build(Map<String, String> params);

	
	
}