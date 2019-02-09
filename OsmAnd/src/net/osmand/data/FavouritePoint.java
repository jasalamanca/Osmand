package net.osmand.data;

import android.content.Context;

import java.io.Serializable;

public class FavouritePoint implements Serializable, LocationPoint {
	private static final long serialVersionUID = 729654300829771466L;
	private String name = "";
	private String description;
	private String category = "";
	private String originObjectName = "";
	private double latitude;
	private double longitude;
	private int color;
	private boolean visible = true;

	public FavouritePoint(double latitude, double longitude, String name, String category) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.category = category;
		if(name == null) {
			name = "";
		}
		this.name = name;
	}

	public int getColor() {
		return color;
	}
	public PointDescription getPointDescription() {
		return new PointDescription(PointDescription.POINT_TYPE_FAVORITE, name);
	}
	@Override
	public PointDescription getPointDescription(Context ctx) {
		return getPointDescription();
	}
	public void setColor(int color) {
		this.color = color;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public String getOriginObjectName() {
		return originObjectName;
	}
	public void setOriginObjectName(String originObjectName) {
		this.originObjectName = originObjectName;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription () {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "Favourite " + getName(); //$NON-NLS-1$
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof FavouritePoint)) return false;

		FavouritePoint fp = (FavouritePoint)o;

		return (this.latitude == fp.latitude)
				   && (this.longitude == fp.longitude)
				   && (this.name.equals(fp.name));
	}

	@Override
	public int hashCode() {
		int hash = (int)latitude*1000 + (int)longitude*1000;
		hash += (name != null) ? name.hashCode() : 0;
		return hash;
	}
}