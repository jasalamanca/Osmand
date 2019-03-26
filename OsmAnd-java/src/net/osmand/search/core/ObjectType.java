package net.osmand.search.core;

public enum ObjectType {
	// ADDRESS
	CITY, VILLAGE, POSTCODE, STREET, HOUSE, STREET_INTERSECTION,
	// POI
	POI_TYPE, POI,
	// LOCATION
	LOCATION, PARTIAL_LOCATION,
	// UI OBJECTS
	FAVORITE, FAVORITE_GROUP, WPT, RECENT_OBJ,

	// ONLINE SEARCH
	ONLINE_SEARCH,
	
	REGION,

	SEARCH_STARTED,
	SEARCH_FINISHED,
	SEARCH_API_FINISHED,
	SEARCH_API_REGION_FINISHED,
	UNKNOWN_NAME_FILTER;

	public static boolean isAddress(ObjectType t) {
		return t == CITY || t == VILLAGE || t == POSTCODE || t == STREET || t == HOUSE || t == STREET_INTERSECTION;
	}

	public static ObjectType getExclusiveSearchType(ObjectType t) {
		if (t == FAVORITE_GROUP) {
			return FAVORITE;
		}
		return null;
	}
}