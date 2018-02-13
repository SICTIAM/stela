package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.Notification;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/acte/notifications")
public class NotificationRestController {

    @GetMapping("/all")
    public List<Notification> getAllNotifications() {
        return Notification.notifications;
    }
}
