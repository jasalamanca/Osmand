package net.osmand.util;

import java.util.Collection;
import java.util.List;

import gnu.trove.list.TLongList;
import net.osmand.data.LatLon;
import net.osmand.osm.edit.Node;
import net.osmand.osm.edit.OsmMapUtils;

public class MapAlgorithms {
	
	public static boolean isClockwiseWay(TLongList c) {
		if (c.size() == 0) {
			return true;
		}

		// calculate middle Y
		long mask = 0xffffffffL;
		long middleY = 0;
		for (int i = 0; i < c.size(); i++) {
			middleY =  middleY + (c.get(i) & mask);
		}
		middleY = middleY /(long) c.size();

		double clockwiseSum = 0;

		boolean firstDirectionUp = false;
		int previousX = Integer.MIN_VALUE;
		int firstX = Integer.MIN_VALUE;

		int prevX = (int) (c.get(0) >> 32);
		int prevY = (int) (c.get(0) & mask);

		for (int i = 1; i < c.size(); i++) {
			int x = (int) (c.get(i) >> 32);
			int y = (int) (c.get(i) & mask);
			int rX = ray_intersect_x(prevX, prevY, x, y, (int) middleY);
			if (rX != Integer.MIN_VALUE) {
				boolean skipSameSide = (y <= middleY) == (prevY <= middleY);
				if (skipSameSide) {
					continue;
				}
				boolean directionUp = prevY >= middleY;
				if (firstX == Integer.MIN_VALUE) {
					firstDirectionUp = directionUp;
					firstX = rX;
				} else {
					boolean clockwise = (!directionUp) == (previousX < rX);
					if (clockwise) {
						clockwiseSum += Math.abs(previousX - rX);
					} else {
						clockwiseSum -= Math.abs(previousX - rX);
					}
				}
				previousX = rX;
			}
			prevX = x;
			prevY = y;
		}
		if (firstX != Integer.MIN_VALUE) {
			boolean clockwise = (!firstDirectionUp) == (previousX < firstX);
			if (clockwise) {
				clockwiseSum += Math.abs(previousX - firstX);
			} else {
				clockwiseSum -= Math.abs(previousX - firstX);
			}
		}

		return clockwiseSum >= 0;
	}

	public static int ray_intersect_x(int prevX, int prevY, int x, int y, int middleY) {
		// prev node above line
		// x,y node below line
		if (prevY > y) {
			int tx = x;
			int ty = y;
			x = prevX;
			y = prevY;
			prevX = tx;
			prevY = ty;
		}
		if (y == middleY || prevY == middleY) {
			middleY -= 1;
		}
		if (prevY > middleY || y < middleY) {
			return Integer.MIN_VALUE;
		} else {
			if (y == prevY) {
				// the node on the boundary !!!
				return x;
			}
			// that tested on all cases (left/right)
			double rx = x + ((double) middleY - y) * ((double) x - prevX) / (((double) y - prevY));
			return (int) rx;
		}
	}

	private static long combine2Points(int x, int y) {
		return (((long) x ) <<32) | ((long)y );
	}
	/**
	 * outx,outy are the coordinates out of the box 
	 * inx,iny are the coordinates from the box (NOT IMPORTANT in/out, just one should be in second out)
	 * @return -1 if there is no instersection or x<<32 | y
	 */
	public static long calculateIntersection(int inx, int iny, int outx, int outy, int leftX, int rightX, int bottomY, int topY) {
		int by = -1;
		int bx = -1;
		// firstly try to search if the line goes in
		if (outy < topY && iny >= topY) {
			int tx = (int) (outx + ((double) (inx - outx) * (topY - outy)) / (iny - outy));
			if (leftX <= tx && tx <= rightX) {
				bx = tx;
				by = topY;
				return combine2Points(bx, by);
			}
		}
		if (outy > bottomY && iny <= bottomY) {
			int tx = (int) (outx + ((double) (inx - outx) * (outy - bottomY)) / (outy - iny));
			if (leftX <= tx && tx <= rightX) {
				bx = tx;
				by = bottomY;
				return combine2Points(bx, by);
			}
		}
		if (outx < leftX && inx >= leftX) {
			int ty = (int) (outy + ((double) (iny - outy) * (leftX - outx)) / (inx - outx));
			if (ty >= topY && ty <= bottomY) {
				by = ty;
				bx = leftX;
				return combine2Points(bx, by);
			}
		}
		if (outx > rightX && inx <= rightX) {
			int ty = (int) (outy + ((double) (iny - outy) * (outx - rightX)) / (outx - inx));
			if (ty >= topY && ty <= bottomY) {
				by = ty;
				bx = rightX;
				return combine2Points(bx, by);
			}
		}

		// try to search if point goes out
		if (outy > topY && iny <= topY) {
			int tx = (int) (outx + ((double) (inx - outx) * (topY - outy)) / (iny - outy));
			if (leftX <= tx && tx <= rightX) {
				bx = tx;
				by = topY;
				return combine2Points(bx, by);
			}
		}
		if (outy < bottomY && iny >= bottomY) {
			int tx = (int) (outx + ((double) (inx - outx) * (outy - bottomY)) / (outy - iny));
			if (leftX <= tx && tx <= rightX) {
				bx = tx;
				by = bottomY;
				return combine2Points(bx, by);
			}
		}
		if (outx > leftX && inx <= leftX) {
			int ty = (int) (outy + ((double) (iny - outy) * (leftX - outx)) / (inx - outx));
			if (ty >= topY && ty <= bottomY) {
				by = ty;
				bx = leftX;
				return combine2Points(bx, by);
			}
		}
		if (outx < rightX && inx >= rightX) {
			int ty = (int) (outy + ((double) (iny - outy) * (outx - rightX)) / (outx - inx));
			if (ty >= topY && ty <= bottomY) {
				by = ty;
				bx = rightX;
				return combine2Points(bx, by);
			}
		}

		return -1L;
	}

	public static boolean containsPoint(Collection<Node> polyNodes, double latitude, double longitude){
		return  countIntersections(polyNodes, latitude, longitude) % 2 == 1;
	}
	
	/**
	 * count the intersections when going from lat, lon to outside the ring
	 * @param polyNodes2 
	 */
	private static int countIntersections(Collection<Node> polyNodes, double latitude, double longitude) {
		int intersections = 0;
		if (polyNodes.size() == 0)
			return 0;
		Node prev = null;
		Node first = null;
		Node last = null;
		for(Node n  : polyNodes) {
			if(prev == null) {
				prev = n;
				first = prev;
				continue;
			}
			if(n == null) {
				continue;
			}
			last = n;
			if (OsmMapUtils.ray_intersect_lon(prev,
					n, latitude, longitude) != -360.0d) {
				intersections++;
			}
			prev = n;
		}
		if(first == null || last == null) {
			return 0;
		}
		// special handling, also count first and last, might not be closed, but
		// we want this!
		if (OsmMapUtils.ray_intersect_lon(first,
				last, latitude, longitude) != -360.0d) {
			intersections++;
		}
		return intersections;
	}
}