package net.osmand.osm.edit;

import java.util.ArrayList;
import java.util.List;

public class Relation extends Entity {
	static class RelationMember {
		private final EntityId entityId;
//		private Entity entity;
		private final String role;
		
		RelationMember(EntityId entityId, String role) {
			this.entityId = entityId;
			this.role = role;
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

}