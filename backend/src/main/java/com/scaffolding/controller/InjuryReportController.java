package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.InjuryMaterial;
import com.scaffolding.entity.InjuryReport;
import com.scaffolding.entity.User;
import com.scaffolding.service.InjuryReportService;
import com.scaffolding.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/injury-report")
@Api(tags = "工伤上报管理")
public class InjuryReportController {

    @Autowired
    private InjuryReportService injuryReportService;

    @Autowired
    private UserService userService;

    @PostMapping
    @ApiOperation("主管上报工伤")
    public Result<InjuryReport> save(@RequestBody InjuryReport report, @RequestParam(required = false) Long userId) {
        try {
            String userName = "现场主管";
            if (userId != null) {
                User user = userService.getById(userId);
                if (user != null) {
                    userName = user.getNickname() != null ? user.getNickname() : user.getUsername();
                }
            }
            InjuryReport saved = injuryReportService.submitReport(report, userId, userName);
            return Result.success("上报成功", saved);
        } catch (Exception e) {
            log.error("工伤上报失败", e);
            return Result.error("上报失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新工伤上报")
    public Result<InjuryReport> update(@PathVariable Long id, @RequestBody InjuryReport report, @RequestParam(required = false) Long userId) {
        try {
            report.setId(id);
            String userName = "现场主管";
            if (userId != null) {
                User user = userService.getById(userId);
                if (user != null) {
                    userName = user.getNickname() != null ? user.getNickname() : user.getUsername();
                }
            }
            InjuryReport updated = injuryReportService.submitReport(report, userId, userName);
            return Result.success("更新成功", updated);
        } catch (Exception e) {
            log.error("更新工伤上报失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("查询工伤上报详情")
    public Result<InjuryReport> getById(@PathVariable Long id) {
        InjuryReport report = injuryReportService.getDetail(id);
        if (report == null) {
            return Result.error("记录不存在");
        }
        return Result.success(report);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询工伤上报")
    public Result<PageResult<InjuryReport>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String reportStatus,
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
        Page<InjuryReport> page = injuryReportService.pageQuery(current, size, projectId, reportStatus, userId, userRole, enterpriseId, laborCompanyId);
        PageResult<InjuryReport> pageResult = new PageResult<>(page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}/check-materials")
    @ApiOperation("检查缺失材料")
    public Result<Map<String, Object>> checkMissingMaterials(@PathVariable Long id) {
        try {
            Map<String, Object> result = injuryReportService.checkMissingMaterials(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查缺失材料失败", e);
            return Result.error("检查失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/insurance")
    @ApiOperation("劳务公司补充保险资料")
    public Result<InjuryReport> updateInsurance(@PathVariable Long id, @RequestBody InjuryReport report, @RequestParam(required = false) Long userId) {
        try {
            report.setId(id);
            String userName = "劳务公司";
            if (userId != null) {
                User user = userService.getById(userId);
                if (user != null) {
                    userName = user.getNickname() != null ? user.getNickname() : user.getUsername();
                }
            }
            InjuryReport updated = injuryReportService.updateInsuranceInfo(report, userId, userName);
            if (updated == null) {
                return Result.error("记录不存在");
            }
            return Result.success("补充成功", updated);
        } catch (Exception e) {
            log.error("补充保险资料失败", e);
            return Result.error("补充失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/conclusion")
    @ApiOperation("出具复工/停工结论")
    public Result<InjuryReport> processConclusion(
            @PathVariable Long id,
            @RequestParam String result,
            @RequestParam(required = false) String resultRemark,
            @RequestParam(required = false) Long userId) {
        try {
            String userName = "管理员";
            if (userId != null) {
                User user = userService.getById(userId);
                if (user != null) {
                    userName = user.getNickname() != null ? user.getNickname() : user.getUsername();
                }
            }
            InjuryReport report = injuryReportService.processConclusion(id, result, resultRemark, userId, userName);
            if (report == null) {
                return Result.error("记录不存在");
            }
            return Result.success("结论已出具，排班档案已更新", report);
        } catch (Exception e) {
            log.error("出具结论失败", e);
            return Result.error("出具结论失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}/materials")
    @ApiOperation("获取证明材料列表")
    public Result<List<InjuryMaterial>> getMaterials(@PathVariable Long id) {
        try {
            List<InjuryMaterial> materials = injuryReportService.getMaterials(id);
            return Result.success(materials);
        } catch (Exception e) {
            log.error("获取材料列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/material")
    @ApiOperation("上传证明材料")
    public Result<InjuryMaterial> addMaterial(@RequestBody InjuryMaterial material, @RequestParam(required = false) Long userId) {
        try {
            String userName = "上传人";
            if (userId != null) {
                User user = userService.getById(userId);
                if (user != null) {
                    userName = user.getNickname() != null ? user.getNickname() : user.getUsername();
                }
            }
            InjuryMaterial saved = injuryReportService.addMaterial(material, userId, userName);
            return Result.success("上传成功", saved);
        } catch (Exception e) {
            log.error("上传证明材料失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/material/{id}")
    @ApiOperation("删除证明材料")
    public Result<?> deleteMaterial(@PathVariable Long id) {
        try {
            injuryReportService.removeMaterial(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除证明材料失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
