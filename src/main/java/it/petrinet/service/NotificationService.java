package it.petrinet.service;

import it.petrinet.model.Notification;
import it.petrinet.model.database.NotificationsDAO;
import java.util.Collections;
import java.util.List;

public class NotificationService {
    private static NotificationService instance;
    private List<Notification> notifications = Collections.emptyList();

    private NotificationService() { }
    public static synchronized NotificationService getInstance() {
        if (instance == null) instance = new NotificationService();
        return instance;
    }

    public void loadForCurrentUser() {
        var user = SessionContext.getInstance().getUser();
        if (user != null) {
            notifications = NotificationsDAO.getNotificationsByReceiver(user);
        } else {
            notifications = Collections.emptyList();
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void removeNotification(Notification notification) {
        if (notification != null) notifications.remove(notification);
    }

    public void clear() {
        notifications = Collections.emptyList();
    }
}