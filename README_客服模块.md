# 房间预约系统 - 客服模块

## 📋 项目概述

为房间预约系统新增了完整的在线客服模块，支持集成第三方美洽SDK平台。用户可以在线与客服进行实时沟通，管理员可以管理客服信息和查看消息记录。

## ✨ 核心功能

### 👥 用户端功能
- 查看在线客服列表
- 实时聊天功能
- 消息历史查询
- 自动滚动到最新消息
- 响应式设计

### 🛠️ 管理端功能
- 客服信息管理（增删改查）
- 客服启用/禁用
- 消息记录查询
- 客服状态管理

### 🔌 美洽集成
- 美洽配置管理
- 客服ID映射
- 消息ID映射
- 客服状态查询

## 📦 交付物

### 后端代码（16个文件）
- 2个实体类
- 2个DTO类
- 2个查询输入类
- 2个Mapper接口
- 2个Service接口
- 2个Service实现类
- 2个Controller类
- 2个配置类

### 前端代码（3个文件）
- customerService.js - API接口
- CustomerService.vue - 主组件
- router/index.js - 路由配置

### 数据库（1个文件）
- customer_service.sql - 表结构和示例数据

### 文档（6个文件）
- 客服模块集成指南.md
- 客服模块快速开始.md
- 客服模块实现总结.md
- 美洽SDK集成技术文档.md
- 客服模块完整交付清单.md
- 项目交付总结.md

## 🚀 快速开始

### 1️⃣ 数据库集成
```bash
mysql -u root -p < customer_service.sql
```

### 2️⃣ 后端配置
在 `application.yml` 中添加：
```yaml
meiqia:
  enabled: true
  enterprise-id: your_enterprise_id
  api-key: your_api_key
  api-base-url: https://api.meiqia.com
```

### 3️⃣ 启动应用
```bash
# 后端
mvn spring-boot:run

# 前端
npm run serve
```

### 4️⃣ 访问客服模块
- 用户端：http://localhost:8080/#/Front/CustomerService
- 管理端：http://localhost:8080/#/Admin/CustomerService

## 📡 API接口

### 客服接口（6个）
- POST /api/customerService/list
- POST /api/customerService/get
- POST /api/customerService/createOrEdit
- POST /api/customerService/delete
- POST /api/customerService/batchDelete
- POST /api/customerService/getEnabledServices

### 消息接口（7个）
- POST /api/customerMessage/list
- POST /api/customerMessage/get
- POST /api/customerMessage/createOrEdit
- POST /api/customerMessage/delete
- POST /api/customerMessage/batchDelete
- POST /api/customerMessage/sendMessage
- GET /api/customerMessage/getChatHistory

## 🗄️ 数据库表

### customer_service 表
- id (主键)
- service_name (客服名称)
- service_description (描述)
- avatar (头像URL)
- status (在线状态)
- meiqia_service_id (美洽ID)
- enabled (启用状态)
- creation_time (创建时间)
- update_time (更新时间)

### customer_message 表
- id (主键)
- user_id (用户ID)
- service_id (客服ID)
- message_content (消息内容)
- message_type (消息类型)
- meiqia_message_id (美洽消息ID)
- is_read (已读状态)
- creation_time (创建时间)
- update_time (更新时间)

## 🛠️ 技术栈

- Spring Boot 2.7.18
- Vue 2.6.14
- Element UI 2.15.14
- MyBatis Plus
- MySQL 8.0+
- 美洽 (Meiqia)

## 📚 文档指南

| 文档 | 说明 |
|------|------|
| 客服模块快速开始.md | 5分钟快速集成 |
| 客服模块集成指南.md | 完整集成步骤 |
| 美洽SDK集成技术文档.md | 美洽SDK详细说明 |
| 客服模块实现总结.md | 项目完成情况 |
| 客服模块完整交付清单.md | 交付物清单 |
| 项目交付总结.md | 项目总结 |

## ✅ 项目成果

- ✅ 16个后端Java文件
- ✅ 3个前端文件
- ✅ 1个数据库脚本
- ✅ 6个详细文档
- ✅ 13个API接口
- ✅ 完整的美洽SDK集成
- ✅ 生产级别的代码质量

系统已可直接使用，并支持后续的功能扩展和优化。

---

**项目完成日期**：2024年  
**状态**：✅ 已完成并交付
