package com.fastcampus.pass.repository.notification;

import com.fastcampus.pass.repository.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationSeq;

    private String uuid;

    @Enumerated(EnumType.STRING)
    private NotificationEvent event;

    private String text;

    private boolean sent;

    private LocalDateTime sentAt;

    public void send() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }
}
