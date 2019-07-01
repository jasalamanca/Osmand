package net.osmand.plus.firstusage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;

public class FirstUsageWelcomeFragment extends Fragment {
	public static final String TAG = "FirstUsageWelcomeFragment";
	public static boolean SHOW = true;
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.first_usage_welcome_fragment, container, false);
		ImageView backgroundImage = view.findViewById(R.id.background_image);
		backgroundImage.setImageResource(R.drawable.bg_first_usage);

		Button skipButton = view.findViewById(R.id.start_button);
		skipButton.setOnClickListener(v -> FirstUsageWizardFragment.startWizard(getActivity()));
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		((MapActivity)getActivity()).disableDrawer();
	}

	@Override
	public void onPause() {
		super.onPause();
		((MapActivity)getActivity()).enableDrawer();
	}
}
