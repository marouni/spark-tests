package fr.marouni.spark.stats

import java.net.URL

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by abbass on 28/08/16.
  */
  object StatsDriver extends App {

    override def main(args: Array[String]) {

      val sparkConf = new SparkConf()
      val sc = new SparkContext("local[*]", "SQL tests", sparkConf)
      val sqlContext = new SQLContext(sc)

      val input: RDD[Double] = sc.parallelize(Seq(1.4, 6.0, 3.5, 6.9, 3.6, 7.0, 8.0))

      // stats
      // println(input.stats().toString())

      // PRDD The function result becomes the key, and the original data element becomes the value in the pair.
      input.keyBy({
        x => x%2
      }).collect().foreach(println(_))


    }


}
