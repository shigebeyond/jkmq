package net.jkcode.jkmq.mqmgr.redis

import net.jkcode.jkmq.mqmgr.IMqManager
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.VoidFuture
import net.jkcode.jkutil.redis.JedisFactory
import net.jkcode.jkutil.serialize.ISerializer
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.ProducerRecord
import redis.clients.jedis.BinaryJedisPubSub
import redis.clients.jedis.Jedis
import java.util.concurrent.CompletableFuture

/**
 * 基于redis实现的消息管理器
 * @author shijianhang<772910474@qq.com>
 * @date 2022-05-10 6:16 PM
 */
class RedisMqManager(protected val configName: String = "default") : IMqManager {

    /**
     * redis配置
     */
    public val config = Config.instance("redis.${configName}", "yaml")

    /**
     * 序列器
     */
    public val serializer: ISerializer = ISerializer.instance(config["serializer"]!!)

    /**
     * redis连接
     */
    protected val jedis: Jedis
        get() {
            return JedisFactory.getConnection(configName)
        }

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @param key 路由key, 仅对kafka有效
     * @return
     */
    override fun sendMq(topic: String, msg: Any, key: String?): CompletableFuture<Void> {
        val data = serializer.serialize(msg)!! // 序列化
        // 推入消息到redis消息通道
        jedis.publish(topic.toByteArray(), data)
        return VoidFuture
    }

    /**
     * 订阅消息并处理
     * @param topic 消息主题
     * @param handler 消息处理函数
     */
    override fun subscribeMq(topic: String, handler: (Any) -> Unit) {
        val sub = object: BinaryJedisPubSub(){
            // 收到消息
            override fun onMessage(channel: ByteArray, message: ByteArray) {
                val data = serializer.unserialize(message)!! // 反序列化
                handler.invoke(data)
            }
        }
        // 监听redis消息通道
        jedis.subscribe(sub, topic.toByteArray())
    }

}