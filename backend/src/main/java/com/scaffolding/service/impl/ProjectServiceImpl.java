package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Project;
import com.scaffolding.mapper.ProjectMapper;
import com.scaffolding.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public Page<Project> pageQuery(Long current, Long size, String projectName, String projectStatus,
                                   Long userId, String userRole, Long enterpriseId, Long laborCompanyId) {
        Page<Project> page = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(projectName)) {
            wrapper.like(Project::getProjectName, projectName);
        }
        if (StringUtils.hasText(projectStatus)) {
            wrapper.eq(Project::getProjectStatus, projectStatus);
        }

        if ("enterprise".equals(userRole) && enterpriseId != null) {
            wrapper.eq(Project::getEnterpriseId, enterpriseId);
        }
        if ("labor".equals(userRole) && laborCompanyId != null) {
            wrapper.eq(Project::getLaborCompanyId, laborCompanyId);
        }

        wrapper.orderByDesc(Project::getCreateTime);
        return this.page(page, wrapper);
    }
}
