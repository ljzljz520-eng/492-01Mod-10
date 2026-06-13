package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.InjuryMaterial;
import com.scaffolding.entity.InjuryReport;
import com.scaffolding.entity.Work;
import com.scaffolding.mapper.InjuryMaterialMapper;
import com.scaffolding.mapper.InjuryReportMapper;
import com.scaffolding.mapper.WorkMapper;
import com.scaffolding.service.InjuryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InjuryReportServiceImpl extends ServiceImpl<InjuryReportMapper, InjuryReport> implements InjuryReportService {

    @Autowired
    private InjuryMaterialMapper injuryMaterialMapper;

    @Autowired
    private WorkMapper workMapper;

    private static final Map<String, String> REQUIRED_MATERIALS = new LinkedHashMap<>();
    static {
        REQUIRED_MATERIALS.put("scene_photo", "现场照片");
        REQUIRED_MATERIALS.put("id_card", "受伤人员身份证");
        REQUIRED_MATERIALS.put("medical_record", "医疗记录/诊断证明");
    }

    private static final Map<String, String> RECOMMENDED_MATERIALS = new LinkedHashMap<>();
    static {
        RECOMMENDED_MATERIALS.put("witness_statement", "见证人证言");
        RECOMMENDED_MATERIALS.put("hospital_receipt", "医院收费单据");
        RECOMMENDED_MATERIALS.put("insurance_policy", "保险单");
    }

    @Override
    public Page<InjuryReport> pageQuery(Long current, Long size, Long projectId, String reportStatus,
                                         Long userId, String userRole, Long enterpriseId, Long laborCompanyId) {
        Page<InjuryReport> page = new Page<>(current, size);
        LambdaQueryWrapper<InjuryReport> wrapper = new LambdaQueryWrapper<>();

        if (projectId != null) {
            wrapper.eq(InjuryReport::getProjectId, projectId);
        }
        if (StringUtils.hasText(reportStatus)) {
            wrapper.eq(InjuryReport::getReportStatus, reportStatus);
        }

        if ("enterprise".equals(userRole) && enterpriseId != null) {
            wrapper.inSql(InjuryReport::getProjectId,
                    "SELECT id FROM project WHERE enterprise_id = " + enterpriseId);
        }
        if ("labor".equals(userRole) && laborCompanyId != null) {
            wrapper.inSql(InjuryReport::getProjectId,
                    "SELECT id FROM project WHERE labor_company_id = " + laborCompanyId);
        }

        wrapper.orderByDesc(InjuryReport::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public InjuryReport getDetail(Long id) {
        return this.getById(id);
    }

    @Override
    public Map<String, Object> checkMissingMaterials(Long injuryReportId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> missingRequired = new ArrayList<>();
        List<Map<String, String>> missingRecommended = new ArrayList<>();

        LambdaQueryWrapper<InjuryMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InjuryMaterial::getInjuryReportId, injuryReportId);
        List<InjuryMaterial> existing = injuryMaterialMapper.selectList(wrapper);
        Set<String> existingTypes = new HashSet<>();
        for (InjuryMaterial m : existing) {
            existingTypes.add(m.getMaterialType());
        }

        for (Map.Entry<String, String> entry : REQUIRED_MATERIALS.entrySet()) {
            if (!existingTypes.contains(entry.getKey())) {
                Map<String, String> item = new HashMap<>();
                item.put("type", entry.getKey());
                item.put("name", entry.getValue());
                item.put("level", "required");
                missingRequired.add(item);
            }
        }

        for (Map.Entry<String, String> entry : RECOMMENDED_MATERIALS.entrySet()) {
            if (!existingTypes.contains(entry.getKey())) {
                Map<String, String> item = new HashMap<>();
                item.put("type", entry.getKey());
                item.put("name", entry.getValue());
                item.put("level", "recommended");
                missingRecommended.add(item);
            }
        }

        result.put("missingRequired", missingRequired);
        result.put("missingRecommended", missingRecommended);
        result.put("missingRequiredCount", missingRequired.size());
        result.put("missingRecommendedCount", missingRecommended.size());
        result.put("canSubmit", missingRequired.isEmpty());
        result.put("allMaterials", new ArrayList<Map<String, String>>() {{
            addAll(missingRequired);
            addAll(missingRecommended);
        }});

        return result;
    }

    @Override
    @Transactional
    public InjuryReport submitReport(InjuryReport report, Long userId, String userName) {
        if (report.getId() == null) {
            String reportNo = "IR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%04d", new Random().nextInt(10000));
            report.setReportNo(reportNo);
            report.setReportUserId(userId);
            report.setReportUserName(userName);
            report.setReportTime(LocalDateTime.now());
            report.setReportStatus("pending");
            if (report.getInjuryStatus() == null) {
                report.setInjuryStatus("normal");
            }
            this.save(report);
        } else {
            report.setUpdateTime(LocalDateTime.now());
            this.updateById(report);
        }
        return report;
    }

    @Override
    @Transactional
    public InjuryReport updateInsuranceInfo(InjuryReport report, Long userId, String userName) {
        InjuryReport existing = this.getById(report.getId());
        if (existing != null) {
            existing.setInsuranceCompany(report.getInsuranceCompany());
            existing.setInsurancePolicyNo(report.getInsurancePolicyNo());
            existing.setInsuranceCoverage(report.getInsuranceCoverage());
            existing.setInsuranceRemark(report.getInsuranceRemark());
            existing.setLaborUserId(userId);
            existing.setLaborUserName(userName);
            existing.setLaborProcessTime(LocalDateTime.now());
            if ("pending".equals(existing.getReportStatus())) {
                existing.setReportStatus("submitted");
            }
            existing.setUpdateTime(LocalDateTime.now());
            this.updateById(existing);
            return existing;
        }
        return null;
    }

    @Override
    @Transactional
    public InjuryReport processConclusion(Long id, String result, String resultRemark, Long userId, String userName) {
        InjuryReport report = this.getById(id);
        if (report != null) {
            report.setInjuryResult(result);
            report.setResultTime(LocalDateTime.now());
            report.setResultRemark(resultRemark);
            report.setReportStatus("approved");
            report.setUpdateTime(LocalDateTime.now());
            this.updateById(report);

            if (report.getWorkId() != null) {
                Work work = workMapper.selectById(report.getWorkId());
                if (work != null) {
                    if ("recovered".equals(result)) {
                        work.setInjuryStatus("recovered");
                    } else if ("suspended".equals(result)) {
                        work.setInjuryStatus("injury");
                    }
                    work.setInjuryReportId(report.getId());
                    work.setUpdateTime(LocalDateTime.now());
                    workMapper.updateById(work);
                }
            }
            return report;
        }
        return null;
    }

    @Override
    public List<InjuryMaterial> getMaterials(Long injuryReportId) {
        LambdaQueryWrapper<InjuryMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InjuryMaterial::getInjuryReportId, injuryReportId);
        wrapper.orderByDesc(InjuryMaterial::getCreateTime);
        return injuryMaterialMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public InjuryMaterial addMaterial(InjuryMaterial material, Long userId, String userName) {
        material.setUploadUserId(userId);
        material.setUploadUserName(userName);
        material.setUploadTime(LocalDateTime.now());
        injuryMaterialMapper.insert(material);

        InjuryReport report = this.getById(material.getInjuryReportId());
        if (report != null && "pending".equals(report.getReportStatus())) {
            Map<String, Object> check = this.checkMissingMaterials(material.getInjuryReportId());
            if ((Boolean) check.get("canSubmit")) {
                report.setReportStatus("submitted");
                report.setUpdateTime(LocalDateTime.now());
                this.updateById(report);
            }
        }
        return material;
    }

    @Override
    @Transactional
    public boolean removeMaterial(Long id) {
        InjuryMaterial material = injuryMaterialMapper.selectById(id);
        if (material != null) {
            injuryMaterialMapper.deleteById(id);
            return true;
        }
        return false;
    }
}
