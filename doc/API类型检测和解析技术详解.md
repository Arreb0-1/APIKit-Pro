# API类型检测和解析技术详解

## 概述

APIKit Pro支持多种API技术栈的自动检测和解析，每种API类型都有其独特的检测指纹和解析逻辑。本文档详细介绍各种API类型的技术实现。

## 1. Swagger/OpenAPI 检测和解析

### 1.1 指纹检测策略

**路径枚举策略**：
```java
// 常见Swagger文档路径
String[] swaggerPaths = {
    "/swagger-resources",
    "/swagger/", "/swagger/index.html",
    "/api/", "/api/index.html",
    "/docs/", "/docs/index.html",
    "/apidocs/", "/apidocs/index.html",
    "/api-docs/", "/api-docs/index.html"
};
```

**内容特征识别**：
1. **JSON格式检测**：
   - 包含`paths`字段的JSON对象
   - 包含`apis`和`basePath`字段的Swagger 1.x格式

2. **YAML格式检测**：
   - 使用SnakeYAML解析器
   - 转换为JsonElement进行统一处理

3. **HTML页面检测**：
   ```java
   Pattern pattern = Pattern.compile("url:(\\s*)\"(.*?)\"");
   Pattern pattern2 = Pattern.compile("discoveryPaths:(\\s*)arrayFrom\\('(.*?)'\\)");
   Pattern pattern3 = Pattern.compile("\"url\":(\\s*)\"(.*?)\"");
   ```

### 1.2 文档解析流程

**SwaggerObject核心解析逻辑**：

1. **基础信息提取**：
   ```java
   public void SwaggerParseObject(JsonObject jsonObject, String basePath) {
       this.basePath = basePath;
       this.definitions = jsonObject.getAsJsonObject("definitions");
       // 解析paths字段
       JsonObject paths = jsonObject.getAsJsonObject("paths");
   }
   ```

2. **路径参数处理**：
   ```java
   // 路径参数替换示例
   "/users/{id}" → "/users/1"
   "/api/v1/{version}/users" → "/api/v1/1/users"
   ```

3. **数据类型映射**：
   ```java
   public static String replaceStr(String testStr) {
       testStr = testStr.replace("string", "test");
       testStr = testStr.replace("integer", "3");
       testStr = testStr.replace("boolean", "false");
       testStr = testStr.replace("array", "[]");
       // ... 更多类型映射
   }
   ```

4. **请求体构造**：
   - 基于schema定义生成示例JSON
   - 支持嵌套对象和数组
   - 处理引用类型($ref)

### 1.3 HTTP请求生成

**请求构造过程**：
```java
// 1. 构造请求行
String requestLine = method + " " + uri + " HTTP/1.1";

// 2. 添加必要头部
headers.add("Host: " + host);
headers.add("Content-Type: application/json");
headers.add("Accept: */*");

// 3. 构造完整请求
byte[] request = helpers.buildHttpMessage(headers, requestBody);
```

## 2. GraphQL 检测和解析

### 2.1 检测策略

**Introspection查询**：
```graphql
query Query {
  __schema {
    queryType { name }
    mutationType { name }
    subscriptionType { name }
    types {
      ...FullType
    }
    directives {
      name
      description
      locations
      args {
        ...InputValue
      }
    }
  }
}
```

**检测流程**：
1. 发送introspection查询到可能的GraphQL端点
2. 检查响应是否包含schema信息
3. 验证GraphQL特征字段

### 2.2 Schema解析

**类型系统解析**：
- Query类型：查询操作
- Mutation类型：修改操作  
- Subscription类型：订阅操作
- 自定义类型：业务对象类型

**字段提取**：
```java
// 提取所有可用的查询字段
for (JsonElement field : queryType.getAsJsonArray("fields")) {
    String fieldName = field.getAsJsonObject().get("name").getAsString();
    // 构造GraphQL查询
}
```

## 3. SOAP 检测和解析

### 3.1 WSDL检测

**检测路径**：
- `?wsdl`
- `?WSDL`
- `/wsdl`
- `/services`

**WSDL特征**：
```xml
<definitions xmlns="http://schemas.xmlsoap.org/wsdl/">
  <types>...</types>
  <message>...</message>
  <portType>...</portType>
  <binding>...</binding>
  <service>...</service>
</definitions>
```

### 3.2 SOAP消息构造

**SOAP Envelope结构**：
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Header>...</soap:Header>
  <soap:Body>
    <operation xmlns="namespace">
      <parameter>value</parameter>
    </operation>
  </soap:Body>
</soap:Envelope>
```

## 4. REST API 检测

### 4.1 RESTful特征识别

**URL模式匹配**：
```java
// RESTful URL模式
Pattern[] restPatterns = {
    Pattern.compile(".*/api/.*"),
    Pattern.compile(".*/v\\d+/.*"),
    Pattern.compile(".*/rest/.*"),
    Pattern.compile(".*/resources/.*")
};
```

**HTTP方法检测**：
- GET: 资源获取
- POST: 资源创建
- PUT: 资源更新
- DELETE: 资源删除
- PATCH: 部分更新

### 4.2 资源路径推断

**路径生成策略**：
```java
// 基于现有路径推断其他资源
"/api/users" → {
    "/api/users/{id}",
    "/api/users/{id}/profile",
    "/api/users/search"
}
```

## 5. Spring Boot Actuator 检测

### 5.1 Actuator端点枚举

**常见端点**：
```java
String[] actuatorEndpoints = {
    "/actuator",
    "/actuator/health",
    "/actuator/info",
    "/actuator/metrics",
    "/actuator/env",
    "/actuator/configprops",
    "/actuator/mappings",
    "/actuator/beans"
};
```

### 5.2 端点信息提取

**健康检查解析**：
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {...}
    }
  }
}
```

**映射信息解析**：
```json
{
  "contexts": {
    "application": {
      "mappings": {
        "dispatcherServlets": {
          "dispatcherServlet": [
            {
              "handler": "com.example.Controller#method()",
              "predicate": "{GET /api/users}"
            }
          ]
        }
      }
    }
  }
}
```

## 6. 通用技术实现

### 6.1 并发检测架构

**线程池配置**：
```java
ExecutorService executor = Executors.newFixedThreadPool(apiTypeConstructors.size());

// 为每种API类型创建检测任务
for (BiFunction<IHttpRequestResponse, Boolean, ApiType> constructor : apiTypeConstructors) {
    executor.submit(() -> {
        ApiType apiType = constructor.apply(baseRequest, isPassive);
        if (apiType.isFingerprintMatch()) {
            results.add(apiType);
        }
    });
}
```

### 6.2 缓存和去重机制

**URL扫描计数**：
```java
public class UrlScanCount {
    private final HashMap<String, Integer> urlCount = new HashMap<>();
    
    public synchronized void add(String url) {
        urlCount.put(url, urlCount.getOrDefault(url, 0) + 1);
    }
    
    public synchronized int get(String url) {
        return urlCount.getOrDefault(url, 0);
    }
}
```

### 6.3 HTTP请求优化

**Cookie管理**：
```java
public class CookieManager implements IHttpListener {
    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, 
                                 IHttpRequestResponse messageInfo) {
        // 自动处理Cookie
    }
}
```

**重定向处理**：
```java
public class RedirectUtils {
    public static boolean isRedirectedResponse(IHttpRequestResponse response) {
        int statusCode = helpers.analyzeResponse(response.getResponse()).getStatusCode();
        return statusCode >= 300 && statusCode < 400;
    }
}
```

### 6.4 错误处理和日志

**异常处理策略**：
```java
try {
    // API检测逻辑
} catch (MalformedURLException e) {
    throw new ApiKitRuntimeException(e);
} catch (Exception e) {
    BurpExtender.getStderr().println(CommonUtils.exceptionToString(e));
}
```

**日志输出**：
```java
BurpExtender.getStdout().println("Scanning\t" + requestUrl);
BurpExtender.getStderr().println("Error: " + errorMessage);
```

## 7. 性能优化

### 7.1 内存管理

**缓存清理**：
```java
public void clearScanState() {
    scannedUrl.clear();
    ApiTypeRest.scannedUrl.clear();
    ApiTypeActuator.scannedUrl.clear();
    // ... 清理其他缓存
}
```

### 7.2 网络优化

**超时控制**：
- 连接超时：5秒
- 读取超时：30秒
- 总体检测超时：5分钟

**请求复用**：
- 复用HTTP连接
- 批量处理请求

## 8. 扩展性设计

### 8.1 插件化架构

**API类型注册**：
```java
public class ApiScanner {
    private final ArrayList<BiFunction<IHttpRequestResponse, Boolean, ApiType>> 
        apiTypeConstructors = new ArrayList<>();
    
    public ApiScanner() {
        // 注册各种API类型检测器
        apiTypeConstructors.add(ApiTypeSwagger::newInstance);
        apiTypeConstructors.add(ApiTypeGraphQL::newInstance);
        // ... 可以轻松添加新的API类型
    }
}
```

### 8.2 配置化支持

**动态配置**：
- 支持运行时修改检测规则
- 支持自定义检测路径
- 支持检测开关控制

## 总结

APIKit Pro通过模块化的设计和并发处理机制，实现了对多种API技术栈的高效检测和解析。每种API类型都有其专门的检测逻辑和解析策略，同时共享通用的基础设施，确保了系统的可扩展性和维护性。