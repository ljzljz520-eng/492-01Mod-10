package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Project;
import com.scaffolding.entity.User;
import com.scaffolding.service.ProjectService;
import com.scaffolding.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/project")
@Api(tags = "项目管理")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @PostMapping
    @ApiOperation("新增项目")
    public Result<Project> save(@RequestBody Project project) {
        try {
            project.setCreateTime(LocalDateTime.now());
            project.setUpdateTime(LocalDateTime.now());
            projectService.save(project);
            return Result.success("新增成功", project);
        } catch (Exception e) {
            log.error("新增项目失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新项目")
    public Result<Project> update(@PathVariable Long id, @RequestBody Project project) {
        try {
            project.setId(id);
            project.setUpdateTime(LocalDateTime.now());
            projectService.updateById(project);
            return Result.success("更新成功", project);
        } catch (Exception e) {
            log.error("更新项目失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除项目")
    public Result<?> delete(@PathVariable Long id) {
        try {
            projectService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除项目失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询项目")
    public Result<Project> getById(@PathVariable Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        return Result.success(project);
    }

    @GetMapping("/list")
    @ApiOperation("查询项目列表（不分页，用于下拉选择）")
    public Result<List<Project>> list(@RequestParam(required = false) Long userId) {
        Long enterpriseId = null;
        Long laborCompanyId = null;
        String userRole = null;
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                userRole = user.getUserRole();
                enterpriseId = user.getEnterpriseId();
                laborCompanyId = user.getLaborCompanyId();
            }
        }
        Page<Project> page = projectService.pageQuery(1L, 1000L, null, null, userId, userRole, enterpriseId, laborCompanyId);
        return Result.success(page.getRecords());
    }

    @GetMapping("/page")
    @ApiOperation("分页查询项目")
    public Result<PageResult<Project>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String projectStatus,
            @RequestParam(required = false) Long userId) {
        Long enterpriseId = null;
        Long laborCompanyId = null;
        String userRole = null;
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                userRole = user.getUserRole();
                enterpriseId = user.getEnterpriseId();
                laborCompanyId = user.getLaborCompanyId();
            }
        }
        Page<Project> page = projectService.pageQuery(current, size, projectName, projectStatus, userId, userRole, enterpriseId, laborCompanyId);
        PageResult<Project> pageResult = new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
        return Result.success(pageResult);
    }
}
