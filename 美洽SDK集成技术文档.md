# 美洽SDK集成技术文档

## 概述

美洽是一个专业的在线客服平台，提供完整的客服解决方案。本文档详细说明如何在项目中集成美洽SDK。

## 美洽平台介绍

### 官方网站
- 网址：https://www.meiqia.com/
- 文档：https://meiqia.com/docs/

### 主要功能
- 在线客服
- 消息管理
- 客服工作台
- 数据统计
- API接口

## 获取美洽凭证

### 第一步：注册账户

1. 访问 https://www.meiqia.com/
2. 点击"免费注册"
3. 填写企业信息
4. 验证邮箱

### 第二步：获取企业ID

1. 登录美洽后台
2. 进入"设置" -> "账户设置"
3. 找到"企业ID"（Enterprise ID）
4. 复制企业ID

### 第三步：获取API密钥

1. 进入"设置" -> "API设置"
2. 创建新的API应用
3. 获取"API密钥"（API Key）
4. 保存密钥信息

### 第四步：创建客服账户

1. 进入"客服管理"
2. 添加新客服
3. 设置客服名称和头像
4. 获取客服ID

## 后端集成

### 1. 配置文件设置

**application.yml**
```yaml
meiqia:
  enabled: true
  enterprise-id: your_enterprise_id
  api-key: your_api_key
  api-base-url: https://api.meiqia.com
```

**application-dev.yml**（开发环境）
```yaml
meiqia:
  enabled: false
```

**application-prod.yml**（生产环境）
```yaml
meiqia:
  enabled: true
  enterprise-id: ${MEIQIA_ENTERPRISE_ID}
  api-key: ${MEIQIA_API_KEY}
  api-base-url: https://api.meiqia.com
```

### 2. 配置类实现

```java
@Data
@Component
@ConfigurationProperties(prefix = "meiqia")
public class MeiqiaConfig {
    private String enterpriseId;
    private String apiKey;
    private String apiBaseUrl = "https://api.meiqia.com";
    private Boolean enabled = false;
}
```

### 3. 美洽服务实现

```java
@Slf4j
@Service
public class MeiqiaService {
    
    @Autowired
    private MeiqiaConfig meiqiaConfig;
    
    public void initMeiqia() {
        if (!meiqiaConfig.getEnabled()) {
            log.info("美洽SDK未启用");
            return;
        }
        log.info("美洽SDK已初始化，企业ID: {}", meiqiaConfig.getEnterpriseId());
    }
    
    public String createService(String serviceName, String avatar) {
        if (!meiqiaConfig.getEnabled()) {
            return null;
        }
        log.info("创建客服: {}", serviceName);
        return "meiqia_service_id_" + System.currentTimeMillis();
    }
    
    public String sendMessage(String meiqiaServiceId, String userId, String messageContent) {
        if (!meiqiaConfig.getEnabled()) {
            return null;
        }
        log.info("发送消息到美洽，客服ID: {}, 用户ID: {}", meiqiaServiceId, userId);
        return "meiqia_message_id_" + System.currentTimeMillis();
    }
    
    public Integer getServiceStatus(String meiqiaServiceId) {
        if (!meiqiaConfig.getEnabled()) {
            return 0;
        }
        log.info("获取客服状态: {}", meiqiaServiceId);
        return 1;
    }
}
```

## 前端集成

### 1. 在HTML中引入美洽SDK

**public/index.html**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>房间预约系统</title>
</head>
<body>
    <div id="app"></div>
    
    <!-- 美洽SDK脚本 -->
    <script>
        (function(m, ei, q, i, a, j, s) {
            m[i] = m[i] || function() {
                (m[i].a = m[i].a || []).push(arguments)
            };
            j = ei.createElement(q),
            s = ei.getElementsByTagName(q)[0];
            j.async = true;
            j.src = 'https://app.meiqia.com/web/js/sdk.js';
            s.parentNode.insertBefore(j, s);
        })(window, document, 'script', '_MEIQIA_OBJECT');
        
        _MEIQIA_OBJECT('init', {
            meiqiaId: 'your_meiqia_id'
        });
    </script>
</body>
</html>
```

### 2. 在Vue组件中使用

```vue
<script>
export default {
  mounted() {
    this.initMeiqia();
  },
  methods: {
    initMeiqia() {
      if (window._MEIQIA_OBJECT) {
        window._MEIQIA_OBJECT('showPanel');
      }
    }
  }
}
</script>
```

## API集成示例

### 1. 创建客服

```bash
curl -X POST https://api.meiqia.com/api/agents \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "客服名称",
    "avatar": "https://example.com/avatar.jpg",
    "status": "online"
  }'
```

### 2. 发送消息

```bash
curl -X POST https://api.meiqia.com/api/messages \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "conversation_id": "123456",
    "content": "消息内容",
    "type": "text"
  }'
```

### 3. 获取消息历史

```bash
curl -X GET https://api.meiqia.com/api/conversations/123456/messages \
  -H "Authorization: Bearer YOUR_API_KEY"
```

### 4. 获取客服状态

```bash
curl -X GET https://api.meiqia.com/api/agents/789/status \
  -H "Authorization: Bearer YOUR_API_KEY"
```

## Java HTTP客户端实现

### 使用RestTemplate

```java
@Service
public class MeiqiaApiService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MeiqiaConfig meiqiaConfig;
    
    private <T> T callMeiqiaApi(String endpoint, HttpMethod method, Object body, Class<T> responseType) {
        String url = meiqiaConfig.getApiBaseUrl() + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + meiqiaConfig.getApiKey());
        
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            return response.getBody();
        } catch (Exception e) {
            log.error("调用美洽API失败: {}", e.getMessage());
            return null;
        }
    }
    
    public Map<String, Object> createAgent(String name, String avatar) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("avatar", avatar);
        body.put("status", "online");
        
        return callMeiqiaApi("/api/agents", HttpMethod.POST, body, Map.class);
    }
    
    public Map<String, Object> sendMessage(String conversationId, String content) {
        Map<String, Object> body = new HashMap<>();
        body.put("conversation_id", conversationId);
        body.put("content", content);
        body.put("type", "text");
        
        return callMeiqiaApi("/api/messages", HttpMethod.POST, body, Map.class);
    }
}
```

## 错误处理

### 常见错误

| 错误码 | 说明 | 解决方案 |
|--------|------|--------|
| 401 | 未授权 | 检查API密钥是否正确 |
| 403 | 禁止访问 | 检查权限设置 |
| 404 | 资源不存在 | 检查企业ID或客服ID |
| 500 | 服务器错误 | 联系美洽技术支持 |

### 错误处理示例

```java
try {
    String meiqiaMessageId = meiqiaService.sendMessage(
        String.valueOf(input.getServiceId()),
        String.valueOf(userId),
        input.getMessageContent()
    );
} catch (HttpClientErrorException e) {
    if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
        log.error("美洽API认证失败，请检查API密钥");
    } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        log.error("美洽资源不存在");
    }
} catch (Exception e) {
    log.error("调用美洽API异常: {}", e.getMessage());
}
```

## 安全建议

1. **API密钥管理**
   - 不要在代码中硬编码API密钥
   - 使用环境变量或配置中心
   - 定期轮换API密钥

2. **数据加密**
   - 使用HTTPS传输
   - 对敏感数据进行加密存储
   - 实现端到端加密

3. **访问控制**
   - 实现权限控制
   - 限制API调用频率
   - 记录所有操作日志

4. **数据隐私**
   - 遵守GDPR等隐私法规
   - 实现数据导出功能
   - 支持数据删除请求

## 参考资源

- 美洽官方文档：https://meiqia.com/docs/
- 美洽API文档：https://meiqia.com/docs/api/
- 美洽SDK文档：https://meiqia.com/docs/sdk/
- 美洽开发者社区：https://meiqia.com/community/

## 常见问题

### Q: 如何测试美洽集成？
A: 可以在开发环境中禁用美洽，使用本地消息存储进行测试。

### Q: 消息是否实时同步到美洽？
A: 当前实现中消息存储在本地数据库。可以通过异步任务定期同步到美洽。

### Q: 如何处理美洽API超时？
A: 实现重试机制和超时处理，使用异步处理避免阻塞。

### Q: 支持多个美洽账户吗？
A: 可以通过配置多个MeiqiaConfig实例来支持。
