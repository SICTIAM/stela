package fr.sictiam.stela.convocationservice.controller;

import fr.sictiam.stela.convocationservice.model.Notification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/convocation/notifications")
public class NotificationRestController {

    @GetMapping("/all")
    public List<Notification> getAllNotifications() {
        return Notification.notifications;
    }
}
