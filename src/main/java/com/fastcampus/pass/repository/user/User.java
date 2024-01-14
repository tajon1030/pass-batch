package com.fastcampus.pass.repository.user;

import com.fastcampus.pass.repository.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Map;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
// json의 타입을 정의합니다.
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class User extends BaseEntity {
    @Id
    private String userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String status;

    private String phone;

    // json 형태로 저장되어 있는 문자열 데이터를 Map으로 매핑합니다.
    @Type(type = "json")
    private Map<String, Object> meta;

    public String getUuid() {
        String uuid = null;
        if (meta.containsKey("uuid")) {
            uuid = String.valueOf(meta.get("uuid"));
        }
        return uuid;

    }

}
