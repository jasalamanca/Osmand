package net.osmand.osm.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
}