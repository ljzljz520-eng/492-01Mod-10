package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("injury_material")
public class InjuryMaterial extends BaseEntity {

    private Long injuryReportId;

    private String materialType;

    private String materialName;

    private Long fileInfoId;

    private String fileName;

    private String filePath;

    private Long uploadUserId;

    private String uploadUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime uploadTime;

    private String remark;
}
