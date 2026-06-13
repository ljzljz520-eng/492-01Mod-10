package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Work;

public interface WorkService extends IService<Work> {

    Page<Work> pageQuery(Long current, Long size, String workName, String workStatus, String priority, Long projectId, String injuryStatus, Long userId, String userRole, Long enterpriseId, Long laborCompanyId);
}
