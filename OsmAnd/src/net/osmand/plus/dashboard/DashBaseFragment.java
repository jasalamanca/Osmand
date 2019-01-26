package net.osmand.plus.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import net.osmand.plus.OsmAndAppCustomization;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.widgets.InterceptorFrameLayout;
import net.osmand.plus.widgets.tools.SwipeDismissTouchListener;

public abstract class DashBaseFragment extends Fragment {
	protected DashboardOnMap dashboard;

	public interface DismissListener {
		void onDismiss();
	}

	protected OsmandApplication getMyApplication() {
		if (getActivity() == null) {
			return null;
		}
		return (OsmandApplication) getActivity().getApplication();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof MapActivity) {
			dashboard = ((MapActivity) context).getDashboard();
			dashboard.onAttach(this);
		}
	}

	@NonNull
	@Override
	final public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
								   @Nullable Bundle savedInstanceState) {
		View childView = initView(inflater, container, savedInstanceState);
		FrameLayout.LayoutParams layoutParams =
				new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
		InterceptorFrameLayout frameLayout = new InterceptorFrameLayout(getActivity());
		frameLayout.setLayoutParams(layoutParams);

		FrameLayout.LayoutParams childLayoutParams =
				new FrameLayout.LayoutParams(
						(ViewGroup.MarginLayoutParams) childView.getLayoutParams());
		frameLayout.addView(childView, childLayoutParams);

		if (isDismissAllowed()) {
			SwipeDismissTouchListener listener = new SwipeDismissTouchListener(childView, null,
					new SwipeDismissTouchListener.DismissCallbacks() {
						@Override
						public boolean canDismiss(Object token) {
							return true;
						}

						@Override
						public void onDismiss(View view, Object token, boolean isSwipeRight) {
							getDismissCallback().onDismiss();
						}
					});
			frameLayout.setOnTouchListener(listener);
			frameLayout.setListener(listener);
			if (getDismissCallback() == null) {
				defaultDismissListener = new DefaultDismissListener(getParentView(), dashboard, getTag(),
						childView);
			}
		}

		return frameLayout;
	}

	protected abstract View initView(LayoutInflater inflater, @Nullable ViewGroup container,
                                     @Nullable Bundle savedInstanceState);

	DismissListener getDismissCallback() {
		return defaultDismissListener;
	}

	private boolean isDismissAllowed() {
		return true;
	}

	public abstract void onOpenDash();

	public void onCloseDash() {
	}

	@Override
	public final void onPause() {
		// use on close 
		super.onPause();
		onCloseDash();
	}

	protected void closeDashboard() {
		dashboard.hideDashboard(false);
	}

	@Override
	public final void onResume() {
		// use on open update
		super.onResume();
		if (dashboard != null && dashboard.isVisible() && getView() != null) {
			onOpenDash();
		}
	}


// --Commented out by Inspection START (13/01/19 17:21):
//	public void onLocationCompassChanged(Location l, double compassValue) {
//	}
// --Commented out by Inspection STOP (13/01/19 17:21)

	@Override
	public void onDetach() {
		super.onDetach();
		if (dashboard != null) {
			dashboard.onDetach(this);
			dashboard = null;
		}
	}

	protected void startFavoritesActivity(int tab) {
		Activity activity = getActivity();
		OsmAndAppCustomization appCustomization = getMyApplication().getAppCustomization();
		final Intent favorites = new Intent(activity, appCustomization.getFavoritesActivity());
		favorites.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		getMyApplication().getSettings().FAVORITES_TAB.set(tab);
		activity.startActivity(favorites);
	}

	View getParentView() {
		return dashboard.getParentView();
	}

	private DismissListener defaultDismissListener;

	public static class DefaultDismissListener implements DismissListener {
		private final View parentView;
		private final DashboardOnMap dashboardOnMap;
		private final String fragmentTag;
		private final View fragmentView;

		DefaultDismissListener(View parentView, DashboardOnMap dashboardOnMap,
							   String fragmentTag, View fragmentView) {
			this.parentView = parentView;
			this.dashboardOnMap = dashboardOnMap;
			this.fragmentTag = fragmentTag;
			this.fragmentView = fragmentView;
		}

		@Override
		public void onDismiss() {
			dashboardOnMap.blacklistFragmentByTag(fragmentTag);
			fragmentView.setTranslationX(0);
			fragmentView.setAlpha(1);
			Snackbar.make(parentView, dashboardOnMap.getMyApplication().getResources()
					.getString(R.string.shared_string_card_was_hidden), Snackbar.LENGTH_LONG)
					.setAction(R.string.shared_string_undo, new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							DefaultDismissListener.this.onUndo();
						}
					})
					.show();
		}

		void onUndo() {
			dashboardOnMap.unblacklistFragmentClass(fragmentTag);
			fragmentView.setTranslationX(0);
			fragmentView.setAlpha(1);
		}
	}
}
