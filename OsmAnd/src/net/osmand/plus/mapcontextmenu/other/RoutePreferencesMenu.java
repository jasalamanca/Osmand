package net.osmand.plus.mapcontextmenu.other;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.CallbackWithObject;
import net.osmand.IndexConstants;
import net.osmand.Location;
import net.osmand.data.LatLon;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.ContextMenuAdapter;
import net.osmand.plus.ContextMenuItem;
import net.osmand.plus.GPXUtilities;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.activities.SettingsBaseActivity;
import net.osmand.plus.activities.SettingsNavigationActivity;
import net.osmand.plus.dashboard.DashboardOnMap;
import net.osmand.plus.download.DownloadActivity;
import net.osmand.plus.download.DownloadActivityType;
import net.osmand.plus.helpers.FileNameTranslationHelper;
import net.osmand.plus.helpers.GpxUiHelper;
import net.osmand.plus.routing.RouteProvider;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.plus.views.MapControlsLayer;
import net.osmand.router.GeneralRouter;
import net.osmand.router.GeneralRouter.RoutingParameter;
import net.osmand.util.Algorithms;
import net.osmand.util.MapUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RoutePreferencesMenu {

	private final OsmandSettings settings;
	private final OsmandApplication app;
	private final MapActivity mapActivity;
	private final MapControlsLayer controlsLayer;
	private final RoutingHelper routingHelper;
	private ArrayAdapter<LocalRoutingParameter> listAdapter;

	public static final String MORE_VALUE = "MORE_VALUE";

	public RoutePreferencesMenu(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
		app = mapActivity.getMyApplication();
		this.controlsLayer = mapActivity.getMapLayers().getMapControlsLayer();
		routingHelper = mapActivity.getRoutingHelper();
		settings = app.getSettings();
	}

	public static class LocalRoutingParameter {

		RoutingParameter routingParameter;
		private final ApplicationMode am;
		
		LocalRoutingParameter(ApplicationMode am) {
			this.am = am;
		}

		String getText(MapActivity mapActivity) {
			return SettingsBaseActivity.getRoutingStringPropertyName(mapActivity, routingParameter.getId(),
					routingParameter.getName());
		}

		boolean isSelected(OsmandSettings settings) {
			final OsmandSettings.CommonPreference<Boolean> property =
					settings.getCustomRoutingBooleanProperty(routingParameter.getId(), routingParameter.getDefaultBoolean());
			if(am != null) {
				return property.getModeValue(am);
			} else {
				return property.get();
			}
		}

		void setSelected(OsmandSettings settings, boolean isChecked) {
			final OsmandSettings.CommonPreference<Boolean> property =
					settings.getCustomRoutingBooleanProperty(routingParameter.getId(), routingParameter.getDefaultBoolean());
			if(am != null) {
				property.setModeValue(am, isChecked);
			} else {
				property.set(isChecked);
			}
		}

		ApplicationMode getApplicationMode() {
			return am;
		}
	}

	private static class LocalRoutingParameterGroup extends LocalRoutingParameter {
		private final String groupName;
		private final List<LocalRoutingParameter> routingParameters = new ArrayList<>();

		LocalRoutingParameterGroup(ApplicationMode am, String groupName) {
			super(am);
			this.groupName = groupName;
		}

		void addRoutingParameter(RoutingParameter routingParameter) {
			LocalRoutingParameter p = new LocalRoutingParameter(getApplicationMode());
			p.routingParameter = routingParameter;
			routingParameters.add(p);
		}

		String getGroupName() {
			return groupName;
		}

		List<LocalRoutingParameter> getRoutingParameters() {
			return routingParameters;
		}

		@Override
        String getText(MapActivity mapActivity) {
			return SettingsBaseActivity.getRoutingStringPropertyName(mapActivity, groupName,
					Algorithms.capitalizeFirstLetterAndLowercase(groupName.replace('_', ' ')));
		}

		@Override
		public boolean isSelected(OsmandSettings settings) {
			return false;
		}

		@Override
		public void setSelected(OsmandSettings settings, boolean isChecked) {
		}

		LocalRoutingParameter getSelected(OsmandSettings settings) {
			for (LocalRoutingParameter p : routingParameters) {
				if (p.isSelected(settings)) {
					return p;
				}
			}
			return null;
		}
	}

	private static class MuteSoundRoutingParameter extends LocalRoutingParameter {
		MuteSoundRoutingParameter() {
			super(null);
		}
	}

	private static class InterruptMusicRoutingParameter extends LocalRoutingParameter {
		InterruptMusicRoutingParameter() {
			super(null);
		}
	}

	private static class VoiceGuidanceRoutingParameter extends LocalRoutingParameter {
		VoiceGuidanceRoutingParameter() {
			super(null);
		}
	}

	private static class AvoidRoadsRoutingParameter extends LocalRoutingParameter {
		AvoidRoadsRoutingParameter() {
			super(null);
		}
	}

	private static class GpxLocalRoutingParameter extends LocalRoutingParameter {
		GpxLocalRoutingParameter() {
			super(null);
		}
	}

	private static class OtherSettingsRoutingParameter extends LocalRoutingParameter {
		OtherSettingsRoutingParameter() {
			super(null);
		}
	}

	private static class OtherLocalRoutingParameter extends LocalRoutingParameter {
		final String text;
		boolean selected;
		final int id;

		OtherLocalRoutingParameter(int id, String text, boolean selected) {
			super(null);
			this.text = text;
			this.selected = selected;
			this.id = id;
		}

		@Override
		public String getText(MapActivity mapActivity) {
			return text;
		}

		@Override
        boolean isSelected(OsmandSettings settings) {
			return selected;
		}

		@Override
		public void setSelected(OsmandSettings settings, boolean isChecked) {
			selected = isChecked;
		}
	}

	private void switchSound() {
		boolean mt = !routingHelper.getVoiceRouter().isMute();
		settings.VOICE_MUTE.set(mt);
		routingHelper.getVoiceRouter().setMute(mt);
	}

	private void switchMusic() {
		boolean mt = !settings.INTERRUPT_MUSIC.get();
		settings.INTERRUPT_MUSIC.set(mt);
	}

	private void doSelectVoiceGuidance() {
		selectVoiceGuidance(mapActivity, result -> {
			applyVoiceProvider(mapActivity, result);
			updateParameters();
			return true;
		});
	}

	private void selectRestrictedRoads() {
		mapActivity.getDashboard().setDashboardVisibility(false, DashboardOnMap.DashboardType.ROUTE_PREFERENCES);
		controlsLayer.getMapRouteInfoMenu().hide();
		app.getAvoidSpecificRoads().showDialog(mapActivity);
	}

	public static void selectVoiceGuidance(final MapActivity mapActivity, final CallbackWithObject<String> callback) {
		final ContextMenuAdapter adapter = new ContextMenuAdapter();

		String[] entries;
		final String[] entrieValues;
		Set<String> voiceFiles = getVoiceFiles(mapActivity);
		entries = new String[voiceFiles.size() + 2];
		entrieValues = new String[voiceFiles.size() + 2];
		int k = 0;
		int selected = -1;
		String selectedValue = mapActivity.getMyApplication().getSettings().VOICE_PROVIDER.get();
		entrieValues[k] = OsmandSettings.VOICE_PROVIDER_NOT_USE;
		entries[k] = mapActivity.getResources().getString(R.string.shared_string_do_not_use);
		ContextMenuItem.ItemBuilder itemBuilder = new ContextMenuItem.ItemBuilder();
		adapter.addItem(itemBuilder.setTitle(entries[k]).createItem());
		if (OsmandSettings.VOICE_PROVIDER_NOT_USE.equals(selectedValue)) {
			selected = k;
		}
		k++;
		for (String s : voiceFiles) {
			entries[k] = (s.contains("tts") ?  mapActivity.getResources().getString(R.string.ttsvoice) + " " : "") +
					FileNameTranslationHelper.getVoiceName(mapActivity, s);
			entrieValues[k] = s;
			adapter.addItem(itemBuilder.setTitle(entries[k]).createItem());
			if (s.equals(selectedValue)) {
				selected = k;
			}
			k++;
		}
		entrieValues[k] = MORE_VALUE;
		entries[k] =  mapActivity.getResources().getString(R.string.install_more);
		adapter.addItem(itemBuilder.setTitle(entries[k]).createItem());

		AlertDialog.Builder bld = new AlertDialog.Builder(mapActivity);
		bld.setSingleChoiceItems(entries, selected, (dialog, which) -> {
			String value = entrieValues[which];
			if (MORE_VALUE.equals(value)) {
				final Intent intent = new Intent(mapActivity, DownloadActivity.class);
				intent.putExtra(DownloadActivity.TAB_TO_OPEN, DownloadActivity.DOWNLOAD_TAB);
				intent.putExtra(DownloadActivity.FILTER_CAT, DownloadActivityType.VOICE_FILE.getTag());
				mapActivity.startActivity(intent);
			} else {
				if (callback != null) {
					callback.processResult(value);
				}
			}
			dialog.dismiss();
		});
		bld.show();
	}

	public static String getVoiceProviderName(Context ctx, String value) {
		if (OsmandSettings.VOICE_PROVIDER_NOT_USE.equals(value)) {
			return ctx.getResources().getString(R.string.shared_string_do_not_use);
		} else {
			return (value.contains("tts") ? ctx.getResources().getString(R.string.ttsvoice) + " " : "") +
					FileNameTranslationHelper.getVoiceName(ctx, value);
		}
	}

	public static void applyVoiceProvider(MapActivity mapActivity, String provider) {
		OsmandApplication app = mapActivity.getMyApplication();
		app.getSettings().VOICE_PROVIDER.set(provider);
		app.initVoiceCommandPlayer(mapActivity, app.getRoutingHelper().getAppMode(), false, null, true, false);
	}

	private static Set<String> getVoiceFiles(MapActivity mapActivity) {
		// read available voice data
		File extStorage = mapActivity.getMyApplication().getAppPath(IndexConstants.VOICE_INDEX_DIR);
		Set<String> setFiles = new LinkedHashSet<>();
		if (extStorage.exists()) {
			for (File f : extStorage.listFiles()) {
				if (f.isDirectory()) {
					setFiles.add(f.getName());
				}
			}
		}
		return setFiles;
	}

	public OnItemClickListener getItemClickListener(final ArrayAdapter<?> listAdapter) {
		return (adapterView, view, item, l) -> {
			Object obj = listAdapter.getItem(item);
			if (obj instanceof LocalRoutingParameterGroup) {
				final LocalRoutingParameterGroup group = (LocalRoutingParameterGroup) obj;
				final ContextMenuAdapter adapter = new ContextMenuAdapter();
				int i = 0;
				int selectedIndex = -1;
				for (LocalRoutingParameter p : group.getRoutingParameters()) {
					adapter.addItem(ContextMenuItem.createBuilder(p.getText(mapActivity))
							.setSelected(false).createItem());
					if (p.isSelected(settings)) {
						selectedIndex = i;
					}
					i++;
				}
				if (selectedIndex == -1) {
					selectedIndex = 0;
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(mapActivity);
				final int layout = R.layout.list_menu_item_native_singlechoice;

				final ArrayAdapter<String> listAdapter1 = new ArrayAdapter<String>(mapActivity, layout, R.id.text1,
						adapter.getItemNames()) {
					@NonNull
					@Override
					public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
						// User super class to create the View
						View v = convertView;
						if (v == null) {
							v = mapActivity.getLayoutInflater().inflate(layout, null);
						}
						final ContextMenuItem item = adapter.getItem(position);
						TextView tv = v.findViewById(R.id.text1);
						tv.setText(item.getTitle());
						tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);

						return v;
					}
				};

				final int[] selectedPosition = {selectedIndex};
				builder.setSingleChoiceItems(listAdapter1, selectedIndex, (dialog, position) -> selectedPosition[0] = position);
				builder.setTitle(group.getText(mapActivity))
						.setPositiveButton(R.string.shared_string_ok, (dialog, which) -> {

							int position = selectedPosition[0];
							if (position >= 0 && position < group.getRoutingParameters().size()) {
								for (int i1 = 0; i1 < group.getRoutingParameters().size(); i1++) {
									LocalRoutingParameter rp = group.getRoutingParameters().get(i1);
									rp.setSelected(settings, i1 == position);
								}
								mapActivity.getRoutingHelper().recalculateRouteDueToSettingsChange();
								updateParameters();
							}
						})
						.setNegativeButton(R.string.shared_string_cancel, null);

				builder.create().show();
			} else if (obj instanceof OtherSettingsRoutingParameter) {
				final Intent settings = new Intent(mapActivity, SettingsNavigationActivity.class);
				settings.putExtra(SettingsNavigationActivity.INTENT_SKIP_DIALOG, true);
				settings.putExtra(SettingsBaseActivity.INTENT_APP_MODE, routingHelper.getAppMode().getStringKey());
				mapActivity.startActivity(settings);
			} else if (obj instanceof MuteSoundRoutingParameter) {
				final CompoundButton btn = view.findViewById(R.id.toggle_item);
				btn.performClick();
			} else if (obj instanceof VoiceGuidanceRoutingParameter) {
				doSelectVoiceGuidance();
			} else if (obj instanceof InterruptMusicRoutingParameter) {
				final CompoundButton btn = view.findViewById(R.id.toggle_item);
				btn.performClick();
			} else if (obj instanceof AvoidRoadsRoutingParameter) {
				selectRestrictedRoads();
			} else if (view.findViewById(R.id.GPXRouteSpinner) != null) {
				showOptionsMenu(view.findViewById(R.id.GPXRouteSpinner));
			} else {
				CheckBox ch = view.findViewById(R.id.toggle_item);
				if (ch != null) {
					ch.setChecked(!ch.isChecked());
				}
			}
		};
	}

	public ArrayAdapter<LocalRoutingParameter> getRoutePreferencesDrawerAdapter(final boolean nightMode) {

		listAdapter = new ArrayAdapter<LocalRoutingParameter>(mapActivity, R.layout.layers_list_activity_item, R.id.title,
				getRoutingParameters(routingHelper.getAppMode())) {
			@NonNull
            @Override
			public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
				LocalRoutingParameter parameter = getItem(position);
				if (parameter instanceof MuteSoundRoutingParameter) {
					View v = mapActivity.getLayoutInflater().inflate(R.layout.switch_select_list_item, null);
					AndroidUtils.setListItemBackground(mapActivity, v, nightMode);
					v.findViewById(R.id.description_text).setVisibility(View.GONE);
					v.findViewById(R.id.select_button).setVisibility(View.GONE);
					((ImageView) v.findViewById(R.id.icon))
							.setImageDrawable(app.getIconsCache().getIcon(R.drawable.ic_action_volume_up, !nightMode));
					final CompoundButton btn = v.findViewById(R.id.toggle_item);
					btn.setVisibility(View.VISIBLE);
					btn.setChecked(!routingHelper.getVoiceRouter().isMute());
					btn.setOnCheckedChangeListener((buttonView, isChecked) -> switchSound());

					TextView tv = v.findViewById(R.id.header_text);
					AndroidUtils.setTextPrimaryColor(mapActivity, tv, nightMode);
					tv.setText(getString(R.string.shared_string_sound));
					return v;
				}
				if (parameter instanceof AvoidRoadsRoutingParameter) {
					View v = mapActivity.getLayoutInflater().inflate(R.layout.switch_select_list_item, null);
					AndroidUtils.setListItemBackground(mapActivity, v, nightMode);
					((ImageView) v.findViewById(R.id.icon))
							.setImageDrawable(app.getIconsCache().getIcon(R.drawable.ic_action_road_works_dark, !nightMode));
					v.findViewById(R.id.toggle_item).setVisibility(View.GONE);
					final TextView btn = v.findViewById(R.id.select_button);
					btn.setTextColor(btn.getLinkTextColors());
					btn.setOnClickListener(v12 -> selectRestrictedRoads());

					TextView tv = v.findViewById(R.id.header_text);
					AndroidUtils.setTextPrimaryColor(mapActivity, tv, nightMode);
					tv.setText(getString(R.string.impassable_road));

					TextView tvDesc = v.findViewById(R.id.description_text);
					AndroidUtils.setTextSecondaryColor(mapActivity, tvDesc, nightMode);
					tvDesc.setText(getString(R.string.impassable_road_desc));

					return v;
				}
				if (parameter instanceof VoiceGuidanceRoutingParameter) {
					View v = mapActivity.getLayoutInflater().inflate(R.layout.switch_select_list_item, null);
					AndroidUtils.setListItemBackground(mapActivity, v, nightMode);
					v.findViewById(R.id.icon).setVisibility(View.GONE);
					v.findViewById(R.id.description_text).setVisibility(View.GONE);
					v.findViewById(R.id.toggle_item).setVisibility(View.GONE);
					final TextView btn = v.findViewById(R.id.select_button);
					btn.setTextColor(btn.getLinkTextColors());
					String voiceProvider = settings.VOICE_PROVIDER.get();
					String voiceProviderStr;
					if (voiceProvider != null) {
						if (OsmandSettings.VOICE_PROVIDER_NOT_USE.equals(voiceProvider)) {
							voiceProviderStr = getString(R.string.shared_string_do_not_use);
						} else {
							voiceProviderStr = FileNameTranslationHelper.getVoiceName(mapActivity, voiceProvider);
						}
						voiceProviderStr += voiceProvider.contains("tts") ? " TTS" : "";
					} else {
						voiceProviderStr = getString(R.string.shared_string_not_selected);
					}
					btn.setText(voiceProviderStr);
					btn.setOnClickListener(v1 -> doSelectVoiceGuidance());

					TextView tv = v.findViewById(R.id.header_text);
					AndroidUtils.setTextPrimaryColor(mapActivity, tv, nightMode);
					tv.setText(getString(R.string.voice_provider));

					return v;
				}
				if (parameter instanceof InterruptMusicRoutingParameter) {
					View v = mapActivity.getLayoutInflater().inflate(R.layout.switch_select_list_item, null);
					AndroidUtils.setListItemBackground(mapActivity, v, nightMode);
					v.findViewById(R.id.select_button).setVisibility(View.GONE);
					v.findViewById(R.id.icon).setVisibility(View.GONE);
					final CompoundButton btn = v.findViewById(R.id.toggle_item);
					btn.setVisibility(View.VISIBLE);
					btn.setChecked(settings.INTERRUPT_MUSIC.get());
					btn.setOnCheckedChangeListener((buttonView, isChecked) -> switchMusic());

					TextView tv = v.findViewById(R.id.header_text);
					AndroidUtils.setTextPrimaryColor(mapActivity, tv, nightMode);
					tv.setText(getString(R.string.interrupt_music));
					TextView tvDesc = v.findViewById(R.id.description_text);
					AndroidUtils.setTextSecondaryColor(mapActivity, tvDesc, nightMode);
					tvDesc.setText(getString(R.string.interrupt_music_descr));

					return v;
				}
				if (parameter instanceof GpxLocalRoutingParameter) {
					View v = mapActivity.getLayoutInflater().inflate(R.layout.plan_route_gpx, null);
					AndroidUtils.setListItemBackground(mapActivity, v, nightMode);
					AndroidUtils.setTextPrimaryColor(mapActivity, v.findViewById(R.id.GPXRouteTitle), nightMode);
					final TextView gpxSpinner = v.findViewById(R.id.GPXRouteSpinner);
					AndroidUtils.setTextPrimaryColor(mapActivity, gpxSpinner, nightMode);
					((ImageView) v.findViewById(R.id.dropDownIcon))
							.setImageDrawable(app.getIconsCache().getIcon(R.drawable.ic_action_arrow_drop_down, !nightMode));
					updateSpinnerItems(gpxSpinner);
					return v;
				}
				if (parameter instanceof OtherSettingsRoutingParameter) {
					View v = mapActivity.getLayoutInflater().inflate(R.layout.layers_list_activity_item, null);
					AndroidUtils.setListItemBackground(mapActivity, v, nightMode);
					final ImageView icon = v.findViewById(R.id.icon);
					icon.setImageDrawable(app.getIconsCache().getIcon(R.drawable.map_action_settings, !nightMode));
					icon.setVisibility(View.VISIBLE);
					TextView titleView = v.findViewById(R.id.title);
					titleView.setText(R.string.routing_settings_2);
					AndroidUtils.setTextPrimaryColor(mapActivity, titleView, nightMode);
					v.findViewById(R.id.toggle_item).setVisibility(View.GONE);
					return v;
				}
				return inflateRoutingParameter(position);
			}

			private View inflateRoutingParameter(final int position) {
				View v = mapActivity.getLayoutInflater().inflate(R.layout.layers_list_activity_item, null);
				AndroidUtils.setListItemBackground(mapActivity, v, nightMode);
				final TextView tv = v.findViewById(R.id.title);
				final TextView desc = v.findViewById(R.id.description);
				final CheckBox ch = v.findViewById(R.id.toggle_item);
				final LocalRoutingParameter rp = getItem(position);
				AndroidUtils.setTextPrimaryColor(mapActivity, tv, nightMode);
				tv.setText(rp.getText(mapActivity));
				ch.setOnCheckedChangeListener(null);
				if (rp instanceof LocalRoutingParameterGroup) {
					LocalRoutingParameterGroup group = (LocalRoutingParameterGroup) rp;
					AndroidUtils.setTextPrimaryColor(mapActivity, desc, nightMode);
					LocalRoutingParameter selected = group.getSelected(settings);
					if (selected != null) {
						desc.setText(selected.getText(mapActivity));
						desc.setVisibility(View.VISIBLE);
					}
					ch.setVisibility(View.GONE);
				} else {
					if (rp.routingParameter != null && rp.routingParameter.getId().equals("short_way")) {
						// if short route settings - it should be inverse of fast_route_mode
						ch.setChecked(!settings.FAST_ROUTE_MODE.getModeValue(routingHelper.getAppMode()));
					} else {
						ch.setChecked(rp.isSelected(settings));
					}
					ch.setVisibility(View.VISIBLE);
					ch.setOnCheckedChangeListener((buttonView, isChecked) -> applyRoutingParameter(rp, isChecked));
				}
				return v;
			}
		};

		return listAdapter;
	}

	private void applyRoutingParameter(LocalRoutingParameter rp, boolean isChecked) {
		// if short way that it should set valut to fast mode opposite of current
		if (rp.routingParameter != null && rp.routingParameter.getId().equals("short_way")) {
			settings.FAST_ROUTE_MODE.setModeValue(routingHelper.getAppMode(), !isChecked);
		}
		rp.setSelected(settings, isChecked);

		if (rp instanceof OtherLocalRoutingParameter) {
			updateGpxRoutingParameter((OtherLocalRoutingParameter) rp);
		}
		mapActivity.getRoutingHelper().recalculateRouteDueToSettingsChange();
	}

	private void updateGpxRoutingParameter(OtherLocalRoutingParameter gpxParam) {
		RouteProvider.GPXRouteParamsBuilder rp = mapActivity.getRoutingHelper().getCurrentGPXRoute();
		boolean selected = gpxParam.isSelected(settings);
		if (rp != null) {
			if (gpxParam.id == R.string.gpx_option_reverse_route) {
				rp.setReverse(selected);
				TargetPointsHelper tg = app.getTargetPointsHelper();
				List<Location> ps = rp.getPoints();
				if (ps.size() > 0) {
					Location first = ps.get(0);
					Location end = ps.get(ps.size() - 1);
					TargetPointsHelper.TargetPoint pn = tg.getPointToNavigate();
					boolean update = false;
					if (pn == null
							|| MapUtils.getDistance(pn.point, new LatLon(first.getLatitude(), first.getLongitude())) < 10) {
						tg.navigateToPoint(new LatLon(end.getLatitude(), end.getLongitude()), false, -1);
						update = true;
					}
					if (tg.getPointToStart() == null
							|| MapUtils.getDistance(tg.getPointToStart().point,
							new LatLon(end.getLatitude(), end.getLongitude())) < 10) {
						tg.setStartPoint(new LatLon(first.getLatitude(), first.getLongitude()), false, null);
						update = true;
					}
					if (update) {
						tg.updateRouteAndRefresh(true);
					}
				}
			} else if (gpxParam.id == R.string.gpx_option_calculate_first_last_segment) {
				rp.setCalculateOsmAndRouteParts(selected);
				settings.GPX_ROUTE_CALC_OSMAND_PARTS.set(selected);
			} else if (gpxParam.id == R.string.gpx_option_from_start_point) {
				rp.setPassWholeRoute(selected);
			} else if (gpxParam.id == R.string.use_points_as_intermediates) {
				settings.GPX_CALCULATE_RTEPT.set(selected);
				rp.setUseIntermediatePointsRTE(selected);
			} else if (gpxParam.id == R.string.calculate_osmand_route_gpx) {
				settings.GPX_ROUTE_CALC.set(selected);
				rp.setCalculateOsmAndRoute(selected);
				updateParameters();
			}
		}
		if (gpxParam.id == R.string.calculate_osmand_route_without_internet) {
			settings.GPX_ROUTE_CALC_OSMAND_PARTS.set(selected);
		}
		if (gpxParam.id == R.string.fast_route_mode) {
			settings.FAST_ROUTE_MODE.set(selected);
		}
		if (gpxParam.id == R.string.speak_favorites) {
			settings.ANNOUNCE_NEARBY_FAVORITES.set(selected);
		}
	}

	private List<LocalRoutingParameter> getRoutingParameters(ApplicationMode am) {
		List<LocalRoutingParameter> list = getRoutingParametersInner(am);
		list.add(0, new MuteSoundRoutingParameter());
		list.add(1, new VoiceGuidanceRoutingParameter());
		list.add(2, new InterruptMusicRoutingParameter());
		list.add(3, new AvoidRoadsRoutingParameter());
		list.add(new GpxLocalRoutingParameter());
		list.add(new OtherSettingsRoutingParameter());
		return list;
	}

	private List<LocalRoutingParameter> getRoutingParametersInner(ApplicationMode am) {
		List<LocalRoutingParameter> list = new ArrayList<>();
		RouteProvider.GPXRouteParamsBuilder rparams = mapActivity.getRoutingHelper().getCurrentGPXRoute();
		if (rparams != null) {
			GPXUtilities.GPXFile fl = rparams.getFile();
			if (fl.hasRtePt()) {
				list.add(new OtherLocalRoutingParameter(R.string.use_points_as_intermediates,
						getString(R.string.use_points_as_intermediates), rparams.isUseIntermediatePointsRTE()));
			}
			list.add(new OtherLocalRoutingParameter(R.string.gpx_option_reverse_route,
					getString(R.string.gpx_option_reverse_route), rparams.isReverse()));
			if (!rparams.isUseIntermediatePointsRTE()) {
				list.add(new OtherLocalRoutingParameter(R.string.gpx_option_from_start_point,
						getString(R.string.gpx_option_from_start_point), rparams.isPassWholeRoute()));
				list.add(new OtherLocalRoutingParameter(R.string.gpx_option_calculate_first_last_segment,
						getString(R.string.gpx_option_calculate_first_last_segment), rparams
						.isCalculateOsmAndRouteParts()));
			}
		}
		GeneralRouter rm = SettingsNavigationActivity.getRouter(app.getDefaultRoutingConfig(), am);
		if (rm == null || (rparams != null && !rparams.isCalculateOsmAndRoute()) && !rparams.getFile().hasRtePt()) {
			return list;
		}
		for (RoutingParameter r : rm.getParameters().values()) {
			if (r.getType() == GeneralRouter.RoutingParameterType.BOOLEAN) {
				if ("relief_smoothness_factor".equals(r.getGroup())) {
					continue;
				}
				if (!Algorithms.isEmpty(r.getGroup())) {
					LocalRoutingParameterGroup rpg = getLocalRoutingParameterGroup(list, r.getGroup());
					if (rpg == null) {
						rpg = new LocalRoutingParameterGroup(am, r.getGroup());
						list.add(rpg);
					}
					rpg.addRoutingParameter(r);
				} else {
					LocalRoutingParameter rp = new LocalRoutingParameter(am);
					rp.routingParameter = r;
					list.add(rp);
				}
			}
		}

		return list;
	}

	private LocalRoutingParameterGroup getLocalRoutingParameterGroup(List<LocalRoutingParameter> list, String groupName) {
		for (LocalRoutingParameter p : list) {
			if (p instanceof LocalRoutingParameterGroup && groupName.equals(((LocalRoutingParameterGroup) p).getGroupName())) {
				return (LocalRoutingParameterGroup) p;
			}
		}
		return null;
	}

	private void updateParameters() {
		ApplicationMode am = routingHelper.getAppMode();
		listAdapter.setNotifyOnChange(false);
		listAdapter.clear();
		for (LocalRoutingParameter r : getRoutingParameters(am)) {
			listAdapter.add(r);
		}
		listAdapter.notifyDataSetChanged();
	}

	private void openGPXFileSelection(final TextView gpxSpinner) {
		GpxUiHelper.selectGPXFile(mapActivity, false, false, result -> {
			mapActivity.getMapActions().setGPXRouteParams(result[0]);
			app.getTargetPointsHelper().updateRouteAndRefresh(true);
			updateSpinnerItems(gpxSpinner);
			updateParameters();
			mapActivity.getRoutingHelper().recalculateRouteDueToSettingsChange();
			return true;
		});
	}

	private void updateSpinnerItems(final TextView gpxSpinner) {
		RouteProvider.GPXRouteParamsBuilder rp = mapActivity.getRoutingHelper().getCurrentGPXRoute();
		gpxSpinner.setText(rp == null ? mapActivity.getString(R.string.shared_string_none) :
				new File(rp.getFile().path).getName());
	}

	private void showOptionsMenu(final TextView gpxSpinner) {
		RouteProvider.GPXRouteParamsBuilder rp = mapActivity.getRoutingHelper().getCurrentGPXRoute();
		final PopupMenu optionsMenu = new PopupMenu(gpxSpinner.getContext(), gpxSpinner);
		MenuItem item = optionsMenu.getMenu().add(
				mapActivity.getString(R.string.shared_string_none));
		item.setOnMenuItemClickListener(item13 -> {
			if (mapActivity.getRoutingHelper().getCurrentGPXRoute() != null) {
				mapActivity.getRoutingHelper().setGpxParams(null);
				settings.FOLLOW_THE_GPX_ROUTE.set(null);
				mapActivity.getRoutingHelper().recalculateRouteDueToSettingsChange();
			}
			updateParameters();
			return true;
		});
		item = optionsMenu.getMenu().add(mapActivity.getString(R.string.select_gpx));
		item.setOnMenuItemClickListener(item12 -> {
			openGPXFileSelection(gpxSpinner);
			return true;
		});
		if (rp != null) {
			item = optionsMenu.getMenu().add(new File(rp.getFile().path).getName());
			item.setOnMenuItemClickListener(item1 -> {
				// nothing to change
				return true;
			});
		}
		optionsMenu.show();
	}

	private String getString(int id) {
		return mapActivity.getString(id);
	}
}
