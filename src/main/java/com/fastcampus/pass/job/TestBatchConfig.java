package com.fastcampus.pass.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TestBatchConfig {

    @Bean
    public Job passJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("passJob", jobRepository)
                .start(myStep(transactionManager, jobRepository))
                .build();
    }

    @Bean
    public Step myStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("passStep", jobRepository)
                .tasklet(myTasklet(), transactionManager) // or .chunk(chunkSize, transactionManager)
                .build();
    }

    public Tasklet myTasklet() {
        return (contribution, chunkContext) -> {
            System.out.println("Execute PassStep");
            return RepeatStatus.FINISHED;
        };
    }

}
