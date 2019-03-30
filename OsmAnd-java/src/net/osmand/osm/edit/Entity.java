package net.osmand.osm.edit;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class Entity implements Serializable {
	public enum EntityType {
		NODE,
		WAY,
		RELATION;

		static EntityType valueOf(Entity e) {
			if (e instanceof Node) {
				return NODE;
			} else if (e instanceof Way) {
				return WAY;
			} else if (e instanceof Relation) {
				return RELATION;
			}
			return null;
		}
	}

	public static class EntityId {
		private final EntityType type;
		private final Long id;

		public EntityId(EntityType type, Long id) {
			this.type = type;
			this.id = id;
		}

		static EntityId valueOf(Entity e) {
			return new EntityId(EntityType.valueOf(e), e.getId());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		@Override
		public String toString() {
			return type + " " + id; //$NON-NLS-1$
		}

		public Long getId() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntityId other = (EntityId) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (type == null)
				return other.type == null;
			else
				return type.equals(other.type);
		}
	}

	// lazy initializing
	private Map<String, String> tags = null;
	private Set<String> changedTags;
	private final long id;
	private boolean dataLoaded;

	Entity(long id) {
		this.id = id;
	}

	Entity(Entity copy, long id) {
		this.id = id;
		for (String t : copy.getTagKeySet()) {
			putTagNoLC(t, copy.getTag(t));
		}
		this.dataLoaded = copy.dataLoaded;
	}

	public Set<String> getChangedTags() {
		return changedTags;
	}
	public void setChangedTags(Set<String> changedTags) {
		this.changedTags = changedTags;
	}
	public long getId() {
		return id;
	}

	public String removeTag(String key) {
		if (tags != null) {
			return tags.remove(key);
		}
		return null;
	}

	//NOTE jsala usado en C++
	public String putTag(String key, String value) {
		return putTagNoLC(key.toLowerCase(), value);
	}

	public String putTagNoLC(String key, String value) {
		if (tags == null) {
			tags = new LinkedHashMap<>();
		}
		return tags.put(key, value);
	}

	public void replaceTags(Map<String, String> toPut) {
		tags = new LinkedHashMap<>(toPut);
	}

	public String getTag(String key) {
		if (tags == null) {
			return null;
		}
		return tags.get(key);
	}

	public Map<String, String> getTags() {
		if (tags == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(tags);
	}

	public Collection<String> getTagKeySet() {
		if (tags == null) {
			return Collections.emptyList();
		}
		return tags.keySet();
	}

	@Override
	public String toString() {
		return EntityId.valueOf(this).toString();
	}

	@Override
	public int hashCode() {
		if (id < 0) {
			return System.identityHashCode(this);
		}
		return (int) id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (id != other.id)
			return false;
		// virtual are not equal
        return id >= 0;
    }
}