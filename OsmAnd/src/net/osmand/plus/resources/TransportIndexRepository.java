package net.osmand.plus.resources;

import net.osmand.data.TransportRoute;
import net.osmand.data.TransportStop;

import java.util.Collection;
import java.util.List;

public interface TransportIndexRepository {
	boolean checkContains(double latitude, double longitude);
	boolean checkContains(double topLatitude, double leftLongitude, double bottomLatitude, double rightLongitude);
	boolean acceptTransportStop(TransportStop stop);
	void searchTransportStops(double topLatitude, double leftLongitude, double bottomLatitude, double rightLongitude,
							  int limit, List<TransportStop> stops);
	Collection<TransportRoute> getRouteForStop(TransportStop stop);
}