package it.petrinet.service;

import it.petrinet.model.Notification;
import it.petrinet.model.database.NotificationsDAO;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class NotificationService {
    private static NotificationService instance;
    private TreeSet<Notification> notifications = new TreeSet<>();
    private final static boolean bugfixMode = false; // Todo: Delete bugfix mode

    private NotificationService() { }
    public static synchronized NotificationService getInstance() {
        if (instance == null) instance = new NotificationService();
        return instance;
    }

    public void loadForCurrentUser() {
        var user = SessionContext.getInstance().getUser();
        if (user != null) {

            //Todo: Delete bugfix mode
            notifications = (bugfixMode) ?
                    NotificationsDAO.getNotificationsByReceiver(user):
                    NotificationsDAO.extractNotificationsByReceiver(user);
            //Todo: Delete bugfix mode
        } else {
            notifications = new TreeSet<>();
        }
    }

    public TreeSet<Notification> getNotifications() {
        return notifications;
    }

    public void removeNotification(Notification notification) {
        if (notification != null) notifications.remove(notification);
    }

    public void clear() {
        notifications = new TreeSet<>();
    }
}