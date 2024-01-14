package com.fastcampus.pass.job.pass;

import com.fastcampus.pass.repository.pass.*;
import com.fastcampus.pass.repository.user.UserGroupMapping;
import com.fastcampus.pass.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class) // JUnit5
public class AddPassesTaskletTest {
    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private PassRepository passRepository;

    @Mock
    private BulkPassRepository bulkPassRepository;

    @Mock
    private UserGroupMappingRepository userGroupMappingRepository;

    // @InjectMocks 클래스의 인스턴스를 생성하고 @Mock으로 생성된 객체를 주입합니다.
    @InjectMocks
    private AddPassesTasklet addPassesTasklet;

    @Test
    public void test_execute() throws Exception {
        // given
        final String userGroupId = "GROUP";
        final String userId = "A1000000";
        final Integer packageSeq = 1;
        final Integer count = 10;

        final LocalDateTime now = LocalDateTime.now();

        final BulkPass bulkPass = BulkPass.builder()
                .packageSeq(packageSeq)
                .userGroupId(userGroupId)
                .status(BulkPassStatus.READY)
                .count(count)
                .startedAt(now)
                .endedAt(now.plusDays(60))
                .build();

        final UserGroupMapping userGroupMapping = UserGroupMapping.builder()
                .userGroupId(userGroupId)
                .userId(userId)
                .build();

        // when
        when(bulkPassRepository.findByStatusAndStartedAtGreaterThan(eq(BulkPassStatus.READY), any())).thenReturn(List.of(bulkPass));
        when(userGroupMappingRepository.findByUserGroupId(eq("GROUP"))).thenReturn(List.of(userGroupMapping));

        RepeatStatus repeatStatus = addPassesTasklet.execute(stepContribution, chunkContext);

        // then
        // execute의 return 값인 RepeatStatus 값을 확인합니다.
        assertEquals(RepeatStatus.FINISHED, repeatStatus);

        // 추가된 Pass 값을 확인합니다.
        ArgumentCaptor<List> passEntitiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(passRepository, times(1)).saveAll(passEntitiesCaptor.capture());
        final List<Pass> passEntities = passEntitiesCaptor.getValue();

        assertEquals(1, passEntities.size());
        final Pass pass = passEntities.get(0);
        assertEquals(packageSeq, pass.getPackageSeq());
        assertEquals(userId, pass.getUserId());
        assertEquals(PassStatus.READY, pass.getStatus());
        assertEquals(count, pass.getRemainingCount());

    }

}
