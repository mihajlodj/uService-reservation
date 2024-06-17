package ftn.reservationservice.services;


import ftn.reservationservice.domain.dtos.NotificationRequest;
import ftn.reservationservice.domain.entities.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AmqpTemplate rabbitTemplate;

    public void sendNotification(String userId, NotificationType notificationType){
        NotificationRequest notificationRequest = new NotificationRequest(userId, notificationType);
        rabbitTemplate.convertAndSend("notificationQueue", notificationRequest);
    }

}
