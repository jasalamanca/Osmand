package net.osmand.plus.search;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import net.osmand.osm.PoiCategory;
import net.osmand.osm.PoiType;
import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.poi.PoiFiltersHelper;
import net.osmand.plus.poi.PoiUIFilter;
import net.osmand.plus.render.RenderingIcons;
import net.osmand.util.Algorithms;

import java.text.Collator;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class QuickSearchCustomPoiFragment extends DialogFragment {
	private static final String TAG = "QuickSearchCustomPoiFragment";
	private static final String QUICK_SEARCH_CUSTOM_POI_FILTER_ID_KEY = "quick_search_custom_poi_filter_id_key";

	private ListView listView;
	private CategoryListAdapter listAdapter;
	private String filterId;
	private PoiUIFilter filter;
	private PoiFiltersHelper helper;
	private View bottomBarShadow;
	private View bottomBar;
	private TextView barTitle;
	private boolean editMode;

	public QuickSearchCustomPoiFragment() {
	}

	private OsmandApplication getMyApplication() {
		return (OsmandApplication) getActivity().getApplication();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean isLightTheme = getMyApplication().getSettings().OSMAND_THEME.get() == OsmandSettings.OSMAND_LIGHT_THEME;
		int themeId = isLightTheme ? R.style.OsmandLightTheme : R.style.OsmandDarkTheme;
		setStyle(STYLE_NO_FRAME, themeId);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		final OsmandApplication app = getMyApplication();
		helper = app.getPoiFilters();
		if (getArguments() != null) {
			filterId = getArguments().getString(QUICK_SEARCH_CUSTOM_POI_FILTER_ID_KEY);
		} else if (savedInstanceState != null) {
			filterId = savedInstanceState.getString(QUICK_SEARCH_CUSTOM_POI_FILTER_ID_KEY);
		}
		if (filterId != null) {
			filter = helper.getFilterById(filterId);
		}
		if (filter == null) {
			filter = helper.getCustomPOIFilter();
			filter.clearFilter();
		}
		editMode = !filterId.equals(helper.getCustomPOIFilter().getFilterId());

		View view = inflater.inflate(R.layout.search_custom_poi, container, false);

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setNavigationIcon(app.getIconsCache().getIcon(R.drawable.ic_action_remove_dark));
		toolbar.setNavigationContentDescription(R.string.shared_string_close);
		toolbar.setNavigationOnClickListener(v -> dismiss());

		TextView title = view.findViewById(R.id.title);
		if (editMode) {
			title.setText(filter.getName());
		}

		listView = view.findViewById(android.R.id.list);
		listView.setBackgroundColor(getResources().getColor(
				app.getSettings().isLightContent() ? R.color.ctx_menu_info_view_bg_light
						: R.color.ctx_menu_info_view_bg_dark));

		View header = inflater.inflate(R.layout.list_shadow_header, null);
		listView.addHeaderView(header, null, false);
		View footer = inflater.inflate(R.layout.list_shadow_footer, listView, false);
		listView.addFooterView(footer, null, false);
		listAdapter = new CategoryListAdapter(app, app.getPoiTypes().getCategories(false));
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener((parent, view1, position, id) -> {
			PoiCategory category = listAdapter.getItem(position - listView.getHeaderViewsCount());
			showDialog(category, false);
		});

		bottomBarShadow = view.findViewById(R.id.bottomBarShadow);
		bottomBar = view.findViewById(R.id.bottomBar);
		barTitle = view.findViewById(R.id.barTitle);
		bottomBar.setOnClickListener(v -> {
			dismiss();
			QuickSearchDialogFragment quickSearchDialogFragment = getQuickSearchDialogFragment();
			if (quickSearchDialogFragment != null) {
				quickSearchDialogFragment.showFilter(filterId);
			}
		});

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(QUICK_SEARCH_CUSTOM_POI_FILTER_ID_KEY, filterId);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (editMode) {
			QuickSearchDialogFragment quickSearchDialogFragment = getQuickSearchDialogFragment();
			if (quickSearchDialogFragment != null) {
				getMyApplication().getSearchUICore().refreshCustomPoiFilters();
				quickSearchDialogFragment.replaceQueryWithUiFilter(filter, "");
				quickSearchDialogFragment.reloadCategories();
			}
		}
		super.onDismiss(dialog);
	}

	private QuickSearchDialogFragment getQuickSearchDialogFragment() {
		Fragment parent = getParentFragment();
		if (parent instanceof QuickSearchDialogFragment) {
			return (QuickSearchDialogFragment) parent;
		} else if (parent instanceof QuickSearchPoiFilterFragment
				&& parent.getParentFragment() instanceof QuickSearchDialogFragment) {
			return (QuickSearchDialogFragment) parent.getParentFragment();
		} else {
			return null;
		}
	}

	private int getIconId(PoiCategory category) {
		String id = null;
		if (category != null) {
			if (RenderingIcons.containsBigIcon(category.getIconKeyName())) {
				id = category.getIconKeyName();
			}
		}
		if (id != null) {
			return RenderingIcons.getBigIconResourceId(id);
		} else {
			return 0;
		}
	}

	public static void showDialog(DialogFragment parentFragment, String filterId) {
		Bundle bundle = new Bundle();
		if (filterId != null) {
			bundle.putString(QUICK_SEARCH_CUSTOM_POI_FILTER_ID_KEY, filterId);
		}
		QuickSearchCustomPoiFragment fragment = new QuickSearchCustomPoiFragment();
		fragment.setArguments(bundle);
		fragment.show(parentFragment.getChildFragmentManager(), TAG);
	}

	private class CategoryListAdapter extends ArrayAdapter<PoiCategory> {
		private final OsmandApplication app;

		CategoryListAdapter(OsmandApplication app, List<PoiCategory> items) {
			super(app, R.layout.list_item_icon24_and_menu, items);
			this.app = app;
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row = convertView;
			if (row == null) {
				row = inflater.inflate(R.layout.list_item_icon24_and_menu, parent, false);
			}
			PoiCategory category = getItem(position);
			if (category != null) {
				ImageView iconView = row.findViewById(R.id.icon);
				ImageView secondaryIconView = row.findViewById(R.id.secondary_icon);
				TextView titleView = row.findViewById(R.id.title);
				TextView descView = row.findViewById(R.id.description);
				Switch check = row.findViewById(R.id.toggle_item);

				boolean categorySelected = filter.isTypeAccepted(category);
				IconsCache ic = app.getIconsCache();
				int iconId = getIconId(category);
				if (iconId != 0) {
					if (categorySelected) {
						iconView.setImageDrawable(ic.getIcon(iconId, R.color.osmand_orange));
					} else {
						iconView.setImageDrawable(ic.getThemedIcon(iconId));
					}
				} else {
					iconView.setImageDrawable(null);
				}
				secondaryIconView.setImageDrawable(
						ic.getIcon(R.drawable.ic_action_additional_option, app.getSettings().isLightContent() ? R.color.icon_color_light : 0));
				check.setOnCheckedChangeListener(null);
				check.setChecked(filter.isTypeAccepted(category));
				String textString = category.getTranslation();
				titleView.setText(textString);
				Set<String> subtypes = filter.getAcceptedSubtypes(category);
				if (categorySelected) {
					if (subtypes == null) {
						descView.setText(getString(R.string.shared_string_all));
					} else {
						StringBuilder sb = new StringBuilder();
						for (String st : subtypes) {
							if (sb.length() > 0) {
								sb.append(", ");
							}
							sb.append(app.getPoiTypes().getPoiTranslation(st));
						}
						descView.setText(sb.toString());
					}
					descView.setVisibility(View.VISIBLE);
				} else {
					descView.setVisibility(View.GONE);
				}
				row.findViewById(R.id.divider).setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);
				addRowListener(category, check);
			}
			return (row);
		}

		private void addRowListener(final PoiCategory category, final Switch check) {
			check.setOnCheckedChangeListener((buttonView, isChecked) -> {
				if (check.isChecked()) {
					showDialog(category, true);
				} else {
					filter.setTypeToAccept(category, false);
					saveFilter();
					notifyDataSetChanged();
				}
			});
		}
	}

	private void saveFilter() {
		helper.editPoiFilter(filter);
		if (!editMode) {
			if (filter.isEmpty()) {
				bottomBarShadow.setVisibility(View.GONE);
				bottomBar.setVisibility(View.GONE);
			} else {
				barTitle.setText(getContext().getString(R.string.selected_categories) + ": " + filter.getAcceptedTypesCount());
				bottomBarShadow.setVisibility(View.VISIBLE);
				bottomBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void showDialog(final PoiCategory poiCategory, boolean selectAll) {
		final int index = listView.getFirstVisiblePosition();
		View v = listView.getChildAt(0);
		final int top = (v == null) ? 0 : v.getTop();
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final LinkedHashMap<String, String> subCategories = new LinkedHashMap<>();
		Set<String> acceptedCategories = filter.getAcceptedSubtypes(poiCategory);
		if (acceptedCategories != null) {
			for(String s : acceptedCategories) {
				subCategories.put(s, Algorithms.capitalizeFirstLetterAndLowercase(s));
			}
		}
		for(PoiType pt :  poiCategory.getPoiTypes()) {
			subCategories.put(pt.getKeyName(), pt.getTranslation());
		}

		final String[] array = subCategories.keySet().toArray(new String[0]);
		final Collator cl = Collator.getInstance();
		cl.setStrength(Collator.SECONDARY);
		Arrays.sort(array, 0, array.length, (object1, object2) -> {
			String v1 = subCategories.get(object1);
			String v2 = subCategories.get(object2);
			return cl.compare(v1, v2);
		});
		final String[] visibleNames = new String[array.length];
		final boolean[] selected = new boolean[array.length];
		boolean allSelected = true;
		for (int i = 0; i < array.length; i++) {
			final String subcategory = array[i];
			visibleNames[i] = subCategories.get(subcategory);
			if (acceptedCategories == null || selectAll) {
				selected[i] = true;
			} else {
				if (allSelected) {
					allSelected = false;
				}
				selected[i] = acceptedCategories.contains(subcategory);
			}
		}

		View titleView = LayoutInflater.from(getActivity())
				.inflate(R.layout.subcategories_dialog_title, null);
		TextView titleTextView = titleView.findViewById(R.id.title);
		titleTextView.setText(poiCategory.getTranslation());
		Switch check = titleView.findViewById(R.id.check);
		check.setChecked(allSelected);
		builder.setCustomTitle(titleView);

		builder.setCancelable(true);
		builder.setNegativeButton(getContext().getText(R.string.shared_string_cancel), (dialog, which) -> {
			dialog.dismiss();
			listAdapter.notifyDataSetChanged();
		});
		builder.setPositiveButton(getContext().getText(R.string.shared_string_apply), (dialog, which) -> {
			LinkedHashSet<String> accepted = new LinkedHashSet<>();
			for (int i = 0; i < selected.length; i++) {
				if(selected[i]){
					accepted.add(array[i]);
				}
			}
			if (subCategories.size() == accepted.size()) {
				filter.selectSubTypesToAccept(poiCategory, null);
			} else if(accepted.size() == 0){
				filter.setTypeToAccept(poiCategory, false);
			} else {
				filter.selectSubTypesToAccept(poiCategory, accepted);
			}
			saveFilter();
			listAdapter.notifyDataSetChanged();
			listView.setSelectionFromTop(index, top);
		});

		builder.setMultiChoiceItems(visibleNames, selected, (dialog, item, isChecked) -> selected[item] = isChecked);
		final AlertDialog dialog = builder.show();
		check.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				Arrays.fill(selected, true);
			} else {
				Arrays.fill(selected, false);
			}
			for (int i = 0; i < selected.length; i++) {
				dialog.getListView().setItemChecked(i, selected[i]);
			}
		});
	}
}