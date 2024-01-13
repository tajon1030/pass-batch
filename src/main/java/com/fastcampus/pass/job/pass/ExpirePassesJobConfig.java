package com.fastcampus.pass.job.pass;


import com.fastcampus.pass.repository.pass.Pass;
import com.fastcampus.pass.repository.pass.PassStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ExpirePassesJobConfig {

    private final int CHUNK_SIZE = 5;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job expirePassesJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("expirePassesJob", jobRepository)
                .start(expirePassesStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step expirePassesStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("expirePassesStep", jobRepository)
                .<Pass, Pass>chunk(CHUNK_SIZE, transactionManager)
                .reader(expirePassesItemReader())
                .processor(expirePassesItemProcessor())
                .writer(expirePassesItemWriter())
                .build();
    }


    @Bean
    @StepScope //Bean의 생성 시점이 스프링 애플리케이션이 실행되는 시점이 아닌 @JobScope, @StepScope가 명시된 메서드가 실행될 때까지 지연시키는 것을 의미
    public JpaCursorItemReader<Pass> expirePassesItemReader() {
        // 데이터를 읽어올때 status가 progress인것들만 읽어와서 처리하면 expired로 변경이 되는데
        // 페이징의 경우 누락이 될수있으므로 커서기반으로 구현
        return new JpaCursorItemReaderBuilder<Pass>()
                .name("expirePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select p from Pass p where p.status = :status and p.endedAt <= :endedAt")
                .parameterValues(Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now()))
                .build();

    }

    @Bean
    public ItemProcessor<Pass, Pass> expirePassesItemProcessor() {
        return pass -> {
            pass.expired();
            return pass;
        };
    }

    @Bean
    public JpaItemWriter<Pass> expirePassesItemWriter() {
        return new JpaItemWriterBuilder<Pass>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

}
