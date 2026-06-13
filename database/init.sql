SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `scaffolding_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `scaffolding_db`;

-- 文件信息表
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) DEFAULT '0' COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `file_extension` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件信息表';

-- 项目表
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `project_code` varchar(50) NOT NULL COMMENT '项目编号',
  `enterprise_id` bigint(20) DEFAULT NULL COMMENT '所属企业ID',
  `enterprise_name` varchar(100) DEFAULT NULL COMMENT '所属企业名称',
  `labor_company_id` bigint(20) DEFAULT NULL COMMENT '劳务公司ID',
  `labor_company_name` varchar(100) DEFAULT NULL COMMENT '劳务公司名称',
  `project_address` varchar(255) DEFAULT NULL COMMENT '项目地址',
  `project_status` varchar(20) DEFAULT 'active' COMMENT '项目状态（active-进行中，completed-已完成，suspended-暂停）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_code` (`project_code`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_labor_company_id` (`labor_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 排班档案表（扩展工作管理表，关联项目）
DROP TABLE IF EXISTS `work`;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `work_name` varchar(100) NOT NULL COMMENT '工作名称',
  `work_content` text COMMENT '工作内容',
  `work_status` varchar(20) DEFAULT 'pending' COMMENT '工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）',
  `work_time` datetime DEFAULT NULL COMMENT '工作时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `priority` varchar(20) DEFAULT 'normal' COMMENT '优先级（low-低，normal-普通，high-高，urgent-紧急）',
  `project_id` bigint(20) DEFAULT NULL COMMENT '所属项目ID',
  `project_name` varchar(100) DEFAULT NULL COMMENT '所属项目名称',
  `work_shift` varchar(50) DEFAULT NULL COMMENT '岗位班次（早班、中班、晚班、白班等）',
  `work_position` varchar(100) DEFAULT NULL COMMENT '工作岗位',
  `injury_status` varchar(20) DEFAULT 'normal' COMMENT '工伤状态（normal-正常，injury-工伤停工，recovered-已复工）',
  `injury_report_id` bigint(20) DEFAULT NULL COMMENT '关联工伤上报ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_work_time` (`work_time`),
  KEY `idx_priority` (`priority`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_injury_status` (`injury_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班档案表';

-- 用户表（扩展角色和所属企业/劳务公司）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（账号）',
  `password` varchar(100) NOT NULL COMMENT '密码（不加密）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `user_role` varchar(20) DEFAULT 'supervisor' COMMENT '用户角色（admin-管理员，supervisor-现场主管，labor-劳务公司，enterprise-企业）',
  `enterprise_id` bigint(20) DEFAULT NULL COMMENT '所属企业ID',
  `enterprise_name` varchar(100) DEFAULT NULL COMMENT '所属企业名称',
  `labor_company_id` bigint(20) DEFAULT NULL COMMENT '所属劳务公司ID',
  `labor_company_name` varchar(100) DEFAULT NULL COMMENT '所属劳务公司名称',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_user_role` (`user_role`),
  KEY `idx_enterprise_id` (`enterprise_id`),
  KEY `idx_labor_company_id` (`labor_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 工伤上报表
DROP TABLE IF EXISTS `injury_report`;
CREATE TABLE `injury_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_no` varchar(50) NOT NULL COMMENT '工伤上报编号',
  `project_id` bigint(20) NOT NULL COMMENT '所属项目ID',
  `project_name` varchar(100) NOT NULL COMMENT '所属项目名称',
  `work_id` bigint(20) DEFAULT NULL COMMENT '关联排班档案ID',
  `injury_time` datetime NOT NULL COMMENT '受伤时间',
  `injury_location` varchar(255) NOT NULL COMMENT '受伤地点',
  `witnesses` varchar(500) DEFAULT NULL COMMENT '见证人（多个用逗号分隔）',
  `hospital` varchar(200) DEFAULT NULL COMMENT '送医医院',
  `work_shift` varchar(50) DEFAULT NULL COMMENT '岗位班次',
  `work_position` varchar(100) DEFAULT NULL COMMENT '工作岗位',
  `injured_person_name` varchar(50) NOT NULL COMMENT '受伤人员姓名',
  `injured_person_idcard` varchar(50) DEFAULT NULL COMMENT '受伤人员身份证号',
  `injured_person_phone` varchar(20) DEFAULT NULL COMMENT '受伤人员电话',
  `injury_description` text COMMENT '伤情描述',
  `report_status` varchar(20) DEFAULT 'pending' COMMENT '上报状态（pending-待补充材料，submitted-已提交，reviewing-审核中，approved-已通过，rejected-已驳回）',
  `injury_result` varchar(20) DEFAULT NULL COMMENT '处理结论（recovered-复工，suspended-停工）',
  `result_time` datetime DEFAULT NULL COMMENT '结论时间',
  `result_remark` varchar(500) DEFAULT NULL COMMENT '结论备注',
  `report_user_id` bigint(20) DEFAULT NULL COMMENT '上报人ID（主管）',
  `report_user_name` varchar(50) DEFAULT NULL COMMENT '上报人姓名',
  `report_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  `labor_user_id` bigint(20) DEFAULT NULL COMMENT '劳务处理人ID',
  `labor_user_name` varchar(50) DEFAULT NULL COMMENT '劳务处理人姓名',
  `labor_process_time` datetime DEFAULT NULL COMMENT '劳务处理时间',
  `insurance_company` varchar(100) DEFAULT NULL COMMENT '保险公司',
  `insurance_policy_no` varchar(100) DEFAULT NULL COMMENT '保险单号',
  `insurance_coverage` decimal(15,2) DEFAULT NULL COMMENT '保险保额',
  `insurance_remark` varchar(500) DEFAULT NULL COMMENT '保险备注',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_no` (`report_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_work_id` (`work_id`),
  KEY `idx_report_status` (`report_status`),
  KEY `idx_injury_time` (`injury_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤上报表';

-- 工伤证明材料表
DROP TABLE IF EXISTS `injury_material`;
CREATE TABLE `injury_material` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `injury_report_id` bigint(20) NOT NULL COMMENT '工伤上报ID',
  `material_type` varchar(50) NOT NULL COMMENT '材料类型（scene_photo-现场照片，medical_record-医疗记录，id_card-身份证，witness_statement-见证人证言，hospital_receipt-医院收据，insurance_policy-保单，other-其他）',
  `material_name` varchar(200) NOT NULL COMMENT '材料名称',
  `file_info_id` bigint(20) DEFAULT NULL COMMENT '文件信息ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名称',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件路径',
  `upload_user_id` bigint(20) DEFAULT NULL COMMENT '上传人ID',
  `upload_user_name` varchar(50) DEFAULT NULL COMMENT '上传人姓名',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_injury_report_id` (`injury_report_id`),
  KEY `idx_material_type` (`material_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工伤证明材料表';

-- 插入默认admin账号
INSERT INTO `user` (`username`, `password`, `nickname`, `user_role`) VALUES ('admin', '123456', '管理员', 'admin');
-- 插入测试账号
INSERT INTO `user` (`username`, `password`, `nickname`, `user_role`, `phone`) VALUES 
('supervisor', '123456', '张主管', 'supervisor', '13800138001'),
('labor', '123456', '李劳务', 'labor', '13800138002'),
('enterprise', '123456', '王企业', 'enterprise', '13800138003');

-- 插入测试项目
INSERT INTO `project` (`project_name`, `project_code`, `enterprise_id`, `enterprise_name`, `labor_company_id`, `labor_company_name`, `project_address`, `project_status`) VALUES 
('城东写字楼项目', 'PRJ2024001', 4, '城东建设集团', 3, '诚信劳务公司', '北京市朝阳区建国路88号', 'active'),
('城西商业广场项目', 'PRJ2024002', 4, '城东建设集团', 3, '诚信劳务公司', '北京市海淀区中关村大街1号', 'active');

-- 插入测试排班档案
INSERT INTO `work` (`work_name`, `work_content`, `work_status`, `work_time`, `start_time`, `end_time`, `priority`, `project_id`, `project_name`, `work_shift`, `work_position`) VALUES 
('钢筋绑扎作业', '完成A区3层钢筋绑扎工作', 'in_progress', '2024-06-13 08:00:00', '2024-06-13 08:00:00', '2024-06-13 18:00:00', 'high', 1, '城东写字楼项目', '白班', '钢筋工'),
('混凝土浇筑', '完成B区地下车库混凝土浇筑', 'pending', '2024-06-14 08:00:00', '2024-06-14 08:00:00', '2024-06-14 20:00:00', 'urgent', 1, '城东写字楼项目', '白班', '混凝土工');

SET FOREIGN_KEY_CHECKS = 1;
