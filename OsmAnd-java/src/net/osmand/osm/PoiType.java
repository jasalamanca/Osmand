package net.osmand.osm;

import java.util.LinkedHashSet;
import java.util.Map;

public class PoiType extends AbstractPoiType {
	private final PoiCategory category;
	private final PoiFilter filter;
	private AbstractPoiType parentType;
	private PoiType referenceType;
	private String osmTag;
	private String osmTag2;
	private String osmValue;
	private String osmValue2;
	private boolean filterOnly;

//	private String nameTag;
	private boolean text;
	private boolean nameOnly;
	private boolean relation;
	private int order = 90;

	public PoiType(MapPoiTypes poiTypes, PoiCategory category, PoiFilter filter, String name) {
		super(name, poiTypes);
		this.category = category;
		this.filter = filter;
	}

	PoiType getReferenceType() {
		return referenceType;
	}
	
	void setReferenceType(PoiType referenceType) {
		this.referenceType = referenceType;
	}
	
	public boolean isReference() {
		return referenceType != null;
	}

	public String getOsmTag() {
		if(isReference()) {
			return referenceType.getOsmTag();
		}
		if(osmTag != null && osmTag.startsWith("osmand_amenity")) {
			return "amenity";
		}
		return osmTag;
	}

	void setOsmTag(String osmTag) {
		this.osmTag = osmTag;
	}

	public String getOsmTag2() {
		if(isReference()) {
			return referenceType.getOsmTag2();
		}
		return osmTag2;
	}

	void setOsmTag2(String osmTag2) {
		this.osmTag2 = osmTag2;
	}

	public String getOsmValue() {
		if(isReference()) {
			return referenceType.getOsmValue();
		}
		return osmValue;
	}

	void setOsmValue(String osmValue) {
		this.osmValue = osmValue;
	}

	public String getOsmValue2() {
		if(isReference()) {
			return referenceType.getOsmValue2();
		}
		return osmValue2;
	}

	void setOsmValue2(String osmValue2) {
		this.osmValue2 = osmValue2;
	}
	public boolean isFilterOnly() {
		return filterOnly;
	}
	void setFilterOnly(boolean filterOnly) {
		this.filterOnly = filterOnly;
	}
	public PoiCategory getCategory() {
		return category;
	}
	public PoiFilter getFilter() {
		return filter;
	}

	public Map<PoiCategory, LinkedHashSet<String>> putTypes(Map<PoiCategory, LinkedHashSet<String>> acceptedTypes) {
		if (isAdditional()) {
			return parentType.putTypes(acceptedTypes);
		}
		PoiType rt = getReferenceType();
		PoiType poiType = rt != null ? rt : this;
		if (!acceptedTypes.containsKey(poiType.category)) {
			acceptedTypes.put(poiType.category, new LinkedHashSet<String>());
		}
		LinkedHashSet<String> set = acceptedTypes.get(poiType.category);
		if(set != null) {
			set.add(poiType.getKeyName());
		}
		return acceptedTypes;
	}

    public void setAdditional(AbstractPoiType parentType) {
        this.parentType = parentType;
    }
    public boolean isAdditional(){
        return parentType != null;
    }
    public AbstractPoiType getParentType() {
        return parentType;
    }
    public boolean isText() {
    	return text;
    }
    public void setText(boolean text) {
    	this.text = text;
    }
//	public void setNameTag(String nameTag) {
//		this.nameTag = nameTag;
//	}
	void setNameOnly(boolean nameOnly) {
		this.nameOnly = nameOnly;
	}
	public void setRelation(boolean relation) {
		this.relation = relation;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return "PoiType{" +
				"category=" + category +
				", parentType=" + parentType +
				", referenceType=" + referenceType +
				", osmTag='" + osmTag + '\'' +
				", osmTag2='" + osmTag2 + '\'' +
				", osmValue='" + osmValue + '\'' +
				", osmValue2='" + osmValue2 + '\'' +
				", text=" + text +
				", nameOnly=" + nameOnly +
				", relation=" + relation +
				", order=" + order +
				'}';
	}
}