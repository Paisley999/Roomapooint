-- 积分商城相关表结构

-- 1. 商品表
CREATE TABLE IF NOT EXISTS `integral_mall_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `category` varchar(50) NOT NULL COMMENT '商品分类(coupon/gift/service)',
  `integral_price` int NOT NULL COMMENT '积分价格',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `description` varchar(500) COMMENT '商品描述',
  `image_url` varchar(255) COMMENT '商品图片URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(1:上架,0:下架)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分商城商品表';

-- 2. 兑换记录表
CREATE TABLE IF NOT EXISTS `integral_mall_exchange_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `integral_price` int NOT NULL COMMENT '消耗积分',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '兑换数量',
  `exchange_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(1:成功,0:失败)',
  `remark` varchar(255) COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_exchange_time` (`exchange_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分商城兑换记录表';

-- 3. 插入示例数据
INSERT INTO `integral_mall_product` (`product_name`, `category`, `integral_price`, `stock`, `description`, `status`) VALUES
('自习室延时卡', 'coupon', 50, 100, '延长自习室使用时间1小时', 1),
('优先预约券', 'coupon', 100, 50, '优先预约自习室座位', 1),
('免费咖啡券', 'gift', 30, 200, '自习室咖啡免费一杯', 1),
('学习用品礼包', 'gift', 80, 30, '包含笔记本、笔等学习用品', 1),
('VIP会员卡', 'service', 200, 20, '享受VIP会员权益一个月', 1),
('专业咨询服务', 'service', 150, 15, '获得专业学习咨询一次', 1);

-- 4. 创建积分消费记录视图（用于统计）
CREATE OR REPLACE VIEW `v_integral_mall_statistics` AS
SELECT 
  DATE(exchange_time) as exchange_date,
  COUNT(*) as exchange_count,
  SUM(integral_price) as total_integral,
  COUNT(DISTINCT user_id) as user_count
FROM `integral_mall_exchange_record`
WHERE status = 1
GROUP BY DATE(exchange_time);
