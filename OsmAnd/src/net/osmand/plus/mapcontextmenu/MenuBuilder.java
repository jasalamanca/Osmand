package net.osmand.plus.mapcontextmenu;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.osmand.AndroidUtils;
import net.osmand.binary.BinaryMapIndexReader;
import net.osmand.data.Amenity;
import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.data.QuadRect;
import net.osmand.osm.PoiCategory;
import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.FontCache;
import net.osmand.plus.render.RenderingIcons;
import net.osmand.plus.widgets.TextViewEx;
import net.osmand.util.Algorithms;
import net.osmand.util.MapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class MenuBuilder {
	static final float SHADOW_HEIGHT_TOP_DP = 17f;
	private static final int TITLE_LIMIT = 60;

	protected final MapActivity mapActivity;
	private MapContextMenu mapContextMenu;
	protected final OsmandApplication app;
	private final LinkedList<PlainMenuItem> plainMenuItems;
	private boolean firstRow;
	private boolean matchWidthDivider;
	protected boolean light;
	private long objectId;
	private LatLon latLon;
	private boolean showTitleIfTruncated = true;
	private boolean showNearestWiki = false;
	protected List<Amenity> nearestWiki = new ArrayList<>();
	private final List<OsmandPlugin> menuPlugins = new ArrayList<>();
	private CollapseExpandListener collapseExpandListener;

	private final String preferredMapLang;
	private String preferredMapAppLang;
	private final boolean transliterateNames;

	public interface CollapseExpandListener {
		void onCollapseExpand();
	}

	class PlainMenuItem {
		private final int iconId;
		private final String buttonText;
		private final String text;
		private final boolean needLinks;
		private final boolean url;
		private final boolean collapsable;
		private final CollapsableView collapsableView;
		private final OnClickListener onClickListener;

		PlainMenuItem(int iconId, String buttonText, String text, boolean needLinks, boolean url,
                      boolean collapsable, CollapsableView collapsableView,
                      OnClickListener onClickListener) {
			this.iconId = iconId;
			this.buttonText = buttonText;
			this.text = text;
			this.needLinks = needLinks;
			this.url = url;
			this.collapsable = collapsable;
			this.collapsableView = collapsableView;
			this.onClickListener = onClickListener;
		}

		int getIconId() {
			return iconId;
		}
		String getButtonText() {
			return buttonText;
		}
		String getText() {
			return text;
		}
		boolean isNeedLinks() {
			return needLinks;
		}
		boolean isUrl() {
			return url;
		}
		OnClickListener getOnClickListener() {
			return onClickListener;
		}
	}

	public static class CollapsableView {
		private final View contenView;
		private final MenuBuilder menuBuilder;
//		private OsmandPreference<Boolean> collapsedPref;
		private boolean collapsed;
//		private CollapseExpandListener collapseExpandListener;

		public CollapsableView(@NonNull View contenView, @NonNull MenuBuilder menuBuilder, boolean collapsed) {
			this.contenView = contenView;
			this.collapsed = collapsed;
			this.menuBuilder = menuBuilder;
		}

		public View getContenView() {
			return contenView;
		}

		public boolean isCollapsed() {
//			if (collapsedPref != null) {
//				return collapsedPref.get();
//			} else {
				return collapsed;
//			}
		}

		public void setCollapsed(boolean collapsed) {
//			if (collapsedPref != null) {
//				collapsedPref.set(collapsed);
//			} else {
				this.collapsed = collapsed;
//			}
//			if (collapseExpandListener != null) {
//				collapseExpandListener.onCollapseExpand(collapsed);
//			}
			if (menuBuilder.collapseExpandListener != null) {
				menuBuilder.collapseExpandListener.onCollapseExpand();
			}
		}
	}

	public MenuBuilder(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
		this.app = mapActivity.getMyApplication();
		this.plainMenuItems = new LinkedList<>();

		preferredMapLang = app.getSettings().MAP_PREFERRED_LOCALE.get();
		preferredMapAppLang = preferredMapLang;
		if (Algorithms.isEmpty(preferredMapAppLang)) {
			preferredMapAppLang = app.getLanguage();
		}
		transliterateNames = app.getSettings().MAP_TRANSLITERATE_NAMES.get();
	}

	void setCollapseExpandListener(CollapseExpandListener collapseExpandListener) {
		this.collapseExpandListener = collapseExpandListener;
	}

	String getPreferredMapLang() {
		return preferredMapLang;
	}
	protected String getPreferredMapAppLang() {
		return preferredMapAppLang;
	}
	boolean isTransliterateNames() {
		return transliterateNames;
	}
	protected MapActivity getMapActivity() {
		return mapActivity;
	}

	protected LatLon getLatLon() {
		return latLon;
	}
	public void setLatLon(LatLon objectLocation) {
		this.latLon = objectLocation;
	}

	void setMapContextMenu(MapContextMenu mapContextMenu) {
		this.mapContextMenu = mapContextMenu;
	}

	public void setShowNearestWiki(boolean showNearestWiki) {
		this.showNearestWiki = showNearestWiki;
	}
	public void setShowTitleIfTruncated(boolean showTitleIfTruncated) {
		this.showTitleIfTruncated = showTitleIfTruncated;
	}

	protected void setShowNearestWiki(boolean showNearestWiki, long objectId) {
		this.objectId = objectId;
		this.showNearestWiki = showNearestWiki;
	}

	void addMenuPlugin(OsmandPlugin plugin) {
		menuPlugins.add(plugin);
	}
	public void setLight(boolean light) {
		this.light = light;
	}

	public void build(View view) {
		firstRow = true;
		if (showTitleIfTruncated) {
			buildTitleRow(view);
		}
		buildNearestWikiRow(view);
		if (needBuildPlainMenuItems()) {
			buildPlainMenuItems(view);
		}
		buildInternal(view);
		buildPluginRows(view);
	}

	void onHide() {
	}
	void onClose() {
		clearPluginRows();
	}

	protected void buildPlainMenuItems(View view) {
		for (PlainMenuItem item : plainMenuItems) {
			buildRow(view, item.getIconId(), item.getButtonText(), item.getText(), 0, item.collapsable, item.collapsableView,
					item.isNeedLinks(), 0, item.isUrl(), item.getOnClickListener(), false);
		}
	}

	protected boolean needBuildPlainMenuItems() {
		return true;
	}

	private void buildPluginRows(View view) {
		for (OsmandPlugin plugin : menuPlugins) {
			plugin.buildContextMenuRows(this, view);
		}
	}

	private void clearPluginRows() {
		for (OsmandPlugin plugin : menuPlugins) {
			plugin.clearContextMenuRows();
		}
	}

	private void buildTitleRow(View view) {
		if (mapContextMenu != null) {
			String title = mapContextMenu.getTitleStr();
			if (title.length() > TITLE_LIMIT) {
				buildRow(view, R.drawable.ic_action_note_dark, null, title, 0, false, null, false, 0, false, null, false);
			}
		}
	}

	protected void buildNearestWikiRow(View view) {
		if (processNearstWiki() && nearestWiki.size() > 0) {
			buildRow(view, R.drawable.ic_action_wikipedia, null, app.getString(R.string.wiki_around) + " (" + nearestWiki.size()+")", 0,
					true, getCollapsableWikiView(view.getContext(), true),
					false, 0, false, null, false);
		}
	}

	protected void buildInternal(View view) {
	}
	protected boolean isFirstRow() {
		return firstRow;
	}
	protected void rowBuilt() {
		firstRow = false;
	}

	protected View buildRow(View view, int iconId, String buttonText, String text, int textColor,
                            boolean collapsable, final CollapsableView collapsableView,
                            boolean needLinks, int textLinesLimit, boolean isUrl, OnClickListener onClickListener, boolean matchWidthDivider) {
		return buildRow(view, iconId == 0 ? null : getRowIcon(iconId), buttonText, text, textColor, null, collapsable, collapsableView,
				needLinks, textLinesLimit, isUrl, onClickListener, matchWidthDivider);
	}

	protected View buildRow(final View view, Drawable icon, final String buttonText, final String text, int textColor, String secondaryText,
                            boolean collapsable, final CollapsableView collapsableView, boolean needLinks,
                            int textLinesLimit, boolean isUrl, OnClickListener onClickListener, boolean matchWidthDivider) {

		if (!isFirstRow()) {
			buildRowDivider(view);
		}

		LinearLayout baseView = new LinearLayout(view.getContext());
		baseView.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams llBaseViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		baseView.setLayoutParams(llBaseViewParams);

		LinearLayout ll = new LinearLayout(view.getContext());
		ll.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		ll.setLayoutParams(llParams);
		ll.setBackgroundResource(AndroidUtils.resolveAttribute(view.getContext(), android.R.attr.selectableItemBackground));
		ll.setOnLongClickListener(v -> {
			copyToClipboard(text, view.getContext());
			return true;
		});

		baseView.addView(ll);

		// Icon
		if (icon != null) {
			LinearLayout llIcon = new LinearLayout(view.getContext());
			llIcon.setOrientation(LinearLayout.HORIZONTAL);
			llIcon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(64f), dpToPx(48f)));
			llIcon.setGravity(Gravity.CENTER_VERTICAL);
			ll.addView(llIcon);

			ImageView iconView = new ImageView(view.getContext());
			LinearLayout.LayoutParams llIconParams = new LinearLayout.LayoutParams(dpToPx(24f), dpToPx(24f));
			llIconParams.setMargins(dpToPx(16f), dpToPx(12f), dpToPx(24f), dpToPx(12f));
			llIconParams.gravity = Gravity.CENTER_VERTICAL;
			iconView.setLayoutParams(llIconParams);
			iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			iconView.setImageDrawable(icon);
			llIcon.addView(iconView);
		}

		// Text
		LinearLayout llText = new LinearLayout(view.getContext());
		llText.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams llTextViewParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		llTextViewParams.weight = 1f;
		llTextViewParams.setMargins(0, 0, dpToPx(10f), 0);
		llTextViewParams.gravity = Gravity.CENTER_VERTICAL;
		llText.setLayoutParams(llTextViewParams);
		ll.addView(llText);

		// Primary text
		TextViewEx textView = new TextViewEx(view.getContext());
		LinearLayout.LayoutParams llTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		llTextParams.setMargins(icon != null ? 0 : dpToPx(16f), dpToPx(secondaryText != null ? 10f : 8f), 0, dpToPx(secondaryText != null ? 6f : 8f));
		textView.setLayoutParams(llTextParams);
		textView.setTypeface(FontCache.getRobotoRegular(view.getContext()));
		textView.setTextSize(16);
		textView.setTextColor(app.getResources().getColor(light ? R.color.ctx_menu_bottom_view_text_color_light : R.color.ctx_menu_bottom_view_text_color_dark));

		int linkTextColor = ContextCompat.getColor(view.getContext(), light ? R.color.ctx_menu_bottom_view_url_color_light : R.color.ctx_menu_bottom_view_url_color_dark);

		if (isUrl) {
			textView.setTextColor(linkTextColor);
		} else if (needLinks) {
			Linkify.addLinks(textView, Linkify.ALL);
			textView.setLinksClickable(true);
			textView.setLinkTextColor(linkTextColor);
			AndroidUtils.removeLinkUnderline(textView);
		}
		if (textLinesLimit > 0) {
			textView.setMinLines(1);
			textView.setMaxLines(textLinesLimit);
		}
		textView.setText(text);
		if (textColor > 0) {
			textView.setTextColor(view.getResources().getColor(textColor));
		}
		llText.addView(textView);

		// Secondary text
		if (!TextUtils.isEmpty(secondaryText)) {
			TextViewEx textViewSecondary = new TextViewEx(view.getContext());
			LinearLayout.LayoutParams llTextSecondaryParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			llTextSecondaryParams.setMargins(icon != null ? 0 : dpToPx(16f), 0, 0, dpToPx(6f));
			textViewSecondary.setLayoutParams(llTextSecondaryParams);
			textViewSecondary.setTypeface(FontCache.getRobotoRegular(view.getContext()));
			textViewSecondary.setTextSize(14);
			textViewSecondary.setTextColor(app.getResources().getColor(light ? R.color.ctx_menu_bottom_view_secondary_text_color_light: R.color.ctx_menu_bottom_view_secondary_text_color_dark));
			textViewSecondary.setText(secondaryText);
			llText.addView(textViewSecondary);
		}

		//Button
		if (!TextUtils.isEmpty(buttonText)) {
			TextViewEx buttonTextView = new TextViewEx(view.getContext());
			LinearLayout.LayoutParams buttonTextViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			buttonTextViewParams.gravity = Gravity.CENTER_VERTICAL;
			buttonTextViewParams.setMargins(dpToPx(8), 0, dpToPx(8), 0);
			buttonTextView.setLayoutParams(buttonTextViewParams);
			buttonTextView.setTypeface(FontCache.getRobotoMedium(view.getContext()));
			buttonTextView.setAllCaps(true);
			buttonTextView.setTextColor(ContextCompat.getColor(view.getContext(), !light ? R.color.ctx_menu_controller_button_text_color_dark_n : R.color.ctx_menu_controller_button_text_color_light_n));
			buttonTextView.setText(buttonText);
			ll.addView(buttonTextView);
		}

		final ImageView iconViewCollapse = new ImageView(view.getContext());
		if (collapsable && collapsableView != null) {
			// Icon
			LinearLayout llIconCollapse = new LinearLayout(view.getContext());
			llIconCollapse.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(40f), dpToPx(48f)));
			llIconCollapse.setOrientation(LinearLayout.HORIZONTAL);
			llIconCollapse.setGravity(Gravity.CENTER_VERTICAL);
			ll.addView(llIconCollapse);

			LinearLayout.LayoutParams llIconCollapseParams = new LinearLayout.LayoutParams(dpToPx(24f), dpToPx(24f));
			llIconCollapseParams.setMargins(0, dpToPx(12f), dpToPx(24f), dpToPx(12f));
			llIconCollapseParams.gravity = Gravity.CENTER_VERTICAL;
			iconViewCollapse.setLayoutParams(llIconCollapseParams);
			iconViewCollapse.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			iconViewCollapse.setImageDrawable(getCollapseIcon(collapsableView.getContenView().getVisibility() == View.GONE));
			llIconCollapse.addView(iconViewCollapse);
			ll.setOnClickListener(v -> {
				if (collapsableView.getContenView().getVisibility() == View.VISIBLE) {
					collapsableView.getContenView().setVisibility(View.GONE);
					iconViewCollapse.setImageDrawable(getCollapseIcon(true));
					collapsableView.setCollapsed(true);
				} else {
					collapsableView.getContenView().setVisibility(View.VISIBLE);
					iconViewCollapse.setImageDrawable(getCollapseIcon(false));
					collapsableView.setCollapsed(false);
				}
			});
			if (collapsableView.isCollapsed()) {
				collapsableView.getContenView().setVisibility(View.GONE);
				iconViewCollapse.setImageDrawable(getCollapseIcon(true));
			}
			baseView.addView(collapsableView.getContenView());
		}

		if (onClickListener != null) {
			ll.setOnClickListener(onClickListener);
		} else if (isUrl) {
			ll.setOnClickListener(v -> {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(text));
				v.getContext().startActivity(intent);
			});
		}

		((LinearLayout) view).addView(baseView);

		rowBuilt();

		setDividerWidth(matchWidthDivider);

		return ll;
	}

	protected void setDividerWidth(boolean matchWidthDivider) {
		this.matchWidthDivider = matchWidthDivider;
	}

	protected void copyToClipboard(String text, Context ctx) {
		((ClipboardManager) app.getSystemService(Activity.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("", text));
		Toast.makeText(ctx,
				ctx.getResources().getString(R.string.copied_to_clipboard) + ":\n" + text,
				Toast.LENGTH_SHORT).show();
	}

	protected void buildRowDivider(View view) {
		View horizontalLine = new View(view.getContext());
		LinearLayout.LayoutParams llHorLineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1f));
		llHorLineParams.gravity = Gravity.BOTTOM;
		if (!matchWidthDivider) {
			llHorLineParams.setMargins(dpToPx(64f), 0, 0, 0);
		}
		horizontalLine.setLayoutParams(llHorLineParams);
		horizontalLine.setBackgroundColor(app.getResources().getColor(light ? R.color.ctx_menu_bottom_view_divider_light : R.color.ctx_menu_bottom_view_divider_dark));
		((LinearLayout) view).addView(horizontalLine);
	}

	public boolean hasCustomAddressLine() {
		return false;
	}
	public void buildCustomAddressLine(LinearLayout ll) {
	}

	public void addPlainMenuItem(int iconId, String text, boolean needLinks, boolean isUrl, OnClickListener onClickListener) {
		plainMenuItems.add(new PlainMenuItem(iconId, null, text, needLinks, isUrl, false, null, onClickListener));
	}

	void addPlainMenuItem(int iconId, String buttonText, String text, boolean needLinks, boolean isUrl, OnClickListener onClickListener) {
		plainMenuItems.add(new PlainMenuItem(iconId, buttonText, text, needLinks, isUrl, false, null, onClickListener));
	}

	void clearPlainMenuItems() {
		plainMenuItems.clear();
	}

	protected Drawable getRowIcon(int iconId) {
		IconsCache iconsCache = app.getIconsCache();
		return iconsCache.getIcon(iconId, light ? R.color.ctx_menu_bottom_view_icon_light : R.color.ctx_menu_bottom_view_icon_dark);
	}

	protected Drawable getRowIcon(Context ctx, String fileName) {
		Drawable d = RenderingIcons.getBigIcon(ctx, fileName);
		if (d != null) {
			d.setColorFilter(app.getResources().getColor(light ? R.color.ctx_menu_bottom_view_icon_light : R.color.ctx_menu_bottom_view_icon_dark), PorterDuff.Mode.SRC_IN);
			return d;
		} else {
			return null;
		}
	}

	protected int dpToPx(float dp) {
		Resources r = app.getResources();
		return (int) TypedValue.applyDimension(
				COMPLEX_UNIT_DIP,
				dp,
				r.getDisplayMetrics()
		);
	}

	protected Drawable getCollapseIcon(boolean collapsed) {
		return app.getIconsCache().getIcon(collapsed ? R.drawable.ic_action_arrow_down : R.drawable.ic_action_arrow_up,
				light ? R.color.ctx_menu_collapse_icon_color_light : R.color.ctx_menu_collapse_icon_color_dark);
	}

	protected CollapsableView getCollapsableTextView(Context context, boolean collapsed, String text) {
		final TextViewEx textView = new TextViewEx(context);
		textView.setVisibility(collapsed ? View.GONE : View.VISIBLE);
		LinearLayout.LayoutParams llTextDescParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		llTextDescParams.setMargins(dpToPx(64f), 0, dpToPx(40f), dpToPx(13f));
		textView.setLayoutParams(llTextDescParams);
		textView.setTypeface(FontCache.getRobotoRegular(context));
		textView.setTextSize(16);
		textView.setTextColor(app.getResources().getColor(light ? R.color.ctx_menu_bottom_view_text_color_light : R.color.ctx_menu_bottom_view_text_color_dark));
		textView.setText(text);
		return new CollapsableView(textView, this, collapsed);
	}

	protected CollapsableView getCollapsableWikiView(Context context, boolean collapsed) {
		LinearLayout view = buildCollapsableContentView(context, collapsed, true);

		for (final Amenity wiki : nearestWiki) {
			TextViewEx button = buildButtonInCollapsableView(context, false, false);
			String name = wiki.getName(preferredMapAppLang, transliterateNames);
			button.setText(name);

			button.setOnClickListener(v -> {
				LatLon latLon = new LatLon(wiki.getLocation().getLatitude(), wiki.getLocation().getLongitude());
				PointDescription pointDescription = mapActivity.getMapLayers().getPoiMapLayer().getObjectName(wiki);
				mapActivity.getContextMenu().show(latLon, pointDescription, wiki);
			});
			view.addView(button);
		}

		return new CollapsableView(view, this, collapsed);
	}

	protected LinearLayout buildCollapsableContentView(Context context, boolean collapsed, boolean needMargin) {
		final LinearLayout view = new LinearLayout(context);
		view.setOrientation(LinearLayout.VERTICAL);
		view.setVisibility(collapsed ? View.GONE : View.VISIBLE);
		LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		if (needMargin) {
			llParams.setMargins(dpToPx(64f), 0, dpToPx(12f), 0);
		}
		view.setLayoutParams(llParams);
		return view;
	}

	protected TextViewEx buildButtonInCollapsableView(Context context, boolean selected, boolean showAll) {
		return buildButtonInCollapsableView(context, selected, showAll, true);
	}

	protected TextViewEx buildButtonInCollapsableView(Context context, boolean selected, boolean showAll, boolean singleLine) {
		TextViewEx button = new TextViewEx(new ContextThemeWrapper(context, light ? R.style.OsmandLightTheme : R.style.OsmandDarkTheme));
		LinearLayout.LayoutParams llWikiButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		llWikiButtonParams.setMargins(0, 0, 0, dpToPx(8f));
		button.setLayoutParams(llWikiButtonParams);
		button.setTypeface(FontCache.getRobotoRegular(context));
		int bg;
		if (selected) {
			bg = light ? R.drawable.context_menu_controller_bg_light_selected: R.drawable.context_menu_controller_bg_dark_selected;
		} else if (showAll) {
			bg = light ? R.drawable.context_menu_controller_bg_light_show_all : R.drawable.context_menu_controller_bg_dark_show_all;
		} else {
			bg = light ? R.drawable.context_menu_controller_bg_light : R.drawable.context_menu_controller_bg_dark;
		}
		button.setBackgroundResource(bg);
		button.setTextSize(14);
		int paddingSides = dpToPx(10f);
		button.setPadding(paddingSides, paddingSides, paddingSides, paddingSides);
		if (!selected) {
			ColorStateList buttonColorStateList = AndroidUtils.createColorStateList(context, !light,
					R.color.ctx_menu_controller_button_text_color_light_n, R.color.ctx_menu_controller_button_text_color_light_p,
					R.color.ctx_menu_controller_button_text_color_dark_n, R.color.ctx_menu_controller_button_text_color_dark_p);
			button.setTextColor(buttonColorStateList);
		} else {
			button.setTextColor(ContextCompat.getColor(context, light ? R.color.ctx_menu_bottom_view_text_color_light : R.color.ctx_menu_bottom_view_text_color_dark));
		}
		button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
		button.setSingleLine(singleLine);
		button.setEllipsize(TextUtils.TruncateAt.END);

		return button;
	}

	protected boolean processNearstWiki() {
		if (showNearestWiki && latLon != null) {
			QuadRect rect = MapUtils.calculateLatLonBbox(
					latLon.getLatitude(), latLon.getLongitude(), 250);
			nearestWiki = app.getResourceManager().searchAmenities(
					new BinaryMapIndexReader.SearchPoiTypeFilter() {
						@Override
						public boolean accept(PoiCategory type, String subcategory) {
							return type.isWiki();
						}

						@Override
						public boolean isEmpty() {
							return false;
						}
					}, rect.top, rect.left, rect.bottom, rect.right, -1, null);
			Collections.sort(nearestWiki, (o1, o2) -> {
				double d1 = MapUtils.getDistance(latLon, o1.getLocation());
				double d2 = MapUtils.getDistance(latLon, o2.getLocation());
				return Double.compare(d1, d2);
			});
			Long id = objectId;
			List<Amenity> wikiList = new ArrayList<>();
			for (Amenity wiki : nearestWiki) {
				String lng = wiki.getContentLanguage("content", preferredMapAppLang, "en");
				if (wiki.getId().equals(id) || (!lng.equals("en") && !lng.equals(preferredMapAppLang))) {
					wikiList.add(wiki);
				}
			}
			nearestWiki.removeAll(wikiList);
			return true;
		}
		return false;
	}
}