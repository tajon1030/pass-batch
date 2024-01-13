package com.fastcampus.pass.repository.pass;

import com.fastcampus.pass.repository.BaseEntity;
import com.fastcampus.pass.repository.packaze.Packaze;
import com.fastcampus.pass.repository.user.User;
import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "packageSeq")
    private Packaze packaze;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

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
}
