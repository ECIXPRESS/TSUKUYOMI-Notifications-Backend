package edu.dosw.domain.ports;

import edu.dosw.domain.model.Notification;
import java.util.List;

public interface WebSocketEmitterPort {
    void emitUserNotification(String userId, Notification notification);
    void emitGlobalNotification(Notification notification);
    void emitBatchNotifications(List<Notification> notifications);
    boolean isUserConnected(String userId);
    int getConnectedUsersCount();
}