# 基础镜像：使用 Java 17 JDK Slim 版本
FROM openjdk:17-jdk-slim

# 时区配置
ENV TZ=Asia/Singapore
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 应用参数（可选）
ENV PARAMS=""

# 将编译好的 JAR 包添加到镜像中
ADD target/AI-ChatBot-app.jar /AI-ChatBot-app.jar

# 启动命令：使用 JAVA_OPTS 和 PARAMS 运行 JAR
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /AI-ChatBot-app.jar $PARAMS"]
