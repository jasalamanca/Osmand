package net.osmand.plus.views.mapwidgets;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.views.OsmandMapLayer.DrawSettings;
import android.app.Activity;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TextInfoWidget  {

	private String contentTitle;
	private final View view;
	private final ImageView imageView;
	private final TextView textView;
	private final TextView textViewShadow;
	private final TextView smallTextView;
	private final TextView smallTextViewShadow;
	private final ImageView topImageView;
	final TextView topTextView;
	private boolean explicitlyVisible;
	private final OsmandApplication app;

	private int dayIcon;
	private int nightIcon;
	private boolean isNight;
	private final ViewGroup bottomLayout;


	public TextInfoWidget(Activity activity) {
		app = (OsmandApplication) activity.getApplication();
		view = activity.getLayoutInflater().inflate(R.layout.map_hud_widget, null);
		bottomLayout = view.findViewById(R.id.widget_bottom_layout);
		topImageView = view.findViewById(R.id.widget_top_icon);
		topTextView = view.findViewById(R.id.widget_top_icon_text);
		imageView = view.findViewById(R.id.widget_icon);
		textView = view.findViewById(R.id.widget_text);
		textViewShadow = view.findViewById(R.id.widget_text_shadow);
		smallTextViewShadow = view.findViewById(R.id.widget_text_small_shadow);
		smallTextView = view.findViewById(R.id.widget_text_small);
	}

	public OsmandApplication getOsmandApplication() {
		return app;
	}

	public View getView() {
		return view;
	}
	
	public void setImageDrawable(Drawable imageDrawable) {
		setImageDrawable(imageDrawable, false);
	}
	
	public void setImageDrawable(int res) {
		setImageDrawable(app.getIconsCache().getIcon(res, 0), false);
	}
	
	
	void setImageDrawable(Drawable imageDrawable, boolean gone) {
		if(imageDrawable != null) {
			imageView.setImageDrawable(imageDrawable);
			imageView.setVisibility(View.VISIBLE);
		} else {
			imageView.setVisibility(gone ? View.GONE : View.INVISIBLE);
		}
		imageView.invalidate();
	}
	
	void setTopImageDrawable(Drawable imageDrawable, String topText) {
		if(imageDrawable != null) {
			topImageView.setImageDrawable(imageDrawable);
			topImageView.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) bottomLayout.getLayoutParams();
			lp.gravity = Gravity.CENTER_HORIZONTAL;
			bottomLayout.setLayoutParams(lp);
			bottomLayout.invalidate();
			topTextView.setVisibility(View.VISIBLE);
			topTextView.setText(topText == null ? "" : topText);
		} else {
			topImageView.setVisibility(View.GONE );
			topTextView.setVisibility(View.GONE );
			LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) bottomLayout.getLayoutParams();
			lp.gravity = Gravity.NO_GRAVITY;
			bottomLayout.setLayoutParams(lp);
		}
		
		topTextView.invalidate();
		topImageView.invalidate();
	}
	
	public boolean setIcons(int widgetDayIcon, int widgetNightIcon) {
		if (dayIcon != widgetDayIcon || nightIcon != widgetNightIcon) {
			dayIcon = widgetDayIcon;
			nightIcon = widgetNightIcon;
			setImageDrawable(!isNight ? dayIcon : nightIcon);
			return true;
		} else {
			return false;
		}
	}

	boolean isNight() {
		return isNight;
	}

	private CharSequence combine(CharSequence text, CharSequence subtext) {
		if (TextUtils.isEmpty(text)) {
			return subtext;
		} else if (TextUtils.isEmpty(subtext)) {
			return text;
		}
		return text + " " + subtext; //$NON-NLS-1$
	}

	void setContentDescription(CharSequence text) {
		view.setContentDescription(combine(contentTitle, text));
	}
	
	public void setContentTitle(int messageId) {
		setContentTitle(view.getContext().getString(messageId));
	}

	public void setContentTitle(String text) {
		contentTitle = text;
		setContentDescription(combine(textView.getText(), smallTextView.getText()));
	}
	
	public void setText(String text, String subtext) {
		setTextNoUpdateVisibility(text, subtext);
		updateVisibility(text != null);
	}

	void setTextNoUpdateVisibility(String text, String subtext) {
		setContentDescription(combine(text, subtext));
//		if(this.text != null && this.text.length() > 7) {
//			this.text = this.text.substring(0, 6) +"..";
//		}
		if(text == null) {
			textView.setText("");
			textViewShadow.setText("");
		} else {
			textView.setText(text);
			textViewShadow.setText(text);
		}
		if(subtext == null) {
			smallTextView.setText("");
			smallTextViewShadow.setText("");
		} else {
			smallTextView.setText(subtext);
			smallTextViewShadow.setText(subtext);
		}
	}
	
	boolean updateVisibility(boolean visible) {
		if (visible != (view.getVisibility() == View.VISIBLE)) {
			if (visible) {
				view.setVisibility(View.VISIBLE);
			} else {
				view.setVisibility(View.GONE);
			}
			view.invalidate();
			if (app.accessibilityEnabled())
				view.setFocusable(visible);
			return true;
		}
		return false;
	}
	
	public boolean isVisible() {
		return view.getVisibility() == View.VISIBLE && view.getParent() != null;
	}

	public boolean updateInfo(DrawSettings drawSettings) {
		return false;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		view.setOnClickListener(onClickListener);
	}

	public void setExplicitlyVisible(boolean explicitlyVisible) {
		this.explicitlyVisible = explicitlyVisible;
	}
	
	public boolean isExplicitlyVisible() {
		return explicitlyVisible;
	}
	
	public void updateIconMode(boolean night) {
		isNight = night;
		if(dayIcon != 0) {
			setImageDrawable(!night? dayIcon : nightIcon);
		}
	}

	public void updateTextColor(int textColor, int textShadowColor, boolean bold, int rad) {
		updateTextColor(smallTextView, smallTextViewShadow, textColor, textShadowColor, bold, rad);
		updateTextColor(textView, textViewShadow, textColor, textShadowColor, bold, rad);
		updateTextColor(topTextView, null, textColor, textShadowColor, bold, rad);
	}
	
	public static void updateTextColor(TextView tv, TextView shadow, int textColor, int textShadowColor, boolean textBold, int rad) {
		if(shadow != null) {
			if(rad > 0) {
				shadow.setVisibility(View.VISIBLE);
				shadow.setTypeface(Typeface.DEFAULT, textBold ? Typeface.BOLD : Typeface.NORMAL);
				shadow.getPaint().setStrokeWidth(rad);
				shadow.getPaint().setStyle(Style.STROKE);
				shadow.setTextColor(textShadowColor);
//				tv.getPaint().setStyle(Style.FILL);
			} else {
//				tv.getPaint().setStyle(Style.FILL_AND_STROKE);
				shadow.setVisibility(View.GONE);
			}
		}
		tv.setTextColor(textColor);
		tv.setTypeface(Typeface.DEFAULT, textBold ? Typeface.BOLD : Typeface.NORMAL);
	}
}
