package net.osmand.plus.quickaction;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.osmedit.OsmEditingPlugin;
import net.osmand.plus.quickaction.actions.AddOSMBugAction;
import net.osmand.plus.quickaction.actions.AddPOIAction;
import net.osmand.plus.quickaction.actions.FavoriteAction;
import net.osmand.plus.quickaction.actions.GPXAction;
import net.osmand.plus.quickaction.actions.MapStyleAction;
import net.osmand.plus.quickaction.actions.MarkerAction;
import net.osmand.plus.quickaction.actions.NavAddDestinationAction;
import net.osmand.plus.quickaction.actions.NavAddFirstIntermediateAction;
import net.osmand.plus.quickaction.actions.NavAutoZoomMapAction;
import net.osmand.plus.quickaction.actions.NavReplaceDestinationAction;
import net.osmand.plus.quickaction.actions.NavResumePauseAction;
import net.osmand.plus.quickaction.actions.NavStartStopAction;
import net.osmand.plus.quickaction.actions.NavVoiceAction;
import net.osmand.plus.quickaction.actions.NewAction;
import net.osmand.plus.quickaction.actions.ShowHideFavoritesAction;
import net.osmand.plus.quickaction.actions.ShowHideOSMBugAction;
import net.osmand.plus.quickaction.actions.ShowHidePoiAction;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuickActionFactory {

	public String quickActionListToString(List<QuickAction> quickActions) {
		return new Gson().toJson(quickActions);
	}

	public List<QuickAction> parseActiveActionsList(String json) {
		Type type = new TypeToken<List<QuickAction>>() {
		}.getType();
		ArrayList<QuickAction> quickActions = new Gson().fromJson(json, type);
		return quickActions != null ? quickActions : new ArrayList<QuickAction>();
	}

	public static List<QuickAction> produceTypeActionsListWithHeaders(List<QuickAction> active) {
		ArrayList<QuickAction> quickActions = new ArrayList<>();
		quickActions.add(new QuickAction(0, R.string.quick_action_add_create_items));
		quickActions.add(new FavoriteAction());
		quickActions.add(new GPXAction());
		QuickAction marker = new MarkerAction();

		if (!marker.hasInstanceInList(active)) {
			quickActions.add(marker);
		}

		if (OsmandPlugin.getEnabledPlugin(OsmEditingPlugin.class) != null) {
			quickActions.add(new AddPOIAction());
			quickActions.add(new AddOSMBugAction());
		}

		quickActions.add(new QuickAction(0, R.string.quick_action_add_configure_map));

		QuickAction favorites = new ShowHideFavoritesAction();
		if (!favorites.hasInstanceInList(active)) {
			quickActions.add(favorites);
		}
		quickActions.add(new ShowHidePoiAction());
		if (OsmandPlugin.getEnabledPlugin(OsmEditingPlugin.class) != null) {
			QuickAction showHideOSMBugAction = new ShowHideOSMBugAction();
			if (!showHideOSMBugAction.hasInstanceInList(active)) {
				quickActions.add(showHideOSMBugAction);
			}
		}

		quickActions.add(new MapStyleAction());

		QuickAction voice = new NavVoiceAction();
		QuickAction addDestination = new NavAddDestinationAction();
		QuickAction addFirstIntermediate = new NavAddFirstIntermediateAction();
		QuickAction replaceDestination = new NavReplaceDestinationAction();
		QuickAction autoZoomMap = new NavAutoZoomMapAction();
		QuickAction startStopNavigation = new NavStartStopAction();
		QuickAction resumePauseNavigation = new NavResumePauseAction();

		ArrayList<QuickAction> navigationQuickActions = new ArrayList<>();

		if (!voice.hasInstanceInList(active)) {
			navigationQuickActions.add(voice);
		}
		if (!addDestination.hasInstanceInList(active)) {
			navigationQuickActions.add(addDestination);
		}
		if (!addFirstIntermediate.hasInstanceInList(active)) {
			navigationQuickActions.add(addFirstIntermediate);
		}
		if (!replaceDestination.hasInstanceInList(active)) {
			navigationQuickActions.add(replaceDestination);
		}
		if (!autoZoomMap.hasInstanceInList(active)) {
			navigationQuickActions.add(autoZoomMap);
		}
		if (!startStopNavigation.hasInstanceInList(active)) {
			navigationQuickActions.add(startStopNavigation);
		}
		if (!resumePauseNavigation.hasInstanceInList(active)) {
			navigationQuickActions.add(resumePauseNavigation);
		}

		if (navigationQuickActions.size() > 0) {
			quickActions.add(new QuickAction(0, R.string.quick_action_add_navigation));
			quickActions.addAll(navigationQuickActions);
		}

		return quickActions;
	}

	public static QuickAction newActionByType(int type) {

		switch (type) {

			case NewAction.TYPE:
				return new NewAction();

			case MarkerAction.TYPE:
				return new MarkerAction();

			case FavoriteAction.TYPE:
				return new FavoriteAction();

			case ShowHideFavoritesAction.TYPE:
				return new ShowHideFavoritesAction();

			case ShowHidePoiAction.TYPE:
				return new ShowHidePoiAction();

			case GPXAction.TYPE:
				return new GPXAction();

			case NavVoiceAction.TYPE:
				return new NavVoiceAction();

			case ShowHideOSMBugAction.TYPE:
				return new ShowHideOSMBugAction();

			case AddOSMBugAction.TYPE:
				return new AddOSMBugAction();

			case AddPOIAction.TYPE:
				return new AddPOIAction();

			case MapStyleAction.TYPE:
				return new MapStyleAction();

			case NavAddDestinationAction.TYPE:
				return new NavAddDestinationAction();

			case NavAddFirstIntermediateAction.TYPE:
				return new NavAddFirstIntermediateAction();

			case NavReplaceDestinationAction.TYPE:
				return new NavReplaceDestinationAction();

			case NavAutoZoomMapAction.TYPE:
				return new NavAutoZoomMapAction();

			case NavStartStopAction.TYPE:
				return new NavStartStopAction();

			case NavResumePauseAction.TYPE:
				return new NavResumePauseAction();

			default:
				return new QuickAction();
		}
	}

	public static QuickAction produceAction(QuickAction quickAction) {

		switch (quickAction.type) {

			case NewAction.TYPE:
				return new NewAction(quickAction);

			case MarkerAction.TYPE:
				return new MarkerAction(quickAction);

			case FavoriteAction.TYPE:
				return new FavoriteAction(quickAction);

			case ShowHideFavoritesAction.TYPE:
				return new ShowHideFavoritesAction(quickAction);

			case ShowHidePoiAction.TYPE:
				return new ShowHidePoiAction(quickAction);

			case GPXAction.TYPE:
				return new GPXAction(quickAction);

			case NavVoiceAction.TYPE:
				return new NavVoiceAction(quickAction);

			case ShowHideOSMBugAction.TYPE:
				return new ShowHideOSMBugAction(quickAction);

			case AddOSMBugAction.TYPE:
				return new AddOSMBugAction(quickAction);

			case AddPOIAction.TYPE:
				return new AddPOIAction(quickAction);

			case MapStyleAction.TYPE:
				return new MapStyleAction(quickAction);

			case NavAddDestinationAction.TYPE:
				return new NavAddDestinationAction(quickAction);

			case NavAddFirstIntermediateAction.TYPE:
				return new NavAddFirstIntermediateAction(quickAction);

			case NavReplaceDestinationAction.TYPE:
				return new NavReplaceDestinationAction(quickAction);

			case NavAutoZoomMapAction.TYPE:
				return new NavAutoZoomMapAction(quickAction);

			case NavStartStopAction.TYPE:
				return new NavStartStopAction(quickAction);

			case NavResumePauseAction.TYPE:
				return new NavResumePauseAction(quickAction);

			default:
				return quickAction;
		}
	}

	public static @DrawableRes int getActionIcon(int type) {

		switch (type) {

			case NewAction.TYPE:
				return R.drawable.ic_action_plus;

			case MarkerAction.TYPE:
				return R.drawable.ic_action_flag_dark;

			case FavoriteAction.TYPE:
				return R.drawable.ic_action_fav_dark;

			case ShowHideFavoritesAction.TYPE:
				return R.drawable.ic_action_fav_dark;

			case ShowHidePoiAction.TYPE:
				return R.drawable.ic_action_gabout_dark;

			case GPXAction.TYPE:
				return R.drawable.ic_action_flag_dark;

			case NavVoiceAction.TYPE:
				return R.drawable.ic_action_volume_up;

			case ShowHideOSMBugAction.TYPE:
				return R.drawable.ic_action_bug_dark;

			case AddOSMBugAction.TYPE:
				return R.drawable.ic_action_bug_dark;

			case AddPOIAction.TYPE:
				return R.drawable.ic_action_gabout_dark;

			case MapStyleAction.TYPE:
				return R.drawable.ic_map;

			case NavAddDestinationAction.TYPE:
				return R.drawable.ic_action_point_add_destination;

			case NavAddFirstIntermediateAction.TYPE:
				return R.drawable.ic_action_intermediate;

			case NavReplaceDestinationAction.TYPE:
				return R.drawable.ic_action_point_add_destination;

			case NavAutoZoomMapAction.TYPE:
				return R.drawable.ic_action_search_dark;

			case NavStartStopAction.TYPE:
				return R.drawable.ic_action_start_navigation;

			case NavResumePauseAction.TYPE:
				return R.drawable.ic_play_dark;

			default:
				return R.drawable.ic_action_plus;
		}
	}

	public static @StringRes int getActionName(int type) {

		switch (type) {

			case NewAction.TYPE:
				return R.string.quick_action_new_action;

			case MarkerAction.TYPE:
				return R.string.quick_action_add_marker;

			case FavoriteAction.TYPE:
				return R.string.quick_action_add_favorite;

			case ShowHideFavoritesAction.TYPE:
				return R.string.quick_action_showhide_favorites_title;

			case ShowHidePoiAction.TYPE:
				return R.string.quick_action_showhide_poi_title;

			case GPXAction.TYPE:
				return R.string.quick_action_add_gpx;

			case NavVoiceAction.TYPE:
				return R.string.quick_action_navigation_voice;

			case ShowHideOSMBugAction.TYPE:
				return R.string.quick_action_showhide_osmbugs_title;

			case AddOSMBugAction.TYPE:
				return R.string.quick_action_add_osm_bug;

			case AddPOIAction.TYPE:
				return R.string.quick_action_add_poi;

			case MapStyleAction.TYPE:
				return R.string.quick_action_map_style;

			case NavAddDestinationAction.TYPE:
				return R.string.quick_action_add_destination;

			case NavAddFirstIntermediateAction.TYPE:
				return R.string.quick_action_add_first_intermediate;

			case NavReplaceDestinationAction.TYPE:
				return R.string.quick_action_replace_destination;

			case NavAutoZoomMapAction.TYPE:
				return R.string.quick_action_auto_zoom;

			case NavStartStopAction.TYPE:
				return R.string.quick_action_start_stop_navigation;

			case NavResumePauseAction.TYPE:
				return R.string.quick_action_resume_pause_navigation;

			default:
				return R.string.quick_action_new_action;
		}
	}

	public static boolean isActionEditable(int type) {

		switch (type) {

			case NewAction.TYPE:
			case MarkerAction.TYPE:
			case ShowHideFavoritesAction.TYPE:
			case ShowHidePoiAction.TYPE:
			case NavVoiceAction.TYPE:
			case NavAddDestinationAction.TYPE:
			case NavAddFirstIntermediateAction.TYPE:
			case NavReplaceDestinationAction.TYPE:
			case NavAutoZoomMapAction.TYPE:
			case ShowHideOSMBugAction.TYPE:
			case NavStartStopAction.TYPE:
			case NavResumePauseAction.TYPE:
				return false;

			default:
				return true;
		}
	}
}
