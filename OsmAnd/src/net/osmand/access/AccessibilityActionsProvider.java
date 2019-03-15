package net.osmand.access;

import net.osmand.data.RotatedTileBox;

// This interface is intended for defining prioritized actions
// to be performed in touch exploration mode. Implementations
// should do nothing and return false when accessibility is disabled.
public interface AccessibilityActionsProvider {
    boolean onClick();
    boolean onLongClick(RotatedTileBox tileBox);
}
