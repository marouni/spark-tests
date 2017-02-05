package fr.marouni.spark.joins

import java.net.URL
import java.util

import fr.marouni.spark.datasets.DatasetsDriver._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{TypedColumn, Dataset, DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by abbass on 12/03/16.
  *
  * Spark Datasets
  */

case class order(company: String, client: String, item: Integer, qty: Double, price: Double)
case class companyinfo(company: String, address: String)

object JoinsDriver extends App {

  override def main(args: Array[String]) {

    // Keys
    /* The default join operation in Spark includes only values for keys present in both RDDs,
    and in the case of multiple values per key, provides all permutations of the key/value pair.
      The best scenario for a standard join is when both RDDs contain the same set of distinct keys.
    With duplicate keys, the size of the data may expand dramatically causing performance issues,
    and if one key is not present in both RDDs you will loose that row of data.*/

    // test files
    val transactionsFile: URL = getClass.getResource("/transcations.csv")
    val directoryFile: URL = getClass.getResource("/directory.csv")

    val sparkConf = new SparkConf()
    val sc = new SparkContext("local[*]", "SQL tests", sparkConf)
    val sqlContext = new SQLContext(sc)

    val file: RDD[String] = sc.textFile(transactionsFile.getPath)
    val rdd: RDD[order] = file.map(line => {
      val splits: Array[String] = line.split(",")
      order(splits(0), splits(1), splits(2).toInt, splits(3).toDouble, splits(4).toDouble)

    })

    val file2: RDD[String] = sc.textFile(directoryFile.getPath)
    val rdd2: RDD[companyinfo] = file2.map(line => {
      val splits: Array[String] = line.split(",")
      companyinfo(splits(0), splits(1))
    })


    val company: RDD[(String, order)] = rdd.map(x => (x.company, x))
    val companyInfo: RDD[(String, companyinfo)] = rdd2.map(x => (x.company, x))

    // Joins with duplicate keys
    // company.distinct().join(companyInfo).take(10).foreach(println(_))

    // 1- Joins
    // val joined: RDD[(String, (order, companyinfo))] = company.join(companyInfo)
    // joined.sample(false, 0.1).take(10).foreach(println(_))

    // 2- CoGroup
    // val cogroup: RDD[(String, (Iterable[order], Iterable[companyinfo]))] = company.cogroup(companyInfo)
    // cogroup.take(10).foreach(println(_))

    // 3- Left outer join
    // val cogrouped: RDD[(String, (Iterable[order], Iterable[companyinfo]))] = company.cogroup(companyInfo)
    // val leftOuterJoin: RDD[(String, order, companyinfo)] = cogrouped.flatMap(x => {
    //  val left = x._2._1
    //  val right = x._2._2
    //  val buffer: ArrayBuffer[(String, order, companyinfo)] = new ArrayBuffer()
    //  left.foreach(t =>
    //    if(right.size == 0) buffer.+=((x._1, t, null))
    //    else
    //    right.foreach(u =>
    //      buffer.+=((x._1, t, u))
    //    )
    //  )
    //  buffer
    // })
    // leftOuterJoin.sample(false, 0.1).take(10).foreach(println(_))

    // 4- Cartesian Product :
    /*val cartesian: RDD[((String, order), (String, companyinfo))] = company.cartesian(companyInfo)
    val tbs: RDD[(String, (order, companyinfo))] = cartesian.map(x => (x._1._1 + x._2._1, (x._1._2, x._2._2)))
    val sorted: RDD[(String, (order, companyinfo))] = tbs.sortByKey()
    sorted.take(10).foreach(println(_)) */

    // Join optimizations
    // Reduce before joining
    /*company.join(companyInfo)
      .reduceByKey((x, y) => if(x._1.price > y._1.price) x else y)
      .take(20).foreach(println(_))
    company.reduceByKey((x, y) => if(x.price > y.price) x else y)
      .join(companyInfo)
      .take(20).foreach(println(_))*/

    // Skewed Join
    /*Sometimes not all of our smaller RDD will fit into memory, but some keys are so over-represented in the large
      data set, so you want to broadcast just the most common keys.This is especially useful if one key is so
      large that it can’t fit on a single partition.In this case you can use countByKeyApprox 2 on the large
      RDD to get an approximate idea of which keys would most benefit from a broadcast.You then filter the smaller
    RDD for only these keys, collecting the result locally in a HashMap. Using sc.broadcast you can broadcast the
    HashMap so that each worker only has one copy and manually perform the join against the HashMap.
      Using the same HashMap you can then filter our large RDD down to not include the large number of
    duplicate keys and perform our standard join, unioning it with the result of our manual join.
    This approach is quite convoluted but may allow you to handle highly skewed data you couldn’t otherwise process.*/

    val key: RDD[(String, Long)] = company.countApproxDistinctByKey()
    key.map(x => (x._2, x._1)).sortByKey(ascending = false).take(50).foreach(println(_))

    // Do not quit we need to check webUI
    Thread.sleep(10000000)


  }
}
