package fr.marouni.spark.encoders

import fr.marouni.beans.SimpleTransaction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}

object SimpleEncodersDriver extends App {

  override def main(args: Array[String]) {

    val sparkSession = SparkSession.builder()
      .appName("Encoders tests")
        .config("spark.master", "local[*]")
      .getOrCreate()

    val t1 = new SimpleTransaction("XYZ", "ABC", 123, 23.0, 1.59)
    val t2 = new SimpleTransaction("XYZ", "ABC", 123, 24.0, 1.59)

    val transRDD: RDD[SimpleTransaction] = sparkSession.sparkContext
      .makeRDD[SimpleTransaction](Seq(t1, t2))

    implicit val transactionEncoder: Encoder[SimpleTransaction] =
      Encoders.bean[SimpleTransaction](classOf[SimpleTransaction])

    val transDS = sparkSession.createDataset(transRDD)

    println(transRDD.count())
  }

}
