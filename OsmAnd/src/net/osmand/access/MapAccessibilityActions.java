package net.osmand.access;

import net.osmand.data.RotatedTileBox;
import net.osmand.plus.activities.MapActivity;

// Accessibility actions for map view.
public class MapAccessibilityActions implements AccessibilityActionsProvider {

    private final MapActivity activity;

    public MapAccessibilityActions(final MapActivity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onClick() {
        if (activity.getMyApplication().accessibilityEnabled()) {
            activity.getMyApplication().getLocationProvider().emitNavigationHint();
            return true;
        }
        return false;
    }

    @Override
    public boolean onLongClick(RotatedTileBox tileBox) {
        if (activity.getMyApplication().accessibilityEnabled() && activity.getMapViewTrackingUtilities().isMapLinkedToLocation()) {
            activity.getMapLayers().getContextMenuLayer().showContextMenu(tileBox.getLatitude(), tileBox.getLongitude(), true);
            return true;
        }
        return false;
    }
}