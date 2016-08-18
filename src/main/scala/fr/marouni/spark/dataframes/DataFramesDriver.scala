package fr.marouni.spark.dataframes

import java.net.URL

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, DataFrame, SQLContext}
import org.apache.spark.{SparkContext, SparkConf}

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

    df.registerTempTable("tab1")

    //hiveContext.sql("SELECT * FROM tab1 LIMIT 10").show()

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
    df.cube("type", "name").avg("ppu").show()

    // windows
    // TBC






  }


}
