package net.osmand.plus.mapmarkers.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import net.osmand.plus.R;

public class MapMarkerHeaderViewHolder extends RecyclerView.ViewHolder {

	final ImageView icon;
	final View iconSpace;
	final TextView title;
	final Switch disableGroupSwitch;
	final View bottomShadow;

	MapMarkerHeaderViewHolder(View itemView) {
		super(itemView);
		icon = itemView.findViewById(R.id.icon);
		iconSpace = itemView.findViewById(R.id.icon_space);
		title = itemView.findViewById(R.id.title);
		disableGroupSwitch = itemView.findViewById(R.id.disable_group_switch);
		bottomShadow = itemView.findViewById(R.id.bottom_shadow);
	}
}
