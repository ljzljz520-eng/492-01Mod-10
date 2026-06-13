package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project")
public class Project extends BaseEntity {

    private String projectName;

    private String projectCode;

    private Long enterpriseId;

    private String enterpriseName;

    private Long laborCompanyId;

    private String laborCompanyName;

    private String projectAddress;

    private String projectStatus;

    private String remark;
}
