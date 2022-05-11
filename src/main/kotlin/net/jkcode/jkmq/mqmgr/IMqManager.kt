package net.jkcode.jkmq.mqmgr

import net.jkcode.jkutil.common.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * 消息管理器
 *   子类实现必须有以mq配置名作为唯一参数的构造函数，在 instance() 中调用
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 6:16 PM
 */
interface IMqManager {

    // 可配置的单例
    companion object {

        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public val instsConfig: IConfig = Config.instance("mq-manager", "yaml")

        /**
         * 单例池
         */
        protected var insts: ConcurrentHashMap<String, IMqManager> = ConcurrentHashMap();

        /**
         * 获得单例
         * @param type mq类型，如 rabbitmq / kafka / redis
         * @param name mq配置名
         * @return
         */
        public fun instance(type: String, name: String): IMqManager {
            return insts.getOrPutOnce("$type:$name") {
                // 获得实现类
                val clazz = getClassByName(instsConfig[type]!!)
                // 检查bean类的构造函数
                val constructor = clazz.getConstructorOrNull(String::class.java)
                if (constructor == null)
                    throw NoSuchMethodException("IMqManager Class [$clazz] has no 1-string-arg constructor") // 无构造函数
                // 实例化: 配置名name作为构造函数参数
                constructor.newInstance(name) as IMqManager
            }
        }
    }

    /**
     * mq配置名
     */
    val name: String

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @param key 路由key, 仅对kafka有效
     * @return
     */
    fun sendMq(topic: String, msg: Any, key: String? = null): CompletableFuture<Void>

    /**
     * 订阅消息并处理
     * @param topic 消息主题
     * @param handler 消息处理函数
     */
    fun subscribeMq(topic: String, handler: (Any)->Unit)
}