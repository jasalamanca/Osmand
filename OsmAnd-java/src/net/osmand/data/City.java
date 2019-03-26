package net.osmand.data;

import java.util.ArrayList;
import java.util.List;


public class City extends MapObject {
	public enum CityType {
		// that's tricky way to play with that numbers (to avoid including suburbs in city & vice verse)
		// district special type and it is not registered as a city
		CITY(10000), TOWN(4000), VILLAGE(1300), HAMLET(1000), SUBURB(400), DISTRICT(400), NEIGHBOURHOOD(300);

		private final double radius;

		CityType(double radius) {
			this.radius = radius;
		}
		public double getRadius() {
			return radius;
		}
	}

	private CityType type = null;
	private final List<Street> listOfStreets = new ArrayList<>();
	private City closestCity = null;
	
	private static long POSTCODE_INTERNAL_ID = -1000;
	public static City createPostcode(String postcode){
		return new City(postcode, POSTCODE_INTERNAL_ID--);
	}

	public City(CityType type) {
		if (type == null) {
			throw new NullPointerException();
		}
		this.type = type;
	}
	
	private City(String postcode, long id) {
		this.type = null;
		this.name = this.enName = postcode;
		this.id = id;
	}

	public boolean isPostcode(){
		return type == null;
	}
	public City getClosestCity() {
		return closestCity;
	}
	public void setClosestCity(City closestCity) {
		this.closestCity = closestCity;
	}
	public void registerStreet(Street street) {
		listOfStreets.add(street);
	}
	public CityType getType() {
		return type;
	}
	public List<Street> getStreets() {
		return listOfStreets;
	}

	@Override
	public String toString() {
		if (isPostcode()) {
			return "Postcode : " + getName() + " " + getLocation(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return "City [" + type + "] " + getName() + " " + getLocation(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}