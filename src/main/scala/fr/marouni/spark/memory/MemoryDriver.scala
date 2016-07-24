package fr.marouni.spark.memory

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, TaskContext}

/**
  * Created by abbass on 14/02/16.
  */
object MemoryDriver extends App {

  val FACTOR = 8

  override def main(args: Array[String]): Unit ={
    val sparkConf = new SparkConf()
    sparkConf.setAppName("test")
    sparkConf.setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    val large: RDD[(Int, Int)] = sc.range(0, FACTOR).flatMap({
      x => 0 until (Math.exp(x.toDouble * 2).toInt)
    }).map({
      x => (scala.util.Random.nextInt(100), x)
    })

    val large2: RDD[(Int, Int)] = sc.range(0, FACTOR).flatMap({
      x => 0 until (Math.exp(x.toDouble * 2).toInt)
    }).map({
      x => (scala.util.Random.nextInt(100), x)
    })

    large.partitions.foreach({
      x => println(x)
        println(large.preferredLocations(x))
    })

    println(large.leftOuterJoin(large2).take(1))

    Thread.sleep(10000000)

  }
}
