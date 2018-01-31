package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.Notification;
import fr.sictiam.stela.acteservice.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/acte/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;

    public NotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/all")
    public List<Notification> getAllNotifications() {
        return Notification.notifications;
    }
}
