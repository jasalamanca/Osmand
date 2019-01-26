package net.osmand.plus.search.listitems;

import net.osmand.plus.OsmandApplication;

public class QuickSearchSelectAllListItem extends QuickSearchListItem {

	private final String name;

	public QuickSearchSelectAllListItem(OsmandApplication app, String name) {
		super(app, null);
		this.name = name;
	}

	public QuickSearchListItemType getType() {
		return QuickSearchListItemType.SELECT_ALL;
	}

	@Override
	public String getName() {
		return name;
	}
}
