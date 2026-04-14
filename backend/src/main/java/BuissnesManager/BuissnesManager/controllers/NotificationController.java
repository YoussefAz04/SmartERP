package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.Notification;
import BuissnesManager.BuissnesManager.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/unread/{entrepriseId}")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long entrepriseId) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(entrepriseId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{entrepriseId}")
    public ResponseEntity<List<Notification>> getAllNotifications(@PathVariable Long entrepriseId) {
        try {
            List<Notification> notifications = notificationService.getAllNotifications(entrepriseId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/count/{entrepriseId}")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long entrepriseId) {
        try {
            long count = notificationService.getUnreadCount(entrepriseId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/mark-viewed")
    public ResponseEntity<Notification> markAsViewed(@PathVariable Long id) {
        try {
            Notification notification = notificationService.markAsViewed(id);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/dismiss")
    public ResponseEntity<Notification> dismissNotification(@PathVariable Long id) {
        try {
            Notification notification = notificationService.dismissNotification(id);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/markAll/{entrepriseId}")
    public ResponseEntity<Void> markAllAsViewed(@PathVariable Long entrepriseId) {
        try {
            notificationService.markAllAsViewed(entrepriseId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
