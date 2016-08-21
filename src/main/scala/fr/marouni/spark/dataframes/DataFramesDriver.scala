package fr.marouni.spark.dataframes

import java.net.URL

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by abbass on 07/07/16.
  */

object DataFramesDriver extends App{

  case class Batter(id: String, _type: String)
  case class Topping(id: String, _type: String)
  case class Batters(batter: Array[Batter])
  case class Item(id: String, _type: String, name: String, ppu: Double, batters: Batters, toppings: Array[Topping])

  val itemSchema=StructType(
    StructField("id",StringType,true)::
      StructField("type",StringType,true)::
      StructField("name",StringType,true)::
      StructField("ppu",DoubleType,true)::
      StructField("batters",
        StructType(
          StructField("batter",
            ArrayType(
              StructType(
                StructField("id",StringType,true)::
                  StructField("type",StringType,true)::Nil
              )
            ),true)::Nil
        ), true)::
      StructField("topping",
        ArrayType(
          StructType(
            StructField("id",StringType,true)::
              StructField("type",StringType,true)::Nil
          )
        ),true)::Nil
  )

  override def main(args: Array[String]): Unit = {

    // test file
    val testFile: URL = getClass.getResource("/hn.json")

    val sparkConf = new SparkConf()
    val sc = new SparkContext("local[*]", "SQL tests", sparkConf)
    val hiveContext = new HiveContext(sc)

    val df: DataFrame = hiveContext
      .read
      .schema(itemSchema)
      .json(testFile.getPath)

    //df.registerTempTable("tab1")

    // hiveContext.sql("SELECT * FROM tab1 LIMIT 10").show()

    // explode() takes in an array (or a map) as an input and outputs the elements of the array (map) as separate rows.
    /*hiveContext.sql(
      """
        |SELECT DISTINCT explode(batters.batter.type) FROM tab1
      """.stripMargin).show()*/

    // A lateral view first applies the UDTF to each row of base table and then joins resulting output rows to the input rows to form a virtual table having the supplied table alias.
    /*hiveContext.sql(
      """
        |SELECT DISTINCT name, batter_type, ppu
        |FROM tab1 LATERAL VIEW explode(batters.batter.type) batter_types_table as batter_type
        |WHERE name LIKE 'C%'
        |ORDER BY batter_type DESC
      """.stripMargin).show()*/

    // Unsupported
    /*hiveContext.sql(
      """
        |SELECT *
        |FROM tab1 LATERAL VIEW explode(batters.batter.type) batter_types_table as batter_type
        |WHERE batter_type LIKE 'R%'
        |ORDER BY batter_type DESC
      """.stripMargin).show()*/

    // Push down filter
    // df.filter(df("name").contains("C")).show()

    // Push down filter on array contents
    // df.filter("array_contains(batters.batter.type, 'Blueberry')").show()

    // Experimental
    /*val df2: DataFrame = df.explode(df("batters.batter")) {
      case Row(types: Seq[Batter]) =>
        types.map({
          case Batter(id, _type) => Batter(_type, id)
        })
    }
    df2.show()*/

    // Missing Data return the first non-null column
    /*val df2: DataFrame = df.selectExpr("coalesce(ppu, name)").show()
     */

    // Drop duplicates
    /*df.dropDuplicates(Seq("ppu")).show()*/

    // Aggregates
    // df.groupBy("type").avg("ppu").show()

    //df.groupBy("type").agg(("ppu", "sum"), ("ppu", "max")).show()

    // Numeric columns stats
    //df.describe("ppu").show()

    // approximations
    //df.groupBy("type").agg(org.apache.spark.sql.functions.approxCountDistinct("ppu")).show()

    // analytics
    //df.cube("type", "name").avg("ppu").show()

    // windows
    // TBC

    // order by
    //val df0: DataFrame = df.orderBy(df("id"))
    //df0.show()

    // Read Hive table as DF
    // hiveContext.table("TABLE_NAME")
    // Refresh table's metadata
    // hiveContext.refreshTable("TABLE_NAME")


    // DF conversion
    /*Converting a DataFrame to an RDD is a transformation (not an action);
    however, converting an RDD to a DataFrame or Dataset may involve computing (or sampling some of) the input RDD.

    Creating a DataFrame from an RDD is not free in the general case.
    The data must be converted into Spark SQL’s internal format.*/

    // DF from an RDD
    // Creating a DataFrame from an RDD is not free in the general case.
    // The data must be converted into Spark SQL’s internal format.
    val rdd0: RDD[String] = sc.textFile(testFile.getPath)
    val rdd1: RDD[Row] = rdd0.map({
      line => Row(line)
    })
    val testSchema=StructType(
      StructField("line",StringType,true):: Nil
    )
    val cdf: DataFrame = hiveContext.createDataFrame(rdd1, testSchema)
    cdf.orderBy(cdf("line")).show()

    // RDD from DF
    // RDD show 1
    /*val rdd0: RDD[String] = sc.textFile(testFile.getPath)
    rdd0.take(1).foreach(println(_))
    // DF show 1
    df.rdd.take(1).foreach(println(_))*/

    Thread.sleep(100000000)
  }


}
