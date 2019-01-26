package net.osmand.osm.edit;

import net.osmand.data.LatLon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Relation extends Entity {
	
	static class RelationMember {
		private final EntityId entityId;
		private Entity entity;
		private final String role;
		
		RelationMember(EntityId entityId, String role) {
			this.entityId = entityId;
			this.role = role;
		}

		EntityId getEntityId() {
			if(entityId == null && entity != null) {
				return EntityId.valueOf(entity);
			}
			return entityId;
		}

		public Entity getEntity() {
			return entity;
		}
		
		@Override
		public String toString() {
			return entityId.toString() + " " + role;
		}
	}
	
	// lazy loading
    private List<RelationMember> members = null;
	public Relation(long id) {
		super(id);
	}
	
	public void addMember(Long id, EntityType type, String role){
		if(members == null){
			members = new ArrayList<>(); 
		}
		members.add(new RelationMember(new EntityId(type, id), role));
	}

	public List<RelationMember> getMembers() {
		if(members == null){
			return Collections.emptyList();
		}
		return members;
	}

	@Override
	public void initializeLinks(Map<EntityId, Entity> entities){
		if (members != null) {
			for(RelationMember rm : members) {
				if(rm.entityId != null && entities.containsKey(rm.entityId)) {
					rm.entity = entities.get(rm.entityId);
				}
			}
		}
	}
	
	@Override
	public LatLon getLatLon() {
		return null;
	}
}
