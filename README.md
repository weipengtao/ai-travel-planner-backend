# AI旅行规划器 - 后端服务

## 项目简介

这是AI旅行规划器的后端服务，基于Spring Boot框架构建，提供RESTful API接口，处理前端请求并实现业务逻辑。

## 技术栈

- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA + Hibernate
- **安全**: Spring Security + JWT
- **AI服务**: 豆包API (大语言模型)
- **语音服务**: 科大讯飞API (语音识别)
- **地图服务**: 高德地图API

## 环境要求

- Java 21或更高版本
- Maven 3.8或更高版本
- MySQL 8.0或更高版本
- Docker (可选，用于容器化部署)

## 配置说明

### 数据库配置

application-docker.yml中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/ai_travel_planner?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: travel_user
    password: travel_pass
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 环境变量

- `SPRING_DATASOURCE_URL`: 数据库连接URL
- `SPRING_DATASOURCE_USERNAME`: 数据库用户名
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
- `JWT_SECRET`: JWT密钥
- `DOUBAO_API_KEY`: 豆包API密钥
- `DOUBAO_BASE_URL`: 豆包API基础URL
- `DOUBAO_MODEL`: 豆包AI模型名称

## 本地运行

### 1. 克隆项目

```bash
git clone [repository-url]
cd ai-travel-planner/backend
```

### 2. 配置数据库

创建MySQL数据库和表结构：

```sql
CREATE DATABASE ai_travel_planner CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'travel_user'@'%' IDENTIFIED BY 'travel_pass';
GRANT ALL PRIVILEGES ON ai_travel_planner.* TO 'travel_user'@'%';
FLUSH PRIVILEGES;
```

### 3. 修改配置

编辑`application.yml`文件，根据本地环境修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_travel_planner?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: travel_user
    password: travel_pass
```

### 4. 构建和运行

```bash
# 使用Maven构建
mvn clean package -DskipTests

# 运行应用
java -jar target/ai-travel-planner-*.jar

# 或者使用Maven运行
mvn spring-boot:run
```

## Docker部署

### 1. 构建镜像

```bash
docker build -t ai-travel-backend .
```

### 2. 使用Docker Compose

后端服务已包含在项目根目录的docker-compose.yml文件中，可以使用以下命令启动：

```bash
docker-compose up backend
```

### 3. 使用预构建镜像（阿里云）

```bash
# 拉取镜像
docker pull crpi-ttqvkor1ktw0z4rr.cn-hangzhou.personal.cr.aliyuncs.com/ai-travel-planer/ai-travel-planner-backend:latest

# 运行容器
docker run -d \
  --name ai-travel-backend \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/ai_travel_planner?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true \
  -e SPRING_DATASOURCE_USERNAME=travel_user \
  -e SPRING_DATASOURCE_PASSWORD=travel_pass \
  -e JWT_SECRET=mySecretKeyForJWTGenerationWhichShouldBeVeryLongAndSecure \
  -e DOUBAO_API_KEY=83c6548c-59d1-4d33-8943-0fa2f9e012a4 \
  -e DOUBAO_BASE_URL=https://ark.cn-beijing.volces.com/api/v3 \
  -e DOUBAO_MODEL=doubao-seed-1-6-251015 \
  -e TZ=Asia/Shanghai \
  -p 8080:8080 \
  crpi-ttqvkor1ktw0z4rr.cn-hangzhou.personal.cr.aliyuncs.com/ai-travel-planer/ai-travel-planner-backend:latest
```

## API接口

应用运行在8080端口，API路径前缀为`/api`：

- **健康检查**: `GET /api/health`
- **用户注册**: `POST /api/auth/register`
- **用户登录**: `POST /api/auth/login`
- **旅行规划**: `POST /api/travel/plan`
- **语音转文字**: `POST /api/speech-to-text`

详细的API文档请参考Swagger UI：`http://localhost:8080/swagger-ui.html`

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ai/travel/
│   │   │       ├── config/        # 配置类
│   │   │       ├── controller/    # 控制器
│   │   │       ├── dto/          # 数据传输对象
│   │   │       ├── entity/       # 实体类
│   │   │       ├── exception/    # 异常处理
│   │   │       ├── repository/   # 数据访问层
│   │   │       ├── security/     # 安全配置
│   │   │       └── service/      # 业务逻辑层
│   │   └── resources/
│   │       ├── application.yml           # 默认配置
│   │       └── application-docker.yml    # Docker环境配置
├── Dockerfile
├── pom.xml
└── README.md
```

## 常见问题

### 1. 数据库连接失败

检查数据库服务是否运行，连接参数是否正确：

```bash
# 检查MySQL服务状态
systemctl status mysql

# 测试数据库连接
mysql -h localhost -u travel_user -p
```

### 2. 端口冲突

如果8080端口已被占用，可以修改application.yml中的server.port：

```yaml
server:
  port: 8081
```

### 3. 构建失败

确保Java和Maven版本正确，清理缓存：

```bash
# 清理Maven缓存
mvn clean

# 强制更新依赖
mvn clean install -U
```

## 开发说明

### 添加新的API接口

1. 创建或修改实体类（Entity）
2. 添加数据访问层（Repository）
3. 实现业务逻辑层（Service）
4. 添加控制器（Controller）
5. 编写单元测试

### 代码规范

- 遵循Spring Boot最佳实践
- 使用RESTful API设计原则
- 添加适当的异常处理
- 编写单元测试和集成测试

## 许可证

本项目仅用于学习和教育目的。