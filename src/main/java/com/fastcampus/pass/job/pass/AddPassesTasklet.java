package com.fastcampus.pass.job.pass;

import com.fastcampus.pass.repository.pass.*;
import com.fastcampus.pass.repository.user.UserGroupMapping;
import com.fastcampus.pass.repository.user.UserGroupMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddPassesTasklet implements Tasklet {

    private final PassRepository passRepository;

    private final BulkPassRepository bulkPassRepository;

    private final UserGroupMappingRepository userGroupMappingRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // 이용권 시작 일시 1일전 userGroupMapping 사용자에게 이용권을 지급해줍니다.
        final LocalDateTime startedAt = LocalDateTime.now().minusDays(1);
        final List<BulkPass> bulkPasses = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt);

        int count = 0;
        // 대량 이용권 정보를 돌면서 userGroup에 속한 userId를 조회하고 해당 userId로 이용권을 추가해준다.
        for (BulkPass bulkPass : bulkPasses) {
            final List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPass.getUserGroupId())
                    .stream().map(UserGroupMapping::getUserId)
                    .toList();

            count += addPasses(bulkPass, userIds);

            bulkPass.complete();
        }

        return RepeatStatus.FINISHED;
    }

    private int addPasses(BulkPass bulkPass, List<String> userIds) {
        List<Pass> passes = new ArrayList<>();
        for (String userId : userIds) {
            Pass pass = PassModelMapper.INSTANCE.toPass(bulkPass, userId);
            passes.add(pass);
        }
        return passRepository.saveAll(passes).size();
    }
}
