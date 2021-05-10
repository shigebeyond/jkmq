
# 概述

jkmq是封装了多个mq client, 简化mq的生产与消费, 目前仅支持 kafka / rabbitmq

## 特性
1. 简单, 易用, 轻量, 易扩展；
2. 性能高, 如 kafka 消费者可执行固定的消费者线程, 可消费多个主题

# 快速入门
下面以 kafka demo 为例

## 生产者 producer

1. 生产者配置
kafka-producer.yaml

```
default:
    bootstrap.servers: 192.168.0.170:9092 # kafka broker server, 多个用逗号分割
    acks: all # 回令类型
    retries: 0 # 重试次数
    batch.size: 16384 # 成批发送的消息大小, 单位byte
    linger.ms: 100 # 定时发送的时间间隔, 单位ms
```


2. 生产者生产消息

```
// 获得 kafka 的mq管理者实现
val mqMgr = IMqManager.instance("kafka")

// 生产消息
val topic = "topic1"
val msg = randomString(10)
val f = mqMgr.sendMq(topic, msg) // 异步发送消息, 返回 CompletableFuture 对象
```

## 消费者 consumer
1. 消费者配置
kafka-consumer.yaml

```
default:
    bootstrap.servers: 192.168.0.170:9092 # kafka broker server, 多个用逗号分割
    group.id: group1 # 消费者分组
    enable.auto.commit: true # 开启自动提交，默认5s提交一次
    auto.commit.interval.ms: 1000 # 自动提交时间间隔
    session.timeout.ms: 30000 # 消费者与服务器断开连接的最大时间
    max.poll.records: 1000 # 单次拉取的记录数
    auto.offset.reset: earliest # 读取位置: earliest/latest
    concurrency: 10 # 并行的消费者数`
```

2. 消费者订阅主题+消费处理

```
// 获得 kafka 的mq管理者实现
val mqMgr = IMqManager.instance("kafka")

// 订阅主题+消费处理
val topic = "topic1"
mqMgr.subscribeMq(topic){ msg -> // 消费处理的回调
    val t = Thread.currentThread().name
    println("$t recieve mq: topic1 - $msg")
}
```