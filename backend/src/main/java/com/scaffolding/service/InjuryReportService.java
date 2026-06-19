package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.InjuryReport;
import com.scaffolding.entity.InjuryMaterial;

import java.util.List;
import java.util.Map;

public interface InjuryReportService extends IService<InjuryReport> {

    Page<InjuryReport> pageQuery(Long current, Long size, Long projectId, String reportStatus, Long userId, String userRole, Long enterpriseId, Long laborCompanyId);

    InjuryReport getDetail(Long id);

    Map<String, Object> checkMissingMaterials(Long injuryReportId);

    InjuryReport submitReport(InjuryReport report, Long userId, String userName);

    InjuryReport updateInsuranceInfo(InjuryReport report, Long userId, String userName);

    InjuryReport processConclusion(Long id, String result, String resultRemark, Long userId, String userName);

    List<InjuryMaterial> getMaterials(Long injuryReportId);

    InjuryMaterial addMaterial(InjuryMaterial material, Long userId, String userName);

    boolean removeMaterial(Long id);

    boolean canUserAccessProject(Long projectId, Long enterpriseId, Long laborCompanyId);
}
