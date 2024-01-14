package com.fastcampus.pass.repository.pass;

import com.fastcampus.pass.repository.packaze.Packaze;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PassModelMapperTest {

    @Test
    public void test_toPassEntity() {
        // given
        final LocalDateTime now = LocalDateTime.now();
        final String userId = "A1000000";

        Packaze packaze = Packaze.builder()
                .packageSeq(1)
                .packageName("test")
                .build();

        BulkPass bulkPass = BulkPass.builder()
                .packaze(packaze)
                .userGroupId("GROUP")
                .status(BulkPassStatus.COMPLETED)
                .count(10)
                .startedAt(now.minusDays(60))
                .endedAt(now)
                .build();

        // when
        final Pass pass = PassModelMapper.INSTANCE.toPass(bulkPass, userId);

        // then
        assertEquals(1, pass.getPackaze().getPackageSeq());
        assertEquals(PassStatus.READY, pass.getStatus());
        assertEquals(10, pass.getRemainingCount());
        assertEquals(now.minusDays(60), pass.getStartedAt());
        assertEquals(now, pass.getEndedAt());
        assertEquals(userId, pass.getUserId());

    }
}