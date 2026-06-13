package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("injury_report")
public class InjuryReport extends BaseEntity {

    private String reportNo;

    private Long projectId;

    private String projectName;

    private Long workId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime injuryTime;

    private String injuryLocation;

    private String witnesses;

    private String hospital;

    private String workShift;

    private String workPosition;

    private String injuredPersonName;

    private String injuredPersonIdcard;

    private String injuredPersonPhone;

    private String injuryDescription;

    private String reportStatus;

    private String injuryResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime resultTime;

    private String resultRemark;

    private Long reportUserId;

    private String reportUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reportTime;

    private Long laborUserId;

    private String laborUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime laborProcessTime;

    private String insuranceCompany;

    private String insurancePolicyNo;

    private BigDecimal insuranceCoverage;

    private String insuranceRemark;

    private String remark;
}
