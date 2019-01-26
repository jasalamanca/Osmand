package net.osmand.plus.mapcontextmenu.editors;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.BaseOsmAndFragment;
import net.osmand.plus.helpers.AndroidUiHelper;
import net.osmand.plus.widgets.AutoCompleteTextViewEx;
import net.osmand.util.Algorithms;

public abstract class PointEditorFragment extends BaseOsmAndFragment {
	private View view;
	private EditText nameEdit;
	private boolean cancelled;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.point_editor_fragment, container, false);
		AndroidUtils.addStatusBarPadding21v(getActivity(), view);

		getEditor().updateLandscapePortrait();
		getEditor().updateNightMode();

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		toolbar.setTitle(getToolbarTitle());
		toolbar.setNavigationIcon(getMyApplication().getIconsCache().getIcon(R.drawable.ic_arrow_back));
		toolbar.setNavigationContentDescription(R.string.access_shared_string_navigate_up);
		toolbar.setTitleTextColor(getResources().getColor(getResIdFromAttribute(getMapActivity(), R.attr.pstsTextColor)));
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		Button saveButton = view.findViewById(R.id.save_button);
		saveButton.setTextColor(getResources().getColor(!getEditor().isLight() ? R.color.osmand_orange : R.color.map_widget_blue));
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				savePressed();
			}
		});

		Button cancelButton = view.findViewById(R.id.cancel_button);
		cancelButton.setTextColor(getResources().getColor(!getEditor().isLight() ? R.color.osmand_orange : R.color.map_widget_blue));
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelled = true;
				dismiss();
			}
		});

		Button deleteButton = view.findViewById(R.id.delete_button);
		deleteButton.setTextColor(getResources().getColor(!getEditor().isLight() ? R.color.osmand_orange : R.color.map_widget_blue));
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				deletePressed();
			}
		});

		if (getEditor().isNew()) {
			deleteButton.setVisibility(View.GONE);
		} else {
			deleteButton.setVisibility(View.VISIBLE);
		}

		view.findViewById(R.id.background_layout).setBackgroundResource(!getEditor().isLight() ? R.color.ctx_menu_info_view_bg_dark : R.color.ctx_menu_info_view_bg_light);
		view.findViewById(R.id.buttons_layout).setBackgroundResource(!getEditor().isLight() ? R.color.ctx_menu_info_view_bg_dark : R.color.ctx_menu_info_view_bg_light);
		view.findViewById(R.id.title_view).setBackgroundResource(!getEditor().isLight() ? R.color.bg_color_dark : R.color.bg_color_light);
		view.findViewById(R.id.description_info_view).setBackgroundResource(!getEditor().isLight() ? R.color.ctx_menu_info_view_bg_dark : R.color.ctx_menu_info_view_bg_light);

		TextView nameCaption = view.findViewById(R.id.name_caption);
		AndroidUtils.setTextSecondaryColor(view.getContext(), nameCaption, !getEditor().isLight());
		nameCaption.setText(getNameCaption());
		TextView categoryCaption = view.findViewById(R.id.category_caption);
		AndroidUtils.setTextSecondaryColor(view.getContext(), categoryCaption, !getEditor().isLight());
		categoryCaption.setText(getCategoryCaption());

		nameEdit = view.findViewById(R.id.name_edit);
		AndroidUtils.setTextPrimaryColor(view.getContext(), nameEdit, !getEditor().isLight());
		AndroidUtils.setHintTextSecondaryColor(view.getContext(), nameEdit, !getEditor().isLight());
		nameEdit.setText(getNameInitValue());
		AutoCompleteTextViewEx categoryEdit = view.findViewById(R.id.category_edit);
		AndroidUtils.setTextPrimaryColor(view.getContext(), categoryEdit, !getEditor().isLight());
		categoryEdit.setText(getCategoryInitValue());
		categoryEdit.setFocusable(false);
		categoryEdit.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(final View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					DialogFragment dialogFragment =
							createSelectCategoryDialog();
					dialogFragment.show(getChildFragmentManager(), SelectCategoryDialogFragment.TAG);
					return true;
				}
				return false;
			}
		});

		final EditText descriptionEdit = view.findViewById(R.id.description_edit);
		AndroidUtils.setTextPrimaryColor(view.getContext(), descriptionEdit, !getEditor().isLight());
		AndroidUtils.setHintTextSecondaryColor(view.getContext(), descriptionEdit, !getEditor().isLight());
		if (getDescriptionInitValue() != null) {
			descriptionEdit.setText(getDescriptionInitValue());
		}

		view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if (descriptionEdit.isFocused()) {
					ScrollView scrollView = view.findViewById(R.id.editor_scroll_view);
					scrollView.scrollTo(0, bottom);
				}
				if (Build.VERSION.SDK_INT >= 21 && AndroidUiHelper.isOrientationPortrait(getActivity())) {
					Rect rect = new Rect();
					getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
					int heightDiff = getResources().getDisplayMetrics().heightPixels - rect.bottom;
					view.findViewById(R.id.buttons_container).setPadding(0, 0, 0, heightDiff);
				}
			}
		});

		ImageView nameImage = view.findViewById(R.id.name_image);
		nameImage.setImageDrawable(getNameIcon());
		ImageView categoryImage = view.findViewById(R.id.category_image);
		categoryImage.setImageDrawable(getCategoryIcon());

		ImageView descriptionImage = view.findViewById(R.id.description_image);
		descriptionImage.setImageDrawable(getRowIcon(R.drawable.ic_action_note_dark));

		if (getMyApplication().accessibilityEnabled()) {
			nameCaption.setFocusable(true);
			categoryCaption.setFocusable(true);
			nameEdit.setHint(R.string.access_hint_enter_name);
			categoryEdit.setHint(R.string.access_hint_enter_category);
			descriptionEdit.setHint(R.string.access_hint_enter_description);
		}

		return view;
	}

	DialogFragment createSelectCategoryDialog() {
		return SelectCategoryDialogFragment.createInstance(getEditor().getFragmentTag());
	}

	private Drawable getRowIcon(int iconId) {
		return getIcon(iconId, getEditor().isLight() ? R.color.icon_color : R.color.icon_color_light);
	}

	@Override
	public void onStart() {
		super.onStart();
		getMapActivity().getContextMenu().setBaseFragmentVisibility(false);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (getEditor().isNew()) {
			nameEdit.selectAll();
			nameEdit.requestFocus();
			AndroidUtils.softKeyboardDelayed(nameEdit);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		hideKeyboard();
		getMapActivity().getContextMenu().setBaseFragmentVisibility(true);
	}

	@Override
	public void onDestroyView() {
		if (!wasSaved() && !getEditor().isNew() && !cancelled) {
			save(false);
		}
		super.onDestroyView();
	}

	@Override
	public int getStatusBarColorId() {
		return R.color.status_bar_light;
	}

	private void hideKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			View currentFocus = getActivity().getCurrentFocus();
			if (currentFocus != null) {
				IBinder windowToken = currentFocus.getWindowToken();
				if (windowToken != null) {
					inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
				}
			}
		}
	}

	private void savePressed() {
		save(true);
	}

	private void deletePressed() {
		delete(true);
	}

	protected abstract boolean wasSaved();
	protected abstract void save(boolean needDismiss);
	protected abstract void delete(boolean needDismiss);

	private static int getResIdFromAttribute(final Context ctx, final int attr) {
		if (attr == 0)
			return 0;
		final TypedValue typedvalueattr = new TypedValue();
		ctx.getTheme().resolveAttribute(attr, typedvalueattr, true);
		return typedvalueattr.resourceId;
	}

	protected abstract PointEditor getEditor();
	protected abstract String getToolbarTitle();

	public void setCategory(String name) {
		AutoCompleteTextViewEx categoryEdit = view.findViewById(R.id.category_edit);
		String n = name.length() == 0 ? getDefaultCategoryName() : name;
		categoryEdit.setText(n);
		ImageView categoryImage = view.findViewById(R.id.category_image);
		categoryImage.setImageDrawable(getCategoryIcon());
		ImageView nameImage = view.findViewById(R.id.name_image);
		nameImage.setImageDrawable(getNameIcon());
	}

	String getDefaultCategoryName() {
		return getString(R.string.shared_string_none);
	}
	MapActivity getMapActivity() {
		return (MapActivity)getActivity();
	}

	protected OsmandApplication getMyApplication() {
		if (getActivity() == null) {
			return null;
		}
		return (OsmandApplication) getActivity().getApplication();
	}

	public void dismiss() {
		dismiss(false);
	}

	void dismiss(boolean includingMenu) {
		if (includingMenu) {
			getMapActivity().getSupportFragmentManager().popBackStack();
			getMapActivity().getContextMenu().close();
		} else {
			getMapActivity().getSupportFragmentManager().popBackStack();
		}
	}

	private String getNameCaption() {
		return getMapActivity().getResources().getString(R.string.shared_string_name);
	}
	private String getCategoryCaption() {
		return getMapActivity().getResources().getString(R.string.favourites_edit_dialog_category);
	}

	protected abstract String getNameInitValue();
	protected abstract String getCategoryInitValue();
	protected abstract String getDescriptionInitValue();

	protected abstract Drawable getNameIcon();
	protected abstract Drawable getCategoryIcon();

	String getNameTextValue() {
		EditText nameEdit = view.findViewById(R.id.name_edit);
		return nameEdit.getText().toString().trim();
	}

	String getCategoryTextValue() {
		AutoCompleteTextViewEx categoryEdit = view.findViewById(R.id.category_edit);
		String name = categoryEdit.getText().toString().trim();
		return name.equals(getDefaultCategoryName()) ? "" : name;
	}

	String getDescriptionTextValue() {
		EditText descriptionEdit = view.findViewById(R.id.description_edit);
		String res = descriptionEdit.getText().toString().trim();
		return Algorithms.isEmpty(res) ? null : res;
	}

	Drawable getPaintedIcon(int iconId, int color) {
		return getPaintedContentIcon(iconId, color);
	}
}