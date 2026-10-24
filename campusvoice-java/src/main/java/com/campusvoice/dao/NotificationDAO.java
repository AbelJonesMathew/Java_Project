package com.campusvoice.dao;

import com.campusvoice.model.Notification;
import java.util.List;

public interface NotificationDAO {
    boolean createNotification(Notification notification);
    List<Notification> findByUserId(String userId);
    boolean markAsRead(String notificationId);
    boolean markAllAsRead(String userId);
}
