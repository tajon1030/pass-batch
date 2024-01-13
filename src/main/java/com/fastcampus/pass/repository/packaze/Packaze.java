package com.fastcampus.pass.repository.packaze;

import com.fastcampus.pass.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@Table(name = "package")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.NONE)
public class Packaze extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer packageSeq;

    private String packageName;
    private Integer count;
    private Integer period;
}
