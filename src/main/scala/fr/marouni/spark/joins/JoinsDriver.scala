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
    val cartesian: RDD[((String, order), (String, companyinfo))] = company.cartesian(companyInfo)
    val tbs: RDD[(String, (order, companyinfo))] = cartesian.map(x => (x._1._1 + x._2._1, (x._1._2, x._2._2)))
    val sorted: RDD[(String, (order, companyinfo))] = tbs.sortByKey()
    sorted.take(10).foreach(println(_))

    // Do not quit we need to check webUI
    Thread.sleep(10000000)


  }
}
