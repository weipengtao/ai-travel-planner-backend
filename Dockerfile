# 使用包含Maven的OpenJDK 21作为基础镜像
FROM maven:3.9-eclipse-temurin-21

# 设置工作目录
WORKDIR /app

# 复制Maven配置文件
COPY pom.xml .

# 下载依赖（利用Docker缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=docker -jar target/*.jar"]