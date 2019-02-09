package net.osmand.osm.edit;

/**
 * Additional entity info
 */
public class EntityInfo {
	private String timestamp;
	private String uid;
	private String user;
	private String visible;
	private String version;
	private String changeset;

	public EntityInfo() {
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getVisible() {
		return visible;
	}
	public void setVisible(String visible) {
		this.visible = visible;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public void setChangeset(String changeset) {
		this.changeset = changeset;
	}
}
