package net.osmand.data;

import android.content.Context;


public interface LocationPoint {
	double getLatitude();
	double getLongitude();
	int getColor();
	PointDescription getPointDescription(Context ctx);
}
