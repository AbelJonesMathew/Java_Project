package com.campusvoice.service;

import com.campusvoice.dao.NotificationDAO;
import com.campusvoice.dao.impl.NotificationDAOImpl;
import com.campusvoice.model.Notification;
import java.util.List;

public class NotificationService {
    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAOImpl();
    }

    public boolean sendNotification(String userId, String title, String message, String type) {
        Notification n = new Notification(userId, title, message, type);
        return notificationDAO.createNotification(n);
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationDAO.findByUserId(userId);
    }

    public boolean markAsRead(String notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    public boolean markAllAsRead(String userId) {
        return notificationDAO.markAllAsRead(userId);
    }
}
