package net.osmand.plus.search.listitems;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.View;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;

public class QuickSearchButtonListItem extends QuickSearchListItem {

	private final int iconId;
	private String title;
	private Spannable spannableTitle;
	private final View.OnClickListener onClickListener;
	private final int colorId;

	public QuickSearchButtonListItem(OsmandApplication app, int iconId, @NonNull String title, View.OnClickListener onClickListener) {
		super(app, null);
		this.iconId = iconId;
		this.title = title.toUpperCase();
		this.onClickListener = onClickListener;
		this.colorId = app.getSettings().isLightContent() ? R.color.color_dialog_buttons_light : R.color.color_dialog_buttons_dark;
	}

	public QuickSearchButtonListItem(OsmandApplication app, int iconId, @NonNull Spannable title, View.OnClickListener onClickListener) {
		super(app, null);
		this.iconId = iconId;
		this.spannableTitle = spannedToUpperCase(title);
		this.onClickListener = onClickListener;
		this.colorId = app.getSettings().isLightContent() ? R.color.color_dialog_buttons_light : R.color.color_dialog_buttons_dark;
	}

	public QuickSearchListItemType getType() {
		return QuickSearchListItemType.BUTTON;
	}

	@Override
	public Drawable getIcon() {
		if (iconId != 0) {
			return app.getIconsCache().getIcon(iconId, colorId);
		} else {
			return null;
		}
	}

	@Override
	public String getName() {
		return title;
	}

	@Override
	public Spannable getSpannableName() {
		return spannableTitle;
	}

	public View.OnClickListener getOnClickListener() {
		return onClickListener;
	}

	private static Spannable spannedToUpperCase(@NonNull Spanned s) {
		Object[] spans = s.getSpans(0, s.length(), Object.class);
		SpannableString spannableString = new SpannableString(s.toString().toUpperCase());

		// reapply the spans to the now uppercase string
		for (Object span : spans) {
			spannableString.setSpan(span, s.getSpanStart(span), s.getSpanEnd(span), s.getSpanFlags(span));
		}
		return spannableString;
	}
}
