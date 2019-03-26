package net.osmand.plus.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.Version;

public class SettingsActivity extends SettingsBaseActivity {

	private static final String INTENT_KEY_SETTINGS_SCREEN = "INTENT_KEY_SETTINGS_SCREEN";
	private static final int SCREEN_GENERAL_SETTINGS = 1;
	private static final int SCREEN_NAVIGATION_SETTINGS = 2;

	private static final int PLUGINS_SELECTION_REQUEST = 1;

	private Preference general;
	private Preference routing;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_pref);
		PreferenceScreen screen = getPreferenceScreen();
		general = screen.findPreference("general_settings");
		general.setOnPreferenceClickListener(this);
		routing = screen.findPreference("routing_settings");
		routing .setOnPreferenceClickListener(this);
		
		getToolbar().setTitle(Version.getFullVersion(getMyApplication()));
		
		Intent intent = getIntent();
		if(intent != null && intent.getIntExtra(INTENT_KEY_SETTINGS_SCREEN, 0) != 0){
			int s = intent.getIntExtra(INTENT_KEY_SETTINGS_SCREEN, 0);
			if(s == SCREEN_GENERAL_SETTINGS){
				startActivity(new Intent(this, SettingsGeneralActivity.class));
			} else if(s == SCREEN_NAVIGATION_SETTINGS){
				startActivity(new Intent(this, SettingsNavigationActivity.class));
			} 
		}
		PreferenceCategory plugins = (PreferenceCategory) screen.findPreference("plugin_settings");
		for(OsmandPlugin op : OsmandPlugin.getEnabledPlugins()) {
			final Class<? extends Activity> sa = op.getSettingsActivity();
			if(sa != null) {
				Preference preference = new Preference(this);
				preference.setTitle(op.getName());
				preference.setKey(op.getId());
				preference.setOnPreferenceClickListener(preference1 -> {
                    startActivity(new Intent(SettingsActivity.this, sa));
                    return false;
                });
				plugins.addPreference(preference);
			}
		}
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == PLUGINS_SELECTION_REQUEST) && (resultCode == PluginsActivity.ACTIVE_PLUGINS_LIST_MODIFIED)) {
			finish();
			startActivity(getIntent());
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == general) {
			startActivity(new Intent(this, SettingsGeneralActivity.class));
			return true;
		} else if (preference == routing) {
			startActivity(new Intent(this, SettingsNavigationActivity.class));
			return true;
		} else {
			super.onPreferenceClick(preference);
		}
		return false;
	}
}