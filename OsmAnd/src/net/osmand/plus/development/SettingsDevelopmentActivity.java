package net.osmand.plus.development;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.View;

import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmAndLocationSimulation;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.SettingsBaseActivity;
import net.osmand.plus.activities.actions.AppModeDialog;
import net.osmand.util.SunriseSunset;

import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SettingsDevelopmentActivity extends SettingsBaseActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(savedInstanceState);
		getToolbar().setTitle(R.string.debugging_and_development);
		PreferenceScreen cat = getPreferenceScreen();

		CheckBoxPreference dbg = createCheckBoxPreference(settings.DEBUG_RENDERING_INFO,
				R.string.trace_rendering, R.string.trace_rendering_descr);
		cat.addPreference(dbg);

		cat.addPreference(createCheckBoxPreference(settings.USE_FAST_RECALCULATION,
				R.string.use_fast_recalculation, R.string.use_fast_recalculation_desc));

		final CheckBoxPreference openGlRender = createCheckBoxPreference(settings.USE_OPENGL_RENDER, R.string.use_opengl_render,R.string.use_opengl_render_descr);
		cat.addPreference(openGlRender);

		cat.addPreference(createCheckBoxPreference(settings.ANIMATE_MY_LOCATION,
				R.string.animate_my_location,
				R.string.animate_my_location_desc));

		final Preference firstRunPreference = new Preference(this);
		firstRunPreference.setTitle(R.string.simulate_initial_startup);
		firstRunPreference.setSummary(R.string.simulate_initial_startup_descr);
		firstRunPreference.setSelectable(true);
		firstRunPreference.setOnPreferenceClickListener(preference -> {
            getMyApplication().getAppInitializer().resetFirstTimeRun();
            getMyApplication().getSettings().FIRST_MAP_IS_DOWNLOADED.set(false);
            getMyApplication().getSettings().WEBGL_SUPPORTED.set(true);
            getMyApplication().getSettings().METRIC_SYSTEM_CHANGED_MANUALLY.set(false);
            getMyApplication().showToastMessage(R.string.shared_string_ok);
            return true;
        });
		cat.addPreference(firstRunPreference);
		
		cat.addPreference(createCheckBoxPreference(settings.SHOULD_SHOW_FREE_VERSION_BANNER,
				R.string.show_free_version_banner,
				R.string.show_free_version_banner_description));
		
		cat.addPreference(createCheckBoxPreference(settings.NO_DISCOUNT_INFO,
				R.string.no_update_info, R.string.no_update_info_desc));

		cat.addPreference(createCheckBoxPreference(settings.SHOW_LEGACY_SEARCH,
				R.string.show_legacy_search, R.string.show_legacy_search_desc));

		Preference pref = new Preference(this);
		final Preference simulate = pref;
		final OsmAndLocationSimulation sim = getMyApplication().getLocationProvider().getLocationSimulation();
		final Runnable updateTitle = () -> simulate.setSummary(sim.isRouteAnimating() ?
                R.string.simulate_your_location_stop_descr : R.string.simulate_your_location_descr);
		pref.setTitle(R.string.simulate_your_location);
		updateTitle.run();
		pref.setKey("simulate_your_location");
		pref.setOnPreferenceClickListener(preference -> {
            updateTitle.run();
            sim.startStopRouteAnimation(SettingsDevelopmentActivity.this, updateTitle);
            return true;
        });
		cat.addPreference(pref);

		pref = new Preference(this);
		pref.setTitle(R.string.test_voice_prompts);
		pref.setSummary(R.string.play_commands_of_currently_selected_voice);
		pref.setKey("test_voice_commands");
		pref.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(SettingsDevelopmentActivity.this, TestVoiceActivity.class));
            return true;
        });
		cat.addPreference(pref);

		pref = new Preference(this);
		pref.setTitle(R.string.app_modes_choose);
		pref.setSummary(R.string.app_modes_choose_descr);
		pref.setKey("available_application_modes");
		pref.setOnPreferenceClickListener(preference -> {
            availableProfileDialog();
            return true;
        });
		cat.addPreference(pref);

		PreferenceCategory info = new PreferenceCategory(this);
		info.setTitle(R.string.info_button);
		cat.addPreference(info);

		pref = new Preference(this);
		pref.setTitle(R.string.global_app_allocated_memory);

		long javaAvailMem = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())/ (1024*1024L);
		long javaTotal = Runtime.getRuntime().totalMemory() / (1024*1024L);
		long dalvikSize = android.os.Debug.getNativeHeapAllocatedSize() / (1024*1024L);
		pref.setSummary(getString(R.string.global_app_allocated_memory_descr, javaAvailMem, javaTotal, dalvikSize));
		pref.setSelectable(false);
		info.addPreference(pref);

		MemoryInfo mem = new Debug.MemoryInfo();
		Debug.getMemoryInfo(mem);
		pref = new Preference(this);
		pref.setTitle(R.string.native_app_allocated_memory);
		pref.setSummary(getString(R.string.native_app_allocated_memory_descr
				, mem.nativePrivateDirty / 1024, mem.dalvikPrivateDirty / 1024 , mem.otherPrivateDirty / 1024
				, mem.nativePss / 1024, mem.dalvikPss / 1024 , mem.otherPss / 1024));
		pref.setSelectable(false);
		info.addPreference(pref);

		final Preference agpspref = new Preference(this);
		agpspref.setTitle(R.string.agps_info);
		if (settings.AGPS_DATA_LAST_TIME_DOWNLOADED.get() != 0L) {
			SimpleDateFormat prt = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
			agpspref.setSummary(getString(R.string.agps_data_last_downloaded, prt.format(settings.AGPS_DATA_LAST_TIME_DOWNLOADED.get())));
		} else {
			agpspref.setSummary(getString(R.string.agps_data_last_downloaded, "--"));
		}
		agpspref.setSelectable(true);
		agpspref.setOnPreferenceClickListener(preference -> {
            if(getMyApplication().getSettings().isInternetConnectionAvailable(true)) {
                getMyApplication().getLocationProvider().redownloadAGPS();
                SimpleDateFormat prt = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
                agpspref.setSummary(getString(R.string.agps_data_last_downloaded, prt.format(settings.AGPS_DATA_LAST_TIME_DOWNLOADED.get())));
            }
        return true;
        });
		info.addPreference(agpspref);

		SunriseSunset sunriseSunset = getMyApplication().getDaynightHelper().getSunriseSunset();
		pref = new Preference(this);
		pref.setTitle(R.string.day_night_info);
		if (sunriseSunset != null) {
			SimpleDateFormat prt = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
			pref.setSummary(getString(R.string.day_night_info_description, prt.format(sunriseSunset.getSunrise()),
					prt.format(sunriseSunset.getSunset())));
		} else {
			pref.setSummary(getString(R.string.day_night_info_description, "null", "null"));
		}
		pref.setSelectable(false);
		info.addPreference(pref);
	}

	private void availableProfileDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		final List<ApplicationMode> modes = ApplicationMode.allPossibleValues();
		modes.remove(ApplicationMode.DEFAULT);
		final Set<ApplicationMode> selected = new LinkedHashSet<>(ApplicationMode.values(settings));
		selected.remove(ApplicationMode.DEFAULT);
		View v = AppModeDialog.prepareAppModeView(this, modes, selected, null, false, true, false,
                v1 -> {
                    StringBuilder vls = new StringBuilder(ApplicationMode.DEFAULT.getStringKey()+",");
                    for(ApplicationMode mode :  modes) {
                        if(selected.contains(mode)) {
                            vls.append(mode.getStringKey()).append(",");
                        }
                    }
                    settings.AVAILABLE_APP_MODES.set(vls.toString());
                });
		b.setTitle(R.string.profile_settings);
		b.setPositiveButton(R.string.shared_string_ok, null);
		b.setView(v);
		b.show();
	}
}
