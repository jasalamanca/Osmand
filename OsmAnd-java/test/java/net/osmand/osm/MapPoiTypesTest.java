package net.osmand.osm;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

class MapPoiTypesTest
{
	public MapPoiTypesTest() {
	}

	private static void print(MapPoiTypes df) {
		List<PoiCategory> pc = df.getCategories(true);
		for (PoiCategory p : pc) {
			System.out.println("Category " + p.getKeyName());
			for (PoiFilter f : p.getPoiFilters()) {
				System.out.println(" Filter " + f.getKeyName());
				print("  ", f);
			}
			print(" ", p);
		}
	}

	private static void print(String indent, PoiFilter f) {
		for (PoiType pt : f.getPoiTypes()) {
			System.out.println(indent + " Type " + pt.getKeyName() +
					(pt.isReference() ? (" -> " + pt.getReferenceType().getCategory().getKeyName()) : ""));
		}
	}

	public static void main(String[] args) {
		MapPoiTypes DEFAULT_INSTANCE = new MapPoiTypes("resources/poi/poi_types.xml");
		DEFAULT_INSTANCE.init();
		print(DEFAULT_INSTANCE)	;
		System.out.println("-----------------");
		List<PoiFilter> lf = DEFAULT_INSTANCE.getTopVisibleFilters();
		for (PoiFilter l : lf) {
			System.out.println("----------------- " + l.getKeyName());
			print("", l);
			Map<PoiCategory, LinkedHashSet<String>> m = l.putTypes(new LinkedHashMap<>());
			System.out.println(m);
		}
	}
}