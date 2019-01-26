package net.osmand.plus.mapmarkers.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.osmand.plus.R;

class MapMarkersShowHideHistoryViewHolder extends RecyclerView.ViewHolder {

	final TextView title;
	final View bottomShadow;

	public MapMarkersShowHideHistoryViewHolder(View itemView) {
		super(itemView);
		title = itemView.findViewById(R.id.show_hide_history_title);
		bottomShadow = itemView.findViewById(R.id.bottom_shadow);
	}
}
