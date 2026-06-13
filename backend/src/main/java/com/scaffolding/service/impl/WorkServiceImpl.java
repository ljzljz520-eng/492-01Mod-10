package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Work;
import com.scaffolding.mapper.WorkMapper;
import com.scaffolding.service.WorkService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkService {

    @Override
    public Page<Work> pageQuery(Long current, Long size, String workName, String workStatus, String priority,
                                 Long projectId, String injuryStatus, Long userId, String userRole,
                                 Long enterpriseId, Long laborCompanyId) {
        Page<Work> page = new Page<>(current, size);
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(workName)) {
            wrapper.like(Work::getWorkName, workName);
        }
        if (StringUtils.hasText(workStatus)) {
            wrapper.eq(Work::getWorkStatus, workStatus);
        }
        if (StringUtils.hasText(priority)) {
            wrapper.eq(Work::getPriority, priority);
        }
        if (projectId != null) {
            wrapper.eq(Work::getProjectId, projectId);
        }
        if (StringUtils.hasText(injuryStatus)) {
            wrapper.eq(Work::getInjuryStatus, injuryStatus);
        }

        if ("enterprise".equals(userRole) && enterpriseId != null) {
            wrapper.inSql(Work::getProjectId,
                    "SELECT id FROM project WHERE enterprise_id = " + enterpriseId);
        }
        if ("labor".equals(userRole) && laborCompanyId != null) {
            wrapper.inSql(Work::getProjectId,
                    "SELECT id FROM project WHERE labor_company_id = " + laborCompanyId);
        }

        wrapper.orderByDesc(Work::getCreateTime);
        return this.page(page, wrapper);
    }
}
