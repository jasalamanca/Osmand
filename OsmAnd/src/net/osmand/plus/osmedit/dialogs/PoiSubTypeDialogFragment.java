package net.osmand.plus.osmedit.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import net.osmand.osm.MapPoiTypes;
import net.osmand.osm.PoiCategory;
import net.osmand.osm.PoiType;
import net.osmand.plus.OsmandApplication;

import java.util.Set;
import java.util.TreeSet;

public class PoiSubTypeDialogFragment extends DialogFragment {
	private static final String KEY_POI_CATEGORY = "amenity";
	private OnItemSelectListener onItemSelectListener;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MapPoiTypes poiTypes = ((OsmandApplication) getActivity().getApplication()).getPoiTypes();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final PoiCategory a = poiTypes.getPoiCategoryByName((String) getArguments().getSerializable(KEY_POI_CATEGORY));
		Set<String> strings = new TreeSet<>();
		if(a == poiTypes.getOtherPoiCategory()) {
			for (PoiCategory category : poiTypes.getCategories(false)) {
				if (!category.isNotEditableOsm()) {
					addCategory(category, strings);
				}
			}
		} else {
			addCategory(a, strings);
		}
		final String[] subCats = strings.toArray(new String[strings.size()]);
		builder.setItems(subCats, (dialog, which) -> {
			onItemSelectListener.select(subCats[which]);
			dismiss();
		});
		return builder.create();
	}

	private void addCategory(final PoiCategory a, Set<String> strings) {
		for (PoiType s : a.getPoiTypes()) {
			if (!s.isReference() && !s.isNotEditableOsm() && s.getBaseLangType() == null) {
				strings.add(s.getTranslation());
			}
		}
	}

	public static PoiSubTypeDialogFragment createInstance(PoiCategory cat) {
		PoiSubTypeDialogFragment fragment = new PoiSubTypeDialogFragment();
		Bundle args = new Bundle();
		args.putSerializable(KEY_POI_CATEGORY, cat.getKeyName());
		fragment.setArguments(args);
		return fragment;
	}

	public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
		this.onItemSelectListener = onItemSelectListener;
	}

	public interface OnItemSelectListener {
		void select(String category);
	}
}