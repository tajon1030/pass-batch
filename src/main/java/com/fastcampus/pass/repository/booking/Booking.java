package com.fastcampus.pass.repository.booking;

import com.fastcampus.pass.repository.BaseEntity;
import com.fastcampus.pass.repository.pass.Pass;
import com.fastcampus.pass.repository.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookingSeq;
    private Integer passSeq;
    private String userId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private boolean usedPass;
    private boolean attended;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime cancelledAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    // endedAt 기준, yyyy-MM-HH 00:00:00
    public LocalDateTime getStatisticsAt() {
        return this.endedAt.withHour(0).withMinute(0).withSecond(0).withNano(0);

    }

}
