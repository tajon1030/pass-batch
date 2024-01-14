package com.fastcampus.pass.repository.pass;

import com.fastcampus.pass.repository.BaseEntity;
import com.fastcampus.pass.repository.packaze.Packaze;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BulkPass extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bulkPassSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packageSeq", insertable = false, updatable = false)
    private Packaze packaze;

    private Integer packageSeq;

    private String userGroupId;

    @Enumerated(EnumType.STRING)
    private BulkPassStatus status;

    private Integer count;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public void complete() {
        this.status = BulkPassStatus.COMPLETED;
    }
}
