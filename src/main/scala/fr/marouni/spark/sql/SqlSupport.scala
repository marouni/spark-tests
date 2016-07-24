package fr.marouni.spark.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by abbass on 12/03/16.
  *
  * SQL support in Spark SQL.
  */
object SqlSupport extends App {

  case class order(company: String, client: String, item: Integer, qty: Double, price: Double)
  case class companyinfo(company: String, address: String)

  override def main(args: Array[String]) {

    val sparkConf = new SparkConf()
    val sc = new SparkContext("local[*]", "SQL tests", sparkConf)
    val sqlContext = new SQLContext(sc)

    val file: RDD[String] = sc.textFile("/home/abbass/dev/spark/sqldata/transcations.csv")
    val rdd: RDD[order] = file.map(line => {
      val splits: Array[String] = line.split(",")
      order(splits(0), splits(1), splits(2).toInt, splits(3).toDouble, splits(4).toDouble)
    })

    val file2: RDD[String] = sc.textFile("/home/abbass/dev/spark/sqldata/directory.csv")
    val rdd2: RDD[companyinfo] = file2.map(line => {
      val splits: Array[String] = line.split(",")
      companyinfo(splits(0), splits(1))
    })

    val df: DataFrame = sqlContext.createDataFrame(rdd)
    df.registerTempTable("tab1")

    val df2: DataFrame = sqlContext.createDataFrame(rdd2)
    df2.registerTempTable("tab2")

    // 1- SELECT
    // sqlContext.sql("SELECT item FROM tab1").show(10)

    // 2- Expressions in SELECT
    // sqlContext.sql("SELECT price, price * 1.25 AS taxed_price FROM tab1").show(10)

    // 3- Built-in functions
    // sqlContext.sql("SELECT price, round(price * 1.25, 1) AS taxed_price FROM tab1").show(10)

    // 4- Text concatenation
    // sqlContext.sql("SELECT concat(company, client) AS id, price, round(price * 1.25, 1) AS taxed_price FROM tab1").show(10)

    // 5- Conditions in WHERE
    // sqlContext.sql(
    //  """
    //    |SELECT * FROM tab1 WHERE
    //    |(
    //    |(qty BETWEEN 1 AND 5
    //    |AND
    //    |price <> 30.0
    //    |AND
    //    |client IN ('XXX', 'YYY', 'ZZZ')
    //    |)
    //    |OR
    //    |client LIKE 'AB%'
    //    |)
    //    |AND
    //    |company IS NOT NULL
    //  """.stripMargin).show(10)

    // 6- Handle nulls as zeros
    // sqlContext.sql(
    //  """
    //    |SELECT * FROM tab1 WHERE
    //    |coalesce(price, 0.0) < 10.0
    //  """.stripMargin).show(10)

    // 7- Aggregations (Spark SQL doesn't support ordinal position in GROUP BY statements)
    // sqlContext.sql(
    //  """
    //    |SELECT company, client, COUNT(*) as count FROM tab1
    //    |WHERE price >= 30.0
    //    |GROUP BY company, client
    //    |ORDER BY count DESC
    //  """.stripMargin).show(10)

    // 8- Having statement
    // sqlContext.sql(
    //  """
    //    |SELECT company, COUNT(*) as count FROM tab1
    //    |WHERE price >= 30.0
    //    |GROUP BY company
    //    |HAVING count > 1
    //    |ORDER BY count DESC
    //  """.stripMargin).show(10)

    // 9- Distincts
    // sqlContext.sql(
    // """
    //    |SELECT COUNT(DISTINCT company, client, item) as dcount FROM tab1
    //  """.stripMargin).show(10)

    // 10- CASE with no ordinal support
    // sqlContext.sql(
    // """
    //  |SELECT
    //  |CASE
    //  |WHEN price >= 30.0 THEN 'HIGH'
    //  |WHEN price >= 10.0 THEN 'MODERATE'
    //  |ELSE 'LOW'
    //  |END AS price_tag,
    //  |COUNT(*) as count
    //  |FROM tab1
    //  |GROUP BY (
    //  |CASE
    //  |WHEN price >= 30.0 THEN 'HIGH'
    //  |WHEN price >= 10.0 THEN 'MODERATE'
    //  |ELSE 'LOW'
    //  |END
    //  |)
    //  |ORDER BY count DESC
    // """.stripMargin).show(10)

    // 11- CASE in aggregations (Horrible exception when FROM is omitted)
    // sqlContext.sql(
    //  """
    //    |SELECT company,
    //    |SUM(
    //    | CASE
    //    |   WHEN price <= 0.0 THEN 1.0
    //    |   ELSE price
    //    |  END
    //    |) as sum
    //    |FROM tab1
    //    |GROUP BY company
    //    |ORDER BY sum DESC
    //  """.stripMargin).show(10)

    // 12- JOINS
    sqlContext.sql(
      """
        |SELECT company
        |FROM tab1
      """.stripMargin).show(10)

     sqlContext.sql(
      """
        |SELECT *
        |FROM tab2
      """.stripMargin).show(10)

     sqlContext.sql(
      """
        |SELECT tab1.company, tab1.client, tab2.address
        |FROM tab1
        |JOIN tab2 ON tab1.company = tab2.company
        |WHERE address is NULL
      """.stripMargin).show(10)

    // sqlContext.sql(
    //  """
    //    |SELECT COUNT(DISTINCT tab1.company)
    //    |FROM tab1
    //    |FULL JOIN tab2 ON tab1.company = tab2.company
    //    |WHERE address is NULL OR client IS NULL
    //  """.stripMargin).show(10)

    // sqlContext.sql(
    //  """
    //  |SELECT client, SUM(price*qty) as total_price
    //  |FROM tab1
    //  |LEFT JOIN tab2 ON tab1.company = tab2.company
    //  |WHERE address is NOT NULL
    //  |GROUP BY client
    //  |ORDER BY total_price DESC
    //  """.stripMargin).show(10)

    // http://blog.jooq.org/2016/03/17/10-easy-steps-to-a-complete-understanding-of-sql/
    /**
      * FROM
        WHERE
        GROUP BY
        HAVING
        SELECT
        DISTINCT
        UNION
        ORDER BY
      */

    // 1- Cannot use derived columns in WHERE clause :
    /*sqlContext.sql(
      """
        |SELECT price * qty as total
        | FROM tab1
        | WHERE (price*qty) > 10.0
      """.stripMargin).show(10)*/

    // 2- Table references (cartesian product MAP ONLY with no shuffle)
    /*sqlContext.sql(
      """
        |SELECT *
        |FROM tab1, tab2
      """.stripMargin).show(10)*/

    // 3- JOIN with a cartesian product
    /*sqlContext.sql(
      """
        |SELECT *
        |FROM tab1 JOIN tab2 ON tab1.company = tab2.company, tab2
      """.stripMargin).show(10)*/

    // 4- Cartesian product with condition (Doesn't generate a cartesian product)
    /*sqlContext.sql(
      """
        |SELECT *
        |FROM tab1, tab2
        |WHERE tab1.company = tab2.company
      """.stripMargin).show(10)*/

    // 5- SEMI JOINS using IN clause not supported (https://issues.apache.org/jira/browse/SPARK-4226)
    /*sqlContext.sql(
      """
        |SELECT company
        |FROM tab1
        |WHERE compnay IN (SELECT company FROM tab2)
      """.stripMargin).show(10)*/

    // 6- SEMI JOINS using EXISTS clause not supported
    /*sqlContext.sql(
      """
        |SELECT company
        |FROM tab1
        |WHERE EXISTS (SELECT company FROM tab2)
      """.stripMargin).show(10)*/

    // 7- SEMI JOIN might be replaced with INNER JOIN with DISTINCT
    /*sqlContext.sql(
      """
        |SELECT DISTINCT tab1.company
        |FROM tab1 JOIN tab2 ON tab1.company = tab2.company
      """.stripMargin).show(10)*/

    // 8- Derived Tables (Supported sub queries in FROM clause)
    /*sqlContext.sql(
      """
        |SELECT cmp, total
        |FROM (SELECT company AS cmp, price * qty AS total FROM tab1) tab1D
        |WHERE total > 10.0
      """.stripMargin).show(10)*/

    // 9- Unions (Union can only be performed on tables with the same number of columns)
    /*sqlContext.sql(
      """
        |SELECT company, client
        |FROM tab1
        |WHERE price > 1.0
        |UNION
        |SELECT company, address
        |FROM tab2
      """.stripMargin).show(10)*/

    // Do not quit we need to check webUI
    Thread.sleep(10000000)


  }
}
