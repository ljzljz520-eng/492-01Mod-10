package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Project;

public interface ProjectService extends IService<Project> {

    Page<Project> pageQuery(Long current, Long size, String projectName, String projectStatus, Long userId, String userRole, Long enterpriseId, Long laborCompanyId);
}
