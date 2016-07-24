package fr.marouni.spark.skews

import java.net.URL

import fr.marouni.spark.joins.JoinsDriver._
import fr.marouni.spark.sql.SqlSupport.{companyinfo, order}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, DataFrame}
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by abbass on 31/03/16.
  */
object DataSkews extends App {

  case class bd(part: Integer, value: Integer)
  case class sd(key: Integer, name: String)

  override def main(args: Array[String]):Unit = {

    // test files
    val transactionsFile: URL = getClass.getResource("/transactions_2")
    val directoryFile: URL = getClass.getResource("/directory_2")

    val sparkConf = new SparkConf()
    val sc = new SparkContext("local[*]", "SQL tests", sparkConf)
    val sqlContext = new SQLContext(sc)

    val file: RDD[String] = sc.textFile(transactionsFile.getPath)
    val rdd: RDD[bd] = file.map(line => {
      val splits: Array[String] = line.split(";")
      bd(splits(0).toInt, splits(1).toInt)

    })

    val file2: RDD[String] = sc.textFile(directoryFile.getPath)
    val rdd2: RDD[sd] = file2.map(line => {
      val splits: Array[String] = line.split(";")
      sd(splits(0).toInt, splits(1))
    })

    val prdd: RDD[(Integer, Integer)] = rdd.map(x => (x.part, x.value))
    val prdd2: RDD[(Integer, String)] = rdd2.map(x => (x.key, x.name))

    /*prdd.partitionBy(new org.apache.spark.HashPartitioner(12)).cache()
    prdd2.partitionBy(new org.apache.spark.HashPartitioner(12)).cache()*/

    val join: RDD[(Integer, (Integer, String))] = prdd.join(prdd2, new org.apache.spark.HashPartitioner(12))
    join.count()

    /*val df: DataFrame = sqlContext.createDataFrame(rdd)
    df.registerTempTable("tab1")

    val df2: DataFrame = sqlContext.createDataFrame(rdd2)
    df2.registerTempTable("tab2")

    // 1- SELECT
    sqlContext.sql("SELECT * FROM tab1").show(10)
    sqlContext.sql("SELECT * FROM tab2").show(10)*/

    // Do not quit we need to check webUI
    Thread.sleep(10000000)

  }
}
