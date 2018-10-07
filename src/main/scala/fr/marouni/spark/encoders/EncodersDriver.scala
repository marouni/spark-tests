package fr.marouni.spark.encoders

import fr.marouni.beans.Transaction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Encoder, Encoders, SparkSession}

object EncodersDriver extends App {

  override def main(args: Array[String]) {

    val sparkSession = SparkSession.builder()
      .appName("Encoders tests")
        .config("spark.master", "local[*]")
      .getOrCreate()

    val t1 = new Transaction("XYZ", "ABC", 123, 23.0, 1.59)
    val t2 = new Transaction("XYZ", "ABC", 123, 24.0, 1.59)

    val transRDD: RDD[Transaction] = sparkSession.sparkContext
      .makeRDD[Transaction](Seq(t1, t2))

    implicit val transactionEncoder: Encoder[Transaction] =
      Encoders.bean[Transaction](classOf[Transaction])

    val transDS = sparkSession.createDataset(transRDD)

    println(transRDD.count())
  }

}
