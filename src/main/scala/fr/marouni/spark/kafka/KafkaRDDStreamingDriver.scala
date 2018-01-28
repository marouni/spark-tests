package fr.marouni.spark.kafka

import java.util
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object KafkaRDDStreamingDriver extends App {

  override def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SimpleStreaming").setMaster("local[1]")
    val sc = new SparkContext(conf)

    // streams will produce data every second
    val ssc = new StreamingContext(sc, Seconds(10))

    // this many messages
    val max = 1000

    // Stream properties
    val consumerConfig: Properties = new Properties
    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "g1")
    consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getCanonicalName)
    consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val kafkaStream =
      KafkaUtils.createDirectStream(
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](
          util.Arrays.asList("test"),
          consumerConfig.asInstanceOf[java.util.Map[String, Object]]
        )

      )

    // now, whenever this Kafka stream produces data the resulting RDD will be printed
    kafkaStream.foreachRDD(r => {
      println("*** got an RDD, size = " + r.count())
      r.foreach(s => println(s))
      if (r.count() > 0) {
        // let's see how many partitions the resulting RDD has -- notice that it has nothing
        // to do with the number of partitions in the RDD used to publish the data (4), nor
        // the number of partitions of the topic (which also happens to be four.)
        println("*** " + r.getNumPartitions + " partitions")
        r.glom().foreach(a => println("*** partition size = " + a.size))
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }

}
