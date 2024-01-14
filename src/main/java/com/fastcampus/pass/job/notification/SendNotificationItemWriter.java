package com.fastcampus.pass.job.notification;

import com.fastcampus.pass.adapter.KakaoTalkMessageAdapter;
import com.fastcampus.pass.repository.notification.Notification;
import com.fastcampus.pass.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SendNotificationItemWriter implements ItemWriter<Notification> {
    private final NotificationRepository notificationRepository;
    private final KakaoTalkMessageAdapter kakaoTalkMessageAdapter;

    @Override
    public void write(List<? extends Notification> notificationEntities) throws Exception {
        int count = 0;

        for (Notification notification : notificationEntities) {
            boolean successful = kakaoTalkMessageAdapter.sendKakaoTalkMessage(notification.getUuid(), notification.getText());

            if (successful) {
                notification.send();
                notificationRepository.save(notification);
                count++;
            }

        }
        log.info("SendNotificationItemWriter - write: 수업 전 알람 {}/{}건 전송 성공", count, notificationEntities.size());

    }
}
