package net.osmand.plus.base;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;

public abstract class OsmAndListFragment extends ListFragment {
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setBackgroundColor(
				getResources().getColor(
						getMyApplication().getSettings().isLightContent() ? R.color.bg_color_light
								: R.color.bg_color_dark));
	}

	protected OsmandApplication getMyApplication() {
		return (OsmandApplication)getActivity().getApplication();
	}
}