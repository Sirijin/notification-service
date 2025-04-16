package org.example;

import lombok.Data;

import java.util.List;

@Data
public class NotificationListWrapper {
    private List<Notification> notifications;
}
