package net.osmand.plus;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import net.osmand.plus.notifications.GpxNotification;
import net.osmand.plus.notifications.NavigationNotification;
import net.osmand.plus.notifications.OsmandNotification;
import net.osmand.plus.notifications.OsmandNotification.NotificationType;

import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {

	private final OsmandApplication app;

	private NavigationNotification navigationNotification;
	private GpxNotification gpxNotification;
	private final List<OsmandNotification> all = new ArrayList<>();

	public NotificationHelper(OsmandApplication app) {
		this.app = app;
		init();
	}

	private void init() {
		navigationNotification = new NavigationNotification(app);
		gpxNotification = new GpxNotification(app);
		all.add(navigationNotification);
		all.add(gpxNotification);
	}

	public Notification buildTopNotification() {
		OsmandNotification notification = acquireTopNotification();
		if (notification != null) {
			removeNotification(notification.getType());
			setTopNotification(notification);
			Notification.Builder notificationBuilder = notification.buildNotification(false);
			return notificationBuilder.build();
		}
		return null;
	}

	private OsmandNotification acquireTopNotification() {
		OsmandNotification notification = null;
		if (navigationNotification.isEnabled()) {
			notification = navigationNotification;
		} else if (gpxNotification.isEnabled() && gpxNotification.isActive()) {
			notification = gpxNotification;
		}
		return notification;
	}

	public void updateTopNotification() {
		OsmandNotification notification = acquireTopNotification();
		setTopNotification(notification);
	}

	private void setTopNotification(OsmandNotification notification) {
		for (OsmandNotification n : all) {
			n.setTop(n == notification);
		}
	}

	public void showNotifications() {
		if (!hasAnyTopNotification()) {
			removeTopNotification();
		}
		for (OsmandNotification notification : all) {
			notification.showNotification();
		}
	}

	public void refreshNotification(NotificationType notificationType) {
		for (OsmandNotification notification : all) {
			if (notification.getType() == notificationType) {
				notification.refreshNotification();
				break;
			}
		}
	}

	public void onNotificationDismissed(NotificationType notificationType) {
		for (OsmandNotification notification : all) {
			if (notification.getType() == notificationType) {
				notification.onNotificationDismissed();
				break;
			}
		}
	}

	private boolean hasAnyTopNotification() {
		for (OsmandNotification notification : all) {
			if (notification.isTop()) {
				return true;
			}
		}
		return false;
	}

	public void refreshNotifications() {
		if (!hasAnyTopNotification()) {
			removeTopNotification();
		}
		for (OsmandNotification notification : all) {
			notification.refreshNotification();
		}
	}

	private void removeTopNotification() {
		NotificationManager notificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(OsmandNotification.TOP_NOTIFICATION_SERVICE_ID);
	}

	private void removeNotification(NotificationType notificationType) {
		for (OsmandNotification notification : all) {
			if (notification.getType() == notificationType) {
				notification.removeNotification();
				break;
			}
		}
	}

	public void removeNotifications() {
		for (OsmandNotification notification : all) {
			notification.removeNotification();
		}
	}
}
