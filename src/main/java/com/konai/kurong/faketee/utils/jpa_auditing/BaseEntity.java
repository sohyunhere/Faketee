package com.konai.kurong.faketee.utils.jpa_auditing;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass // JPA Entity 클래스들이 이 클래스를 상속할 경우 CRE_DTTM, UPD_DTTM, CRE_ID, UPD_ID를 컬럼으로 인식
@EntityListeners(AuditingEntityListener.class) // 해당 클래스에 Auditing 기능 포함
public class BaseEntity {

    @CreatedDate // Entity가 생성되어 저장될 때 시간이 자동 저장
    private LocalDateTime CRE_DTTM;

    @LastModifiedDate // Entity의 값을 변경할 때 시간이 자동 저장
    private LocalDateTime UPD_DTTM;

    @CreatedBy
    private Long CRE_ID;

    @LastModifiedBy
    private Long UPD_ID;
}
