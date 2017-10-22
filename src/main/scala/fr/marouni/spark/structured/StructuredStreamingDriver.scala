package fr.marouni.spark.structured

import java.text.SimpleDateFormat

import org.apache.spark.sql.SparkSession

/**
  * Created by abbass on 04/06/17.
  */
object StructuredStreamingDriver extends App {

  override def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Test_SS").master("local[*]").getOrCreate()

    val input = spark.read.json(getClass.getResource("/enron.json").getPath)

    // Count
    println(input.count())

    // Schema
    input.printSchema()

    // Schema to CC
    // val s2cc = new Schema2CaseClass
    // import s2cc.implicits._
    // println(s2cc.schemaToCaseClass(input.schema, "MyClass"))

    // date to TS
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val StringToDate : (String => java.sql.Timestamp) = (arg : String) =>
      new java.sql.Timestamp(format.parse(arg).getTime)
    import org.apache.spark.sql.functions._
    val StringToDateUDF = udf(StringToDate)
    val inputTS = input.withColumn("ts", StringToDateUDF(input.col("date")))
    inputTS.printSchema()
    inputTS.cache()

    val count = inputTS.groupBy(window(inputTS.col("ts"), "1 hour")).count()
    count.sort("window").show()

    //count.show()

    //count.createOrReplaceTempView("count")
    //spark.sql("SELECT MAX(count) FROM count").show()

  }
}