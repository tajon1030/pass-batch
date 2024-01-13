package com.fastcampus.pass.repository.packaze;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface PackageRepository extends JpaRepository<Packaze, Integer> {

    List<Packaze> findByCreatedAtAfter(LocalDateTime localDateTime, Pageable packageSeq);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE Packaze p
            SET p.count = :count
            , p.period = :period
            WHERE p.packageSeq = :packageSeq
            """)
    int updateCountAndPeriod(Integer packageSeq, Integer count, Integer period);
}
