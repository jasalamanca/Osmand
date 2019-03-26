package net.osmand.plus.mapmarkers.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;

public abstract class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_HEADER = 12;
	private static final int TYPE_ITEM = 13;

	private GroupsAdapterListener listener;
	final OsmandApplication app;
	final IconsCache iconsCache;

	GroupsAdapter(Context context) {
		this.app = (OsmandApplication) context.getApplicationContext();
		this.iconsCache = app.getIconsCache();
	}

	public void setAdapterListener(GroupsAdapterListener listener) {
		this.listener = listener;
	}

	@NonNull
    @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == TYPE_HEADER) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_markers_group_header, parent, false);
			return new MapMarkersGroupHeaderViewHolder(view);
		} else {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.markers_group_view_holder, parent, false);
			view.setOnClickListener(view1 -> {
                if (listener != null) {
                    listener.onItemClick(view1);
                }
            });
			return new MapMarkersGroupViewHolder(view);
		}
	}

	@Override
	public int getItemViewType(int position) {
		return position == 0 ? TYPE_HEADER : TYPE_ITEM;
	}

	public interface GroupsAdapterListener {
		void onItemClick(View view);
	}
}
