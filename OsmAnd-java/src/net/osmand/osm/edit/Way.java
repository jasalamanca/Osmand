package net.osmand.osm.edit;

import net.osmand.data.LatLon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import gnu.trove.list.array.TLongArrayList;

public class Way extends Entity {
	// lazy loading
	private TLongArrayList nodeIds = null;
	private List<Node> nodes = null;

	public Way(long id) {
		super(id);
	}

	public void addNode(long id) {
		if (nodeIds == null) {
			nodeIds = new TLongArrayList();
		}
		nodeIds.add(id);
	}

	long getFirstNodeId() {
		if (nodeIds == null) {
			return -1;
		}
		return nodeIds.get(0);
	}

	long getLastNodeId() {
		if (nodeIds == null) {
			return -1;
		}
		return nodeIds.get(nodeIds.size() - 1);
	}

	public void addNode(Node n) {
		if (nodeIds == null) {
			nodeIds = new TLongArrayList();
		}
		if (nodes == null) {
			nodes = new ArrayList<>();
		}
		nodeIds.add(n.getId());
		nodes.add(n);
	}

	public List<Node> getNodes() {
		if (nodes == null) {
			return Collections.emptyList();
		}
		return nodes;
	}

	@Override
	public void initializeLinks(Map<EntityId, Entity> entities) {
		if (nodeIds != null) {
			if (nodes == null) {
				nodes = new ArrayList<>();
			} else {
				nodes.clear();
			}
			int nIsize = nodeIds.size();
			for (int i = 0; i < nIsize; i++) {
				nodes.add((Node) entities.get(new EntityId(EntityType.NODE, nodeIds.get(i))));
			}
		}
	}

	@Override
	public LatLon getLatLon() {
		if (nodes == null) {
			return null;
		}
		return OsmMapUtils.getWeightCenterForWay(this);
	}
}