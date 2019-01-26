package net.osmand.plus.resources;

import java.util.Collection;
import java.util.List;

import net.osmand.ResultMatcher;
import net.osmand.data.TransportRoute;
import net.osmand.data.TransportStop;

public interface TransportIndexRepository {
	
	boolean checkContains(double latitude, double longitude);

	boolean checkContains(double topLatitude, double leftLongitude, double bottomLatitude, double rightLongitude);
	
	boolean acceptTransportStop(TransportStop stop);
	
	void searchTransportStops(double topLatitude, double leftLongitude, double bottomLatitude, double rightLongitude,
                              int limit, List<TransportStop> stops, ResultMatcher<TransportStop> matcher);
	
	Collection<TransportRoute> getRouteForStop(TransportStop stop);
		
	
}
