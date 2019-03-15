package net.osmand.plus.resources;

import net.osmand.ResultMatcher;
import net.osmand.data.Building;
import net.osmand.data.City;
import net.osmand.data.LatLon;
import net.osmand.data.MapObject;
import net.osmand.data.Street;

import java.util.List;

public interface RegionAddressRepository {
	String getName();
	String getCountryName();
	String getFileName() ;
	String getLang();
	boolean isTransliterateNames();
	LatLon getEstimatedRegionCenter();
	// is called on low memory
    void clearCache();
	void preloadCities(ResultMatcher<City> resultMatcher);
	void preloadBuildings(Street street, ResultMatcher<Building> resultMatcher);
	void preloadStreets(City o, ResultMatcher<Street> resultMatcher);
	List<City> getLoadedCities();
	// Returns city or postcode (if id < 0)
    City getCityById(long id, String name);
	Street getStreetByName(City cityOrPostcode, String name);
	List<Street> getStreetsIntersectStreets(Street st);
	void addCityToPreloadedList(City city);
	List<City> fillWithSuggestedCities(String name, ResultMatcher<City> resultMatcher, boolean searchVillagesMode);
	List<MapObject> searchMapObjectsByName(String name, ResultMatcher<MapObject> resultMatcher);
}