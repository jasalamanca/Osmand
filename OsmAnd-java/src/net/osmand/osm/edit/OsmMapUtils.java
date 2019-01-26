package net.osmand.osm.edit;

import net.osmand.data.LatLon;
import net.osmand.util.MapAlgorithms;
import net.osmand.util.MapUtils;

import java.util.Collection;

public class OsmMapUtils {

	private static double getDistance(Node e1, Node e2) {
		return MapUtils.getDistance(e1.getLatitude(), e1.getLongitude(), e2.getLatitude(), e2.getLongitude());
	}

	private static LatLon getWeightCenterForNodes(Collection<Node> nodes) {
		if (nodes.isEmpty()) {
			return null;
		}
		double longitude = 0;
		double latitude = 0;
		int count = 0;
		for (Node n : nodes) {
			if (n != null) {
				count++;
				longitude += n.getLongitude();
				latitude += n.getLatitude();
			}
		}
		if (count == 0) {
			return null;
		}
		return new LatLon(latitude / count, longitude / count);
	}
	
	public static LatLon getWeightCenterForWay(Way w) {
		Collection<Node> nodes = w.getNodes();
		if (nodes.isEmpty()) {
			return null;
		}
		boolean area = w.getFirstNodeId() == w.getLastNodeId();
		LatLon ll = area ? getMathWeightCenterForNodes(nodes) : getWeightCenterForNodes(nodes);
		if(ll == null) {
			return null;
		}
		double flat = ll.getLatitude();
		double flon = ll.getLongitude();
		if(!area || !MapAlgorithms.containsPoint(nodes, ll.getLatitude(), ll.getLongitude())) {
			double minDistance = Double.MAX_VALUE;
			for (Node n : nodes) {
				if (n != null) {
					double d = MapUtils.getDistance(n.getLatitude(), n.getLongitude(), ll.getLatitude(), ll.getLongitude());
					if(d < minDistance) {
						flat = n.getLatitude();
						flon = n.getLongitude();
						minDistance = d;
					}
				}
			}	
		}
		
		return new LatLon(flat, flon);
	}

	private static LatLon getMathWeightCenterForNodes(Collection<Node> nodes) {
		if (nodes.isEmpty()) {
			return null;
		}
		double longitude = 0;
		double latitude = 0;
		double sumDist = 0;
		Node prev = null;
		for (Node n : nodes) {
			if (n != null) {
				if (prev == null) {
					prev = n;
				} else {
					double dist = getDistance(prev, n);
					sumDist += dist;
					longitude += (prev.getLongitude() + n.getLongitude()) * dist / 2;
					latitude += (n.getLatitude() + n.getLatitude()) * dist / 2;
					prev = n;
				}
			}
		}
		if (sumDist == 0) {
			if (prev == null) {
				return null;
			}
			return prev.getLatLon();
		}
		return new LatLon(latitude / sumDist, longitude / sumDist);
	}

	// try to intersect from left to right
	public static double ray_intersect_lon(Node node, Node node2, double latitude, double longitude) {
		// a node below
		Node a = node.getLatitude() < node2.getLatitude() ? node : node2;
		// b node above
		Node b = a == node2 ? node : node2;
		if (latitude == a.getLatitude() || latitude == b.getLatitude()) {
			latitude += 0.00000001d;
		}
		if (latitude < a.getLatitude() || latitude > b.getLatitude()) {
			return -360d;
		} else {
			if (longitude < Math.min(a.getLongitude(), b.getLongitude())) {
				return -360d;
			} else {
				if (a.getLongitude() == b.getLongitude() && longitude == a.getLongitude()) {
					// the node on the boundary !!!
					return longitude;
				}
				// that tested on all cases (left/right)
				double lon = b.getLongitude() - (b.getLatitude() - latitude) * (b.getLongitude() - a.getLongitude())
						/ (b.getLatitude() - a.getLatitude());
				if (lon <= longitude) {
					return lon;
				} else {
					return -360d;
				}
			}
		}
	}
}