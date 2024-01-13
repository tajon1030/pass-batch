package com.fastcampus.pass.job.pass;

import com.fastcampus.pass.config.TestBatchConfig;
import com.fastcampus.pass.repository.packaze.Packaze;
import com.fastcampus.pass.repository.pass.Pass;
import com.fastcampus.pass.repository.pass.PassRepository;
import com.fastcampus.pass.repository.pass.PassStatus;
import com.fastcampus.pass.repository.user.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {ExpirePassesJobConfig.class, TestBatchConfig.class})
class ExpirePassesJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PassRepository passRepository;


    private void addPassEntities(int size) {
        final LocalDateTime now = LocalDateTime.now();
        final Random random = new Random();
        final Packaze packaze = Packaze.builder()
                .packageSeq(1)
                .build();

        List<Pass> passEntities = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            final User user = User.builder()
                    .userId("A" + 1000000 + i)
                    .build();
            Pass pass = Pass.builder()
                    .packaze(packaze)
                    .status(PassStatus.PROGRESSED)
                    .user(user)
                    .remainingCount(random.nextInt(11))
                    .startedAt(now.minusDays(60))
                    .endedAt(now.minusDays(1))
                    .build();
            passEntities.add(pass);

        }
        passRepository.saveAll(passEntities);
    }

    @Test
    public void test_expirePassesStep() throws Exception {
        // given
        addPassEntities(10);

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance jobInstance = jobExecution.getJobInstance();

        // then
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals("expirePassesJob", jobInstance.getJobName());

    }


}