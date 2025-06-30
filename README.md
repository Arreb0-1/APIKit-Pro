# APIKit Pro

## 简介

这是一个基于[API-Security/APIKit](https://github.com/API-Security/APIKit)的增强版本，主要改进：

- 🛡️ 新增扫描开关，避免Burp启动时自动扫描
- 🚀 修复输出页面卡死问题
- 🎨 优化UI界面，改进cookie输入框布局
- 🔍 新增Bypass扫描功能

## 主要功能

### 基础功能
- 支持主动/被动扫描发现API文档
- 自动解析API文档并生成Burp数据包
- 支持多种API类型识别
<img width="1824" alt="image" src="https://github.com/user-attachments/assets/87cc6dcc-8f9d-4496-9cf4-419db66c6947" />

### 新增功能
1. **Scanner Enabled**: 扫描开关控制
<img width="666" alt="image" src="https://github.com/user-attachments/assets/88d8abbd-cd3b-4614-b1e2-1763f45baf4f" />

2. **Send with Cookie**: 支持保留原始Cookie
3. **Auto Request**: 自动扫描所有发现的API接口
4. **Bypass扫描**: 支持自定义bypass后缀(如;.js)

## 使用说明

### 自动扫描
开启**Scanner Enabled**按钮，即可被动扫描访问流量中存在的API文档，敏感路径等，开启 **Auto Request**: 自动扫描所有发现的API接口
<img width="666" alt="image" src="https://github.com/user-attachments/assets/88d8abbd-cd3b-4614-b1e2-1763f45baf4f" />

### 手动扫描
#### Do Auto API scan
右键任意请求 -> 选择"Do Auto API scan" -> 开始API指纹探测

#### Do Target API Scan
1. 右键选择"Do Target API scan"
2. 配置API类型、BasePath、文档路径等
3. 可选配置Bypass后缀进行绕过测试

`如接口时http://localhost:8089/actuator/health，拼接字段就变成了http://localhost:8089/actuator/health;.js`

`如果存在参数，如http://localhost:8089/auth/adminlogin?id=123拼接后就变成了http://localhost:8089/auth/adminlogin;.js?id=123`

<img width="1827" alt="image" src="https://github.com/user-attachments/assets/53bca247-697a-4e62-a75f-0b4eb1b5d86f" />

## 免责声明

1. 本工具仅用于授权的企业安全建设
2. 使用本工具时需确保符合当地法律法规
3. 使用本工具造成的任何非法后果由使用者承担
4. 使用本工具即视为同意本免责声明
5. 本工具仅能在取得足够合法授权的企业安全建设中使用，在使用本工具过程中，您应确保自己所有行为符合当地的法律法规。
6. 如您在使用本工具的过程中存在任何非法行为，您将自行承担所有后果，本工具所有开发者和所有贡献者不承担任何法律及连带责任。
7. 除非您已充分阅读、完全理解并接受本协议所有条款，否则，请您不要安装并使用本工具。
8. 您的使用行为或者您以其他任何明示或者默示方式表示接受本协议的，即视为您已阅读并同意本协议的约束。


## 致谢
感谢[API-Security/APIKit](https://github.com/API-Security/APIKit)项目的原作者。
