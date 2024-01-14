package com.fastcampus.pass.repository.pass;

import com.fastcampus.pass.repository.BaseEntity;
import com.fastcampus.pass.repository.packaze.Packaze;
import com.fastcampus.pass.repository.user.User;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class Pass extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer passSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packageSeq", insertable = false, updatable = false)
    private Packaze packaze;

    private Integer packageSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    private String userId;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    @Enumerated(EnumType.STRING)
    private PassStatus status;

    private Integer remainingCount;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private LocalDateTime expiredAt;

    public void expired() {
        this.status = PassStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();
    }

    public void usePass() {
        this.remainingCount--;
    }
}
