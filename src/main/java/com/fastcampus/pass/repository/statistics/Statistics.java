package com.fastcampus.pass.repository.statistics;

import com.fastcampus.pass.repository.booking.Booking;
import com.fastcampus.pass.repository.booking.BookingStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Statistics {
    @Id
    private Integer statisticsSeq;

    private LocalDateTime statisticsAt;

    private Integer allCount;

    private Integer attendedCount;

    private Integer cancelledCount;

    public static Statistics create(final Booking booking) {
        Statistics statistics = Statistics.builder()
                .statisticsAt(booking.getStatisticsAt())
                .allCount(1)
                .build();
        if (booking.isAttended()) {
            statistics.setAttendedCount(1);

        }
        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            statistics.setCancelledCount(1);

        }
        return statistics;

    }

    public void add(final Booking booking) {
        this.allCount++;

        if (booking.isAttended()) {
            this.attendedCount++;
        }
        if (BookingStatus.CANCELLED.equals(booking.getStatus())) {
            this.cancelledCount++;
        }

    }
}
