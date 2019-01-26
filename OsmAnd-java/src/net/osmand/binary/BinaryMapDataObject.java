package net.osmand.binary;

import net.osmand.binary.BinaryMapIndexReader.MapIndex;
import net.osmand.render.RenderingRulesStorage;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

public class BinaryMapDataObject {
	int[] coordinates = null;
	int[][] polygonInnerCoordinates = null;
	boolean area = false;
	int[] types = null;
	int[] additionalTypes = null;
	private int objectType = RenderingRulesStorage.POINT_RULES;
	
	TIntObjectHashMap<String> objectNames = null;
	TIntArrayList namesOrder = null;
	long id = 0;
	MapIndex mapIndex = null;

	public BinaryMapDataObject(){
	}

	public BinaryMapDataObject(long id, int[] coordinates, int[][] polygonInnerCoordinates, int objectType, boolean area, 
			int[] types, int[] additionalTypes){
		this.polygonInnerCoordinates = polygonInnerCoordinates;
		this.coordinates = coordinates;
		this.additionalTypes = additionalTypes;
		this.types = types;
		this.id = id;
		this.objectType = objectType;
		this.area = area;
	}

	public String getName(){
		if(objectNames == null){
			return "";
		}
		String name = objectNames.get(mapIndex.nameEncodingType);
		if(name == null){
			return "";
		}
		return name;
	}
	
	public TIntObjectHashMap<String> getObjectNames() {
		return objectNames;
	}

	public void putObjectName(int type, String name){
		if(objectNames == null){
			objectNames = new TIntObjectHashMap<>();
			namesOrder = new TIntArrayList();
		}
		objectNames.put(type, name);
		namesOrder.add(type);
	}
	
	public int[][] getPolygonInnerCoordinates() {
		return polygonInnerCoordinates;
	}
	
	public int[] getTypes(){
		return types;
	}
	
	public boolean containsType(int cachedType) {
		if(cachedType != -1) {
            for (int type : types) {
                if (type == cachedType) {
                    return true;
                }
            }
		}
		return false;
	}
	
	public boolean containsAdditionalType(int cachedType) {
		if (cachedType != -1) {
            for (int additionalType : additionalTypes) {
                if (additionalType == cachedType) {
                    return true;
                }
            }
		}
		return false;
	}
	
	public String getNameByType(int type) {
		if(type != -1 && objectNames != null) {
			return objectNames.get(type);
		}
		return null;
	}
	
	public int[] getAdditionalTypes() {
		return additionalTypes;
	}
	public void setArea(boolean area) {
		this.area = area;
	}
	public long getId() {
		return id;
	}
	void setId(long id) {
		this.id = id;
	}
	public TIntArrayList getNamesOrder() {
		return namesOrder;
	}
	public MapIndex getMapIndex() {
		return mapIndex;
	}
	public void setMapIndex(MapIndex mapIndex) {
		this.mapIndex = mapIndex;
	}
	
	public int getPointsLength(){
		if(coordinates == null){
			return 0;
		}
		return coordinates.length / 2;
	}
	public int getPoint31YTile(int ind) {
		return coordinates[2 * ind + 1];
	}
	public int getPoint31XTile(int ind) {
		return coordinates[2 * ind];
	}
}
