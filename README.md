# 工程说明文档

## 环境要求
- Java17 or Java21
- SpringBoot3.X
- 搭建一个H2 Server
- 搭建一个RabbitMQ

## 脚手架
基于[阿里云原生脚手架](9https://start.aliyun.com/)搭建的项目，但修改了相关jar包版本。

## 构建fat jar maven命令
```shell
mvn clean package spring-boot:repackage -Dmaven.test.skip=true
```

## 可修改的配置

```yaml
database:
  building.type: embedded # 枚举包括：embedded、remote
  spring-batch:
    jdbc-url:  jdbc:h2:tcp://localhost:19200/D:\Program Files\H2\bin\spring-batch
    username: sa
    password: sa
    driver-class-name: org.h2.Driver

# 当database.building.type=embedded时，下面spring。h2.console下的标签的false改为true
spring.h2.console:
  settings.web-allow-others: false
  path: /h2-console #http://127.0.0.1:8080/h2-console
  enabled: false
  settings.trace: false
  
spring-batch.custom:
  remote-partition:
    type: polling # 枚举包括：aggregate、polling
```

## 工程启动后触发master节点任务
GET http://localhost:8080/endpoints/remotePartitionJob

## 多实例启动脚本
多实例在单机启动时请修改监听端口，避免端口占用。  
对等节点启动脚本：
```shell
java -jar -Dserver.port=10000 -Dmanagement.server.port=10001 -Dspring.amqp.deserialization.trust.all=true spring-batch-learn-samples.jar
```
单个master节点，多个Slave节点启动脚本：
```shell
java -jar -Dspring.profiles.avtice=master -Dserver.port=11000 -Dmanagement.server.port=11001 -Dspring.amqp.deserialization.trust.all=true spring-batch-learn-samples.jar
```
```shell
java -jar -Dspring.profiles.avtice=slave -Dserver.port=12000 -Dmanagement.server.port=12001 -Dspring.amqp.deserialization.trust.all=true spring-batch-learn-samples.jar
```

## 开发注意
1. Spring Batch Integration提供的RemotePartitioningManagerStepBuilderFactory和RemotePartitioningWorkerStepBuilderFactory，其定义的IntegrationFlow并没有启动，这里通过自开发的IntegrationFlowStarterRunner在工程启动完成后触发启动。