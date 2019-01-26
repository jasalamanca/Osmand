package net.osmand.plus.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.activities.MapActivity;

public abstract class OsmandNotification {

	final static int NAVIGATION_NOTIFICATION_SERVICE_ID = 5;
	final static int GPX_NOTIFICATION_SERVICE_ID = 6;
	public final static int TOP_NOTIFICATION_SERVICE_ID = 100;

	final static int WEAR_NAVIGATION_NOTIFICATION_SERVICE_ID = 1005;
	final static int WEAR_GPX_NOTIFICATION_SERVICE_ID = 1006;

	final OsmandApplication app;
	boolean ongoing = true;
	int color;
	int icon;
	private boolean top;

	private final String groupName;

	public enum NotificationType {
		NAVIGATION,
		GPX,
		GPS
	}

	OsmandNotification(OsmandApplication app, String groupName) {
		this.app = app;
		this.groupName = groupName;
		init();
	}

	void init() {
	}

	public String getGroupName() {
		return groupName;
	}

	public abstract NotificationType getType();

	public boolean isTop() {
		return top;
	}

	public void setTop(boolean top) {
		this.top = top;
	}

	Notification.Builder createBuilder(boolean wearable) {
		Intent contentIntent = new Intent(app, MapActivity.class);
		PendingIntent contentPendingIntent = PendingIntent.getActivity(app, 0, contentIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(app)
				.setPriority(top ? Notification.PRIORITY_HIGH : getPriority())
				.setOngoing(ongoing && !wearable)
				.setContentIntent(contentPendingIntent)
				.setDeleteIntent(NotificationDismissReceiver.createIntent(app, getType()));
//        builder.setGroup(groupName).setGroupSummary(!wearable);

//		if (color != 0) {
//			builder.setColor(color);
//		}
		if (icon != 0) {
			builder.setSmallIcon(icon);
		}

		return builder;
	}

	public abstract Notification.Builder buildNotification(boolean wearable);

	protected abstract int getOsmandNotificationId();

	protected abstract int getOsmandWearableNotificationId();

	protected abstract int getPriority();

	public abstract boolean isActive();

	protected abstract boolean isEnabled();

	void setupNotification(Notification notification) {
	}

	public void onNotificationDismissed() {
	}

	private void notifyWearable(NotificationManager notificationManager) {
		Notification.Builder wearNotificationBuilder = buildNotification(true);
		if (wearNotificationBuilder != null) {
			Notification wearNotification = wearNotificationBuilder.build();
			notificationManager.notify(getOsmandWearableNotificationId(), wearNotification);
		}
	}

	public boolean showNotification() {
		NotificationManager notificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

		if (isEnabled()) {
			Notification.Builder notificationBuilder = buildNotification(false);
			if (notificationBuilder != null) {
				Notification notification = notificationBuilder.build();
				setupNotification(notification);
				notificationManager.notify(top ? TOP_NOTIFICATION_SERVICE_ID : getOsmandNotificationId(), notification);
				notifyWearable(notificationManager);
				return true;
			}
		}
		return false;
	}

	public boolean refreshNotification() {
		NotificationManager notificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

		if (isEnabled()) {
			Notification.Builder notificationBuilder = buildNotification(false);
			if (notificationBuilder != null) {
				Notification notification = notificationBuilder.build();
				setupNotification(notification);
				if (top) {
					notificationManager.cancel(getOsmandNotificationId());
					notificationManager.notify(TOP_NOTIFICATION_SERVICE_ID, notification);
				} else {
					notificationManager.notify(getOsmandNotificationId(), notification);
				}
				notifyWearable(notificationManager);
				return true;
			} else {
				notificationManager.cancel(getOsmandNotificationId());
			}
		} else {
			notificationManager.cancel(getOsmandNotificationId());
		}
		return false;
	}

	public void removeNotification() {
		NotificationManager notificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.cancel(getOsmandNotificationId());
		notificationManager.cancel(getOsmandWearableNotificationId());
	}

	public void closeSystemDialogs(Context context) {
		Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		context.sendBroadcast(it);
	}
}
