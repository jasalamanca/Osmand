package net.osmand.osm.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.osmand.data.LatLon;
import net.osmand.osm.edit.Entity.EntityId;
import net.osmand.osm.edit.Relation.RelationMember;

public class Relation extends Entity {
	
	public static class RelationMember {
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
		
		public String getRole() {
			return role;
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
	
	public List<RelationMember> getMembers(String role) {
		if (members == null) {
			return Collections.emptyList();
		}
		if (role == null) {
			return members;
		}
		List<RelationMember> l = new ArrayList<>();
		for (RelationMember m : members) {
			if (role.equals(m.role)) {
				l.add(m);
			}
		}
		return l;
	}
	
	public List<Entity> getMemberEntities(String role) {
		if (members == null) {
			return Collections.emptyList();
		}
		List<Entity> l = new ArrayList<>();
		for (RelationMember m : members) {
			if (role == null || role.equals(m.role)) {
				if(m.entity != null) {
					l.add(m.entity);
				}
			}
		}
		return l;
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

	public boolean remove(EntityId key) {
		if(members != null) {
			Iterator<RelationMember> it = members.iterator();
			while(it.hasNext()) {
				RelationMember rm = it.next();
				if(key.equals(rm.getEntityId())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean remove(RelationMember key) {
		if(members != null) {
			Iterator<RelationMember> it = members.iterator();
			while(it.hasNext()) {
				RelationMember rm = it.next();
				if(rm == key) {
					it.remove();
					return true;
				}
			}
		}
		return false;
	}

}
