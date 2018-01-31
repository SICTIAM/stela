package fr.sictiam.stela.pesservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.sictiam.stela.pesservice.model.Notification;

@RestController
@RequestMapping("/api/pes/notifications")
public class NotificationRestController {

    @GetMapping("/all")
    public List<Notification> getAllNotifications() {
        return Notification.notifications;
    }
}
