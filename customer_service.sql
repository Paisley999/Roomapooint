-- 客服信息表
CREATE TABLE IF NOT EXISTS `customer_service` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_name` varchar(100) NOT NULL COMMENT '客服名称',
  `service_description` varchar(500) COMMENT '客服描述',
  `avatar` varchar(500) COMMENT '客服头像URL',
  `status` int DEFAULT 0 COMMENT '客服状态 0-离线 1-在线',
  `meiqia_service_id` varchar(100) COMMENT '美洽客服ID',
  `enabled` int DEFAULT 1 COMMENT '是否启用 0-禁用 1-启用',
  `creation_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服信息表';

-- 客服消息表
CREATE TABLE IF NOT EXISTS `customer_message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `service_id` int NOT NULL COMMENT '客服ID',
  `message_content` longtext NOT NULL COMMENT '消息内容',
  `message_type` int DEFAULT 0 COMMENT '消息类型 0-用户消息 1-客服消息',
  `meiqia_message_id` varchar(100) COMMENT '美洽消息ID',
  `is_read` int DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
  `creation_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_message_type` (`message_type`),
  KEY `idx_is_read` (`is_read`),
  CONSTRAINT `fk_customer_message_user` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_customer_message_service` FOREIGN KEY (`service_id`) REFERENCES `customer_service` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服消息表';

-- 插入示例客服数据
INSERT INTO `customer_service` (`service_name`, `service_description`, `avatar`, `status`, `enabled`) VALUES
('小美', '热情的客服小美', 'https://via.placeholder.com/100?text=小美', 1, 1),
('小洽', '专业的客服小洽', 'https://via.placeholder.com/100?text=小洽', 1, 1),
('客服团队', '我们的客服团队随时为您服务', 'https://via.placeholder.com/100?text=客服', 1, 1);
