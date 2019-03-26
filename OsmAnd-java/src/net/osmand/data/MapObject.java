package net.osmand.data;

import net.osmand.Collator;
import net.osmand.OsmAndCollator;
import net.osmand.util.Algorithms;
import net.sf.junidecode.Junidecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MapObject implements Comparable<MapObject> {
	String name = null;
	String enName = null;
	/**
	 * Looks like: {ru=Москва, dz=མོསི་ཀོ...} and does not contain values of OSM tags "name" and "name:en",
	 * see {@link name} and {@link enName} respectively.
	 */
    private Map<String, String> names = null;
	private LatLon location = null;
	private int fileOffset = 0;
	Long id = null;
	private Object referenceFile = null;


	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		if (id != null) {
			return id;
		}
		return null;
	}

	public String getName() {
		if (name != null) {
			return name;
		}
		return ""; //$NON-NLS-1$
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setName(String lang, String name) {
		if (Algorithms.isEmpty(lang)) {
			setName(name);
		} else if (lang.equals("en")) {
			setEnName(name);
		} else {
			if (names == null) {
				names = new HashMap<>();
			}
			names.put(lang, name);
		}
	}

	public Map<String, String> getNamesMap(boolean includeEn) {
		if (!includeEn || Algorithms.isEmpty(enName)) {
			if (names == null) {
				return Collections.emptyMap();
			}
			return names;
		}
		Map<String, String> mp = new HashMap<>();
		if (names != null) {
			mp.putAll(names);
		}
		mp.put("en", enName);
		return mp;
	}

	public List<String> getAllNames() {
		List<String> l = new ArrayList<>();
		if (!Algorithms.isEmpty(enName)) {
			l.add(enName);
		}
		if (names != null) {
			l.addAll(names.values());
		}
		return l;
	}
	
	public List<String> getAllNames(boolean transliterate) {
		List<String> l = new ArrayList<>();
		String enName = getEnName(transliterate); 
		if (!Algorithms.isEmpty(enName)) {
			l.add(enName);
		}
		if (names != null) {
			l.addAll(names.values());
		}
		return l;
	}

	public String getName(String lang) {
		return getName(lang, false);
	}

	public String getName(String lang, boolean transliterate) {
		if (lang != null && lang.length() > 0) {
			if (lang.equals("en")) {
				// ignore transliterate option here for backward compatibility
				return getEnName(true);
			} else {
				// get name
				if (names != null) {
					String nm = names.get(lang);
					if (!Algorithms.isEmpty(nm)) {
						return nm;
					}
					if (transliterate) {
						return Junidecode.unidecode(getName());
					}
				}
			}
		}
		return getName();
	}

	public String getEnName(boolean transliterate) {
		if (!Algorithms.isEmpty(enName)) {
			return this.enName;
		} else if (!Algorithms.isEmpty(getName()) && transliterate) {
			return Junidecode.unidecode(getName());
		}
		return ""; //$NON-NLS-1$
	}

	public void setEnName(String enName) {
		this.enName = enName;
	}

	public LatLon getLocation() {
		return location;
	}

	public void setLocation(double latitude, double longitude) {
		location = new LatLon(latitude, longitude);
	}

	@Override
	public int compareTo(MapObject o) {
		return OsmAndCollator.primaryCollator().compare(getName(), o.getName());
	}

	public int getFileOffset() {
		return fileOffset;
	}
	public void setFileOffset(int fileOffset) {
		this.fileOffset = fileOffset;
	}
	public String toStringEn() {
		return getClass().getSimpleName() + ":" + getEnName(true);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + name + "(" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapObject other = (MapObject) obj;
		if (id == null) {
            return other.id == null;
		} else
		    return id.equals(other.id);
    }

	public static class MapObjectComparator implements Comparator<MapObject> {
		private final String l;
		final Collator collator = OsmAndCollator.primaryCollator();
		private final boolean transliterate;

        public MapObjectComparator(String lang, boolean transliterate) {
			this.l = lang;
			this.transliterate = transliterate;
		}

		@Override
		public int compare(MapObject o1, MapObject o2) {
			if (o1 == null ^ o2 == null) {
				return (o1 == null) ? -1 : 1;
			} else if (o1 == o2) {
				return 0;
			} else {
				return collator.compare(o1.getName(l, transliterate), o2.getName(l, transliterate));
			}
		}
    }
	
	public void setReferenceFile(Object referenceFile) {
		this.referenceFile = referenceFile;
	}
	public Object getReferenceFile() {
		return referenceFile;
	}
}