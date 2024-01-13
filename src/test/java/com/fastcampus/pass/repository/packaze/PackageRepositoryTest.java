package com.fastcampus.pass.repository.packaze;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class PackageRepositoryTest {

    @Autowired
    private PackageRepository packageRepository;

    @Test
    public void test_save() {
        // given
        Packaze packaze = new Packaze();
        packaze.setPackageName("바디챌린지 12주");
        packaze.setPeriod(30);

        // when
        packageRepository.save(packaze);

        // then
        assertNotNull(packaze.getPackageSeq());
    }

    @Test
    public void test_findByCreatedAtAfter() {
        // given
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(1);

        Packaze packaze0 = new Packaze();
        packaze0.setPackageName("학생전용 3개월");
        packaze0.setPeriod(90);
        packageRepository.save(packaze0);

        Packaze packaze1 = new Packaze();
        packaze1.setPackageName("학생전용 6개월");
        packaze1.setPeriod(180);
        packageRepository.save(packaze1);

        // when
        final List<Packaze> packazes = packageRepository.findByCreatedAtAfter(localDateTime, PageRequest.of(0, 1, Sort.by("packageSeq").descending()));

        // then
        assertEquals(1, packazes.size());
        assertEquals(packaze1.getPackageSeq(), packazes.get(0).getPackageSeq());
    }

    @Test
    public void test_updateCountAndPeriod() {
        Packaze packaze = new Packaze();
        packaze.setPackageName("바프 이벤트 6개월");
        packaze.setPeriod(180);
        packageRepository.save(packaze);

        // when
        int updatedCount = packageRepository.updateCountAndPeriod(packaze.getPackageSeq(), 30, 120);
        final Packaze updatedPackaze = packageRepository.findById(packaze.getPackageSeq()).get();

        // then
        assertEquals(1, updatedCount);
        assertEquals(30, updatedPackaze.getCount());
        assertEquals(120, updatedPackaze.getPeriod());

    }

    @Test
    public void test_delete(){
        Packaze packaze = new Packaze();
        packaze.setPackageName("제거할 이용권");
        packaze.setCount(1);
        Packaze savedPackaze = packageRepository.save(packaze);

        // when
        packageRepository.deleteById(savedPackaze.getPackageSeq());

        // then
        assertTrue(packageRepository.findById(savedPackaze.getPackageSeq()).isEmpty());
    }

}