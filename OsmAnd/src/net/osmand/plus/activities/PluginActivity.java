package net.osmand.plus.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;

public class PluginActivity extends OsmandActionBarActivity {
	private static final String TAG = "PluginActivity";
	public static final String EXTRA_PLUGIN_ID = "plugin_id";

	private OsmandPlugin plugin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent == null || !intent.hasExtra(EXTRA_PLUGIN_ID)) {
			Log.e(TAG, "Required extra '" + EXTRA_PLUGIN_ID + "' is missing");
			finish();
			return;
		}
		String pluginId = intent.getStringExtra(EXTRA_PLUGIN_ID);
		if (pluginId == null) {
			Log.e(TAG, "Extra '" + EXTRA_PLUGIN_ID + "' is null");
			finish();
			return;
		}
		for (OsmandPlugin plugin : OsmandPlugin.getAvailablePlugins()) {
			if (!plugin.getId().equals(pluginId))
				continue;

			this.plugin = plugin;
			break;
		}
		if (plugin == null) {
			Log.e(TAG, "Plugin '" + EXTRA_PLUGIN_ID + "' not found");
			finish();
			return;
		}

		setContentView(R.layout.plugin);
		//noinspection ConstantConditions
		getSupportActionBar().setTitle(plugin.getName());
		if(plugin.getAssetResourceName() != 0) {
			ImageView img = findViewById(R.id.plugin_image);
			img.setImageResource(plugin.getAssetResourceName());
		}

		TextView descriptionView = findViewById(R.id.plugin_description);
		descriptionView.setText(plugin.getDescription());

		Button settingsButton = findViewById(R.id.plugin_settings);
		settingsButton.setOnClickListener(view -> startActivity(new Intent(PluginActivity.this, plugin.getSettingsActivity())));

		CompoundButton enableDisableButton = findViewById(R.id.plugin_enable_disable);
		enableDisableButton.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (plugin.isActive() == isChecked) {
                        return;
                    }

                    boolean ok = OsmandPlugin.enablePlugin(PluginActivity.this, (OsmandApplication)getApplication(),
                            plugin, isChecked);
                    if (!ok) {
                        return;
                    }
                    updateState();
                });
		Button getButton = findViewById(R.id.plugin_get);
		getButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(plugin.getInstallURL())));
            } catch (Exception e) {
                //ignored
            }
        });

		updateState();
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateState();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
			case android.R.id.home:
				finish();
				return true;

		}
		return false;
	}

	private void updateState() {
		CompoundButton enableDisableButton = findViewById(R.id.plugin_enable_disable);
		Button getButton = findViewById(R.id.plugin_get);
		Button settingsButton = findViewById(R.id.plugin_settings);
		settingsButton.setCompoundDrawablesWithIntrinsicBounds(
				getMyApplication().getIconsCache().getThemedIcon(R.drawable.ic_action_settings),
				null, null, null);
		View installHeader = findViewById(R.id.plugin_install_header);

		if (plugin.needsInstallation()) {
			getButton.setVisibility(View.VISIBLE);
			enableDisableButton.setVisibility(View.GONE);
			settingsButton.setVisibility(View.GONE);
			installHeader.setVisibility(View.VISIBLE);
			View worldGlobeIcon = installHeader.findViewById(R.id.ic_world_globe);
			Drawable worldGlobeDrawable = getMyApplication().getIconsCache().getThemedIcon(
					R.drawable.ic_world_globe_dark);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				worldGlobeIcon.setBackground(worldGlobeDrawable);
			} else {
				//noinspection deprecation
				worldGlobeIcon.setBackgroundDrawable(worldGlobeDrawable);
			}
		} else {
			getButton.setVisibility(View.GONE);
			enableDisableButton.setVisibility(View.VISIBLE);
			enableDisableButton.setChecked(plugin.isActive());

			final Class<? extends Activity> settingsActivity = plugin.getSettingsActivity();
			if (settingsActivity == null || !plugin.isActive()) {
				settingsButton.setVisibility(View.GONE);
			} else {
				settingsButton.setVisibility(View.VISIBLE);
			}

			installHeader.setVisibility(View.GONE);
		}
	}
}
