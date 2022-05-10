package net.jkcode.jkmq

import io.netty.channel.DefaultEventLoop
import net.jkcode.jkmq.mqmgr.IMqManager
import net.jkcode.jkutil.common.randomBoolean
import net.jkcode.jkutil.common.randomString
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.junit.Test
import java.util.*
import java.time.Duration;
import java.util.function.Consumer

class RedisMqTests {

    public val mqMgr = IMqManager.instance("redis")


    @Test
    fun testProducer() {
        for(i in 0..1000) {
            sendMq()
            Thread.sleep(100)
        }
    }

    private fun sendMq() {
        val topic = if (randomBoolean()) "topic1" else "topic2"
        val msg = randomString(10)
        val f = mqMgr.sendMq(topic, msg)
        f.get()
        val t = Thread.currentThread().name
        println("$t send mq: $topic - $msg")
    }

    @Test
    fun testConsumer() {
        mqMgr.subscribeMq("topic1"){ msg ->
            val t = Thread.currentThread().name
            println("$t recieve mq: topic1 - $msg")
        }
        mqMgr.subscribeMq("topic2"){ msg ->
            val t = Thread.currentThread().name
            println("$t recieve mq: topic2 - $msg")
        }
        Thread.sleep(10000000000)
    }

}