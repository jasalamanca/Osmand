package net.osmand.data;

public class QuadPointDouble {
	public double x;
	public double y;

	public QuadPointDouble(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public QuadPointDouble(QuadPointDouble a) {
		this(a.x, a.y);
	}
}