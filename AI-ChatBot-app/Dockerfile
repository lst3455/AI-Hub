# 基础镜像
FROM openjdk:8-jre-slim
# 作者
#MAINTAINER St-Lin
# 配置
ENV PARAMS=""
# 时区
ENV TZ=Asia/Singapore
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 添加应用
ADD target/AI-ChatBot-app.jar /AI-ChatBot-app.jar
## 在镜像运行为容器后执行的命令
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /AI-ChatBot-app.jar $PARAMS"]