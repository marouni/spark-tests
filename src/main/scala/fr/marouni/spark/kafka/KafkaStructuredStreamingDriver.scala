package fr.marouni.spark.kafka

import org.apache.spark.sql.SparkSession

object KafkaStructuredStreamingDriver extends App {

  override def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .appName("Structured_Simple")
      .config("spark.master", "local[1]")
      .getOrCreate()

    val ds1 = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "test")
      .option("startingOffsets", "earliest") // equivalent of auto.offset.reset which is not allowed here
      .load()

    val counts = ds1.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")

    val query = counts.writeStream
      .format("console") // write all counts to console when updated
      .start()

    query.awaitTermination()
  }

}
