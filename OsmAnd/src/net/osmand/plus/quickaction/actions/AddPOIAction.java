package net.osmand.plus.quickaction.actions;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.osmand.data.LatLon;
import net.osmand.osm.AbstractPoiType;
import net.osmand.osm.MapPoiTypes;
import net.osmand.osm.PoiCategory;
import net.osmand.osm.PoiType;
import net.osmand.osm.edit.Node;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.osmedit.EditPoiData;
import net.osmand.plus.osmedit.EditPoiDialogFragment;
import net.osmand.plus.osmedit.OpenstreetmapLocalUtil;
import net.osmand.plus.osmedit.OpenstreetmapPoint;
import net.osmand.plus.osmedit.OpenstreetmapUtil;
import net.osmand.plus.osmedit.OsmEditingPlugin;
import net.osmand.plus.osmedit.OsmPoint;
import net.osmand.plus.osmedit.dialogs.PoiSubTypeDialogFragment;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.util.Algorithms;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.osmand.plus.osmedit.AdvancedEditPoiFragment.addPoiToStringSet;
import static net.osmand.plus.osmedit.EditPoiData.POI_TYPE_TAG;

public class AddPOIAction extends QuickAction {
	public static final int TYPE = 13;
	private static final String KEY_TAG = "key_tag";
	private static final String KEY_DIALOG = "dialog";

	private transient EditText title;
	private transient String prevType = "";

	public AddPOIAction() {
		super(TYPE);
	}
	public AddPOIAction(QuickAction quickAction) {
		super(quickAction);
	}

	@Override
	public void execute(final MapActivity activity) {
		LatLon latLon = activity.getMapView()
				.getCurrentRotatedTileBox()
				.getCenterLatLon();

		OsmEditingPlugin plugin = OsmandPlugin.getPlugin(OsmEditingPlugin.class);
		if (plugin == null) return;
		Node node = new Node(latLon.getLatitude(), latLon.getLongitude(), -1);
		node.replaceTags(getTagsFromParams());
		EditPoiData editPoiData = new EditPoiData(node, activity.getMyApplication());
		if (Boolean.valueOf(getParams().get(KEY_DIALOG))) {
			Node newNode = editPoiData.getEntity();
			EditPoiDialogFragment editPoiDialogFragment =
					EditPoiDialogFragment.createInstance(newNode, true, getTagsFromParams());
			editPoiDialogFragment.show(activity.getSupportFragmentManager(),
					EditPoiDialogFragment.TAG);
		} else {
			OpenstreetmapUtil mOpenstreetmapUtil;
			if (activity.getMyApplication().getSettings().OFFLINE_EDITION.get()
					|| !activity.getMyApplication().getSettings().isInternetConnectionAvailable(true)) {
				mOpenstreetmapUtil = plugin.getPoiModificationLocalUtil();
			} else {
				mOpenstreetmapUtil = plugin.getPoiModificationRemoteUtil();
			}

			final boolean offlineEdit = mOpenstreetmapUtil instanceof OpenstreetmapLocalUtil;
			Node newNode = new Node(node.getLatitude(), node.getLongitude(), node.getId());
			OsmPoint.Action action = newNode.getId() < 0 ? OsmPoint.Action.CREATE : OsmPoint.Action.MODIFY;
			for (Map.Entry<String, String> tag : editPoiData.getTagValues().entrySet()) {
				if (tag.getKey().equals(EditPoiData.POI_TYPE_TAG)) {
					final PoiType poiType = editPoiData.getAllTranslatedSubTypes().get(tag.getValue().trim().toLowerCase());
					if (poiType != null) {
						newNode.putTagNoLC(poiType.getOsmTag(), poiType.getOsmValue());
						if (poiType.getOsmTag2() != null) {
							newNode.putTagNoLC(poiType.getOsmTag2(), poiType.getOsmValue2());
						}
					} else if (!Algorithms.isEmpty(tag.getValue())) {
						newNode.putTagNoLC(editPoiData.getPoiCategory().getDefaultTag(), tag.getValue());

					}
					if (offlineEdit && !Algorithms.isEmpty(tag.getValue())) {
						newNode.putTagNoLC(tag.getKey(), tag.getValue());
					}
				} else if (!Algorithms.isEmpty(tag.getKey()) && !Algorithms.isEmpty(tag.getValue())) {
					newNode.putTagNoLC(tag.getKey(), tag.getValue());
				}
			}
			EditPoiDialogFragment.commitNode(action, newNode, mOpenstreetmapUtil.getEntityInfo(newNode.getId()), "", false,
                    result -> {
                        if (result != null) {
                            OsmEditingPlugin plugin1 = OsmandPlugin.getPlugin(OsmEditingPlugin.class);
                            if (plugin1 != null && offlineEdit) {
                                List<OpenstreetmapPoint> points = plugin1.getDBPOI().getOpenstreetmapPoints();
                                if (points.size() > 0) {
                                    OsmPoint point = points.get(points.size() - 1);
                                    activity.getContextMenu().showOrUpdate(
                                            new LatLon(point.getLatitude(), point.getLongitude()),
                                            plugin1.getOsmEditsLayer(activity).getObjectName(point), point);
                                }
                            }

                            activity.getMapView().refreshMap(true);
                        }

                        return false;
                    }, activity, mOpenstreetmapUtil, null);

		}
	}

	@Override
	public void setAutoGeneratedTitle(EditText title) {
		this.title = title;
	}

	@Override
	public void drawUI(final ViewGroup parent, final MapActivity activity) {
		final View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.quick_action_add_poi_layout, parent, false);

		final OsmandApplication application = activity.getMyApplication();
		Drawable deleteDrawable = application.getIconsCache().getPaintedIcon(R.drawable.ic_action_remove_dark,
				activity.getResources().getColor(R.color.dash_search_icon_dark));

		final LinearLayout editTagsLineaLayout =
                view.findViewById(R.id.editTagsList);

		final MapPoiTypes poiTypes = application.getPoiTypes();
		final Map<String, PoiType> allTranslatedNames = poiTypes.getAllTranslatedNames(true);
		final TagAdapterLinearLayoutHack mAdapter = new TagAdapterLinearLayoutHack(editTagsLineaLayout, getTagsFromParams(), deleteDrawable);
		// It is possible to not restart initialization every time, and probably move initialization to appInit
		Map<String, PoiType> translatedTypes = poiTypes.getAllTranslatedNames(true);
		HashSet<String> tagKeys = new HashSet<>();
		HashSet<String> valueKeys = new HashSet<>();
		for (AbstractPoiType abstractPoiType : translatedTypes.values()) {
			addPoiToStringSet(abstractPoiType, tagKeys, valueKeys);
		}
		addPoiToStringSet(poiTypes.getOtherMapCategory(), tagKeys, valueKeys);
		tagKeys.addAll(EditPoiDialogFragment.BASIC_TAGS);
		mAdapter.setTagData(tagKeys.toArray(new String[0]));
		mAdapter.setValueData(valueKeys.toArray(new String[0]));
		Button addTagButton = view.findViewById(R.id.addTagButton);
		addTagButton.setOnClickListener(v -> {
            for (int i = 0; i < editTagsLineaLayout.getChildCount(); i++) {
                View item = editTagsLineaLayout.getChildAt(i);
                if (((EditText) item.findViewById(R.id.tagEditText)).getText().toString().isEmpty() &&
                        ((EditText) item.findViewById(R.id.valueEditText)).getText().toString().isEmpty())
                    return;
            }
            mAdapter.addTagView("", "");
        });

		mAdapter.updateViews();

		final TextInputLayout poiTypeTextInputLayout = view.findViewById(R.id.poiTypeTextInputLayout);
		final AutoCompleteTextView poiTypeEditText = view.findViewById(R.id.poiTypeEditText);
		final Switch showDialog = view.findViewById(R.id.saveButton);
		showDialog.setChecked(Boolean.valueOf(getParams().get(KEY_DIALOG)));

		final String text = getTagsFromParams().get(POI_TYPE_TAG);
		poiTypeEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String tp = s.toString();
				putTagIntoParams(POI_TYPE_TAG, tp);
				PoiCategory category = getCategory(allTranslatedNames);

				if (category != null) {
					poiTypeTextInputLayout.setHint(category.getTranslation());
				}

				String add = application.getString(R.string.shared_string_add);

				if (title != null) {

					if (prevType.equals(title.getText().toString())
							|| title.getText().toString().equals(activity.getString(getNameRes()))
							|| title.getText().toString().equals((add + " "))) {

						if (!tp.isEmpty()) {

							title.setText(add + " " + tp);
							prevType = title.getText().toString();
						}
					}
				}
			}
		});
		poiTypeEditText.setText(text != null ? text : "");
		poiTypeEditText.setOnTouchListener((v, event) -> {
            final EditText editText = (EditText) v;
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getX() >= (editText.getRight()
                        - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()
                        - editText.getPaddingRight())) {
                    PoiCategory category = getCategory(allTranslatedNames);
                    PoiCategory tempPoiCategory = (category != null) ? category : poiTypes.getOtherPoiCategory();
                    PoiSubTypeDialogFragment f =
                            PoiSubTypeDialogFragment.createInstance(tempPoiCategory);
                    f.setOnItemSelectListener(poiTypeEditText::setText);
                    f.show(activity.getSupportFragmentManager(), "PoiSubTypeDialogFragment");

                    return true;
                }
            }
            return false;
        });

		setUpAdapterForPoiTypeEditText(activity, allTranslatedNames, poiTypeEditText);

		ImageButton onlineDocumentationButton =
                view.findViewById(R.id.onlineDocumentationButton);
		onlineDocumentationButton.setOnClickListener(v -> activity.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://wiki.openstreetmap.org/wiki/Map_Features"))));

		boolean isLightTheme = activity.getMyApplication().getSettings().OSMAND_THEME.get() == OsmandSettings.OSMAND_LIGHT_THEME;
		final int colorId = isLightTheme ? R.color.inactive_item_orange : R.color.dash_search_icon_dark;
		final int color = activity.getResources().getColor(colorId);
		onlineDocumentationButton.setImageDrawable(activity.getMyApplication().getIconsCache().getPaintedIcon(R.drawable.ic_action_help, color));
		parent.addView(view);
	}

	private void setUpAdapterForPoiTypeEditText(final MapActivity activity, final Map<String, PoiType> allTranslatedNames, final AutoCompleteTextView poiTypeEditText) {
		final Map<String, PoiType> subCategories = new LinkedHashMap<>();
		for (Map.Entry<String, PoiType> s : allTranslatedNames.entrySet()) {
			addMapEntryAdapter(subCategories, s.getKey(), s.getValue());
		}
		final ArrayAdapter<Object> adapter;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			adapter = new ArrayAdapter<>(activity,
					R.layout.list_textview, subCategories.keySet().toArray());
		} else {
			TypedValue typedValue = new TypedValue();
			Resources.Theme theme = activity.getTheme();
			theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
			final int textColor = typedValue.data;

			adapter = new ArrayAdapter<Object>(activity,
					R.layout.list_textview, subCategories.keySet().toArray()) {
				@NonNull
                @Override
				public View getView(int position, View convertView, @NonNull ViewGroup parent) {
					final View view = super.getView(position, convertView, parent);
					((TextView) view.findViewById(R.id.textView)).setTextColor(textColor);
					return view;
				}
			};
		}
		adapter.sort((lhs, rhs) -> lhs.toString().compareTo(rhs.toString()));
		poiTypeEditText.setAdapter(adapter);
		poiTypeEditText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Object item = parent.getAdapter().getItem(position);
				poiTypeEditText.setText(item.toString());
				setUpAdapterForPoiTypeEditText(activity, allTranslatedNames, poiTypeEditText);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private PoiCategory getCategory(Map<String, PoiType> allTranslatedNames) {
		String tp = getTagsFromParams().get(POI_TYPE_TAG);
		if (tp == null) return null;
		PoiType pt = allTranslatedNames.get(tp.toLowerCase());
		if (pt != null) {
			return pt.getCategory();
		} else
			return null;
	}

	private void addMapEntryAdapter(final Map<String, PoiType> subCategories, String key, PoiType v) {
		if (!subCategories.containsKey(key.toLowerCase())) {
			subCategories.put(Algorithms.capitalizeFirstLetterAndLowercase(key), v);
		}
	}

	private class TagAdapterLinearLayoutHack {
		private final LinearLayout linearLayout;
		private final Map<String, String> tagsData;
		private final ArrayAdapter<String> tagAdapter;
		private final ArrayAdapter<String> valueAdapter;
		private final Drawable deleteDrawable;

		TagAdapterLinearLayoutHack(LinearLayout linearLayout,
                                   Map<String, String> tagsData,
                                   Drawable deleteDrawable) {
			this.linearLayout = linearLayout;
			this.tagsData = tagsData;
			this.deleteDrawable = deleteDrawable;

			tagAdapter = new ArrayAdapter<>(linearLayout.getContext(), R.layout.list_textview);
			valueAdapter = new ArrayAdapter<>(linearLayout.getContext(), R.layout.list_textview);
		}

		void updateViews() {
			linearLayout.removeAllViews();
			List<Map.Entry<String, String>> entries = new ArrayList<>(tagsData.entrySet());
			for (Map.Entry<String, String> tag : entries) {
				if (tag.getKey().equals(POI_TYPE_TAG)
						/*|| tag.getKey().equals(OSMSettings.OSMTagKey.NAME.getValue())*/)
					continue;
				addTagView(tag.getKey(), tag.getValue());
			}
		}

		void addTagView(String tg, String vl) {
			View convertView = LayoutInflater.from(linearLayout.getContext())
					.inflate(R.layout.poi_tag_list_item, null, false);
			final AutoCompleteTextView tagEditText =
                    convertView.findViewById(R.id.tagEditText);
			ImageButton deleteItemImageButton =
                    convertView.findViewById(R.id.deleteItemImageButton);
			deleteItemImageButton.setImageDrawable(deleteDrawable);
			final String[] previousTag = new String[]{tg};
			deleteItemImageButton.setOnClickListener(v -> {
                linearLayout.removeView((View) v.getParent());
                tagsData.remove(tagEditText.getText().toString());
                setTagsIntoParams(tagsData);
            });
			final AutoCompleteTextView valueEditText =
                    convertView.findViewById(R.id.valueEditText);
			tagEditText.setText(tg);
			tagEditText.setAdapter(tagAdapter);
			tagEditText.setThreshold(1);
			tagEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String s = tagEditText.getText().toString();
                    tagsData.remove(previousTag[0]);
                    tagsData.put(s, valueEditText.getText().toString());
                    previousTag[0] = s;
                    setTagsIntoParams(tagsData);
                } else {
                    tagAdapter.getFilter().filter(tagEditText.getText());
                }
            });

			valueEditText.setText(vl);
			valueEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					tagsData.put(tagEditText.getText().toString(), s.toString());
					setTagsIntoParams(tagsData);
				}
			});

			initAutocompleteTextView(valueEditText, valueAdapter);

			linearLayout.addView(convertView);
			tagEditText.requestFocus();
		}

		void setTagData(String[] tags) {
			tagAdapter.clear();
			for (String s : tags) {
				tagAdapter.add(s);
			}
			tagAdapter.sort(String.CASE_INSENSITIVE_ORDER);
			tagAdapter.notifyDataSetChanged();
		}

		void setValueData(String[] values) {
			valueAdapter.clear();
			for (String s : values) {
				valueAdapter.add(s);
			}
			valueAdapter.sort(String.CASE_INSENSITIVE_ORDER);
			valueAdapter.notifyDataSetChanged();
		}
	}

	private static void initAutocompleteTextView(final AutoCompleteTextView textView,
												 final ArrayAdapter<String> adapter) {
		textView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                adapter.getFilter().filter(textView.getText());
            }
        });
	}

	@Override
	public boolean fillParams(View root) {
		getParams().put(KEY_DIALOG, Boolean.toString(((Switch) root.findViewById(R.id.saveButton)).isChecked()));
		return !getParams().isEmpty() && (getParams().get(KEY_TAG) != null || !getTagsFromParams().isEmpty());
	}

	private Map<String, String> getTagsFromParams() {
		Map<String, String> quickActions = null;
		if (getParams().get(KEY_TAG) != null) {
			String json = getParams().get(KEY_TAG);
			Type type = new TypeToken<LinkedHashMap<String, String>>() {
			}.getType();
			quickActions = new Gson().fromJson(json, type);
		}
		return quickActions != null ? quickActions : new LinkedHashMap<>();
	}

	private void setTagsIntoParams(Map<String, String> tags) {
		if (!tags.containsKey(POI_TYPE_TAG)) {
			Map<String, String> additionalTags = new HashMap<>(tags);
			tags.clear();
			tags.put(POI_TYPE_TAG, getTagsFromParams().get(POI_TYPE_TAG));
			tags.putAll(additionalTags);
		}
		getParams().put(KEY_TAG, new Gson().toJson(tags));
	}

	private void putTagIntoParams(String tag, String value) {
		Map<String, String> tagsFromParams = getTagsFromParams();
		tagsFromParams.put(tag, value);
		setTagsIntoParams(tagsFromParams);
	}
}