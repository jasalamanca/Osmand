package net.osmand.osm.io;

import net.osmand.osm.edit.Entity;
import net.osmand.osm.edit.Entity.EntityId;

interface IOsmStorageFilter {
	
	boolean acceptEntityToLoad(OsmBaseStorage storage, EntityId entityId, Entity entity);

}
