package com.fastcampus.pass.job.notification;

import com.fastcampus.pass.repository.booking.Booking;
import com.fastcampus.pass.repository.booking.BookingStatus;
import com.fastcampus.pass.repository.notification.Notification;
import com.fastcampus.pass.repository.notification.NotificationEvent;
import com.fastcampus.pass.repository.notification.NotificationModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SendNotificationBeforeClassJobConfig {
    private final int CHUNK_SIZE = 10;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final SendNotificationItemWriter itemWriter;

    @Bean
    public Job sendNotificationBeforeClassJob() {
        return jobBuilderFactory.get("sendNotificationBeforeClassJob")
                .start(addNotificationStep())
                .next(sendNotificationStep())
                .build();
    }

    @Bean
    public Step addNotificationStep() {
        return stepBuilderFactory.get("addNotificationStep")
                .<Booking, Notification>chunk(CHUNK_SIZE) // input - booking, output - notification
                .reader(addNotificationItemReader())
                .processor(addNotificationItemProcessor())
                .writer(addNotificationItemWriter())
                .build();
    }

    /**
     * JpaPagingItemReader: JPA에서 사용하는 페이징 기법입니다.
     * 쿼리 당 pageSize만큼 가져오며 다른 PagingItemReader와 마찬가지로 Thread-safe 합니다.
     */
    @Bean
    public JpaPagingItemReader<Booking> addNotificationItemReader() {
        return new JpaPagingItemReaderBuilder<Booking>()
                .name("addNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                // pageSize: 한 번에 조회할 row 수
                .pageSize(CHUNK_SIZE)
                // 상태(status)가 준비중이며, 시작일시(startedAt)이 10분 후 시작하는 예약이 알람 대상이 됩니다.
                .queryString("select b from Booking b join fetch b.user where b.status = :status and b.startedAt <= :startedAt order by b.bookingSeq")
                .parameterValues(Map.of("status", BookingStatus.READY, "startedAt", LocalDateTime.now().plusMinutes(10)))
                .build();
    }

    @Bean
    public ItemProcessor<Booking, Notification> addNotificationItemProcessor() {
        return booking -> NotificationModelMapper.INSTANCE.toNotification(booking, NotificationEvent.BEFORE_CLASS);
    }

    @Bean
    public JpaItemWriter<Notification> addNotificationItemWriter() {
        return new JpaItemWriterBuilder<Notification>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    /**
     * reader는 synchrosized로 순차적으로 실행되지만 writer는 multi-thread 로 동작합니다.
     */
    @Bean
    public Step sendNotificationStep() {
        return this.stepBuilderFactory.get("sendNotificationStep")
                .<Notification, Notification>chunk(CHUNK_SIZE)
                .reader(sendNotificationItemReader())
                .writer(itemWriter)
                .taskExecutor(new SimpleAsyncTaskExecutor()) // 가장 간단한 멀티쓰레드 TaskExecutor를 선언하였습니다.
                .build();
    }

    /**
     * notification에 있는 sent값을 보고 데이터를 조회해오고 sent를 다시 업데이트 해줘야함
     * paging을 하게 되면 누락이 발생할 수 있다.
     * 따라서 cursor 기법을 사용해야한다.
     *
     * multi-thread 환경에서 reader와 writer는 thread-safe 해야합니다.
     * 그러나 cursor 기법의 ItemReader는 thread-safe하지 않다.
     * Paging 기법을 사용하거나 synchronized 를 선언하여 순차적으로 수행해야합니다.
     *
     * => SynchronizedItemStreamReader로 감싼 cursor기법을 사용
     * -> reader는 순차적으로 실행됨 대신 writer부분을 multi-thread방식으로 진행(어짜피 writer가 코스트가 많이드는부분이므로 유의미한 방식)
     */
    @Bean
    public SynchronizedItemStreamReader<Notification> sendNotificationItemReader() {
        JpaCursorItemReader<Notification> itemReader = new JpaCursorItemReaderBuilder<Notification>()
                .name("sendNotificationItemReader")
                .entityManagerFactory(entityManagerFactory)
                // 이벤트(event)가 수업 전이며, 발송 여부(sent)가 미발송인 알람이 조회 대상이 됩니다.
                .queryString("select n from Notification n where n.event = :event and n.sent = :sent")
                .parameterValues(Map.of("event", NotificationEvent.BEFORE_CLASS, "sent", false))
                .build();

        return new SynchronizedItemStreamReaderBuilder<Notification>()
                .delegate(itemReader)
                .build();

    }
}
