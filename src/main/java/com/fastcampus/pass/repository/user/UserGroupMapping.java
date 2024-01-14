package com.fastcampus.pass.repository.user;

import com.fastcampus.pass.repository.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Getter
@IdClass(UserGroupMappingId.class)
public class UserGroupMapping extends BaseEntity {

    @Id
    private String userGroupId;

    @Id
    private String userId;

    private String userGroupName;

    private String description;

}
