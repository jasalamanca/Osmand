package net.osmand.search.core;

import net.osmand.ResultMatcher;
import net.osmand.binary.BinaryMapIndexReader.SearchPoiTypeFilter;
import net.osmand.data.Amenity;

public interface CustomSearchPoiFilter extends SearchPoiTypeFilter {

	String getName();
	
	Object getIconResource();
	
	ResultMatcher<Amenity> wrapResultMatcher(final ResultMatcher<Amenity> matcher);
	
}
