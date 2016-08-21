package fr.marouni.spark.datasets

import java.net.URL
import java.util

import fr.marouni.spark.dataframes.DataFramesDriver._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions._

/**
  * Created by abbass on 12/03/16.
  *
  * Spark Datasets
  */

case class order(company: String, client: String, item: Integer, qty: Double, price: Double)
case class companyinfo(company: String, address: String)

object DatasetsDriver extends App {

  override def main(args: Array[String]) {

    // test files
    val transactionsFile: URL = getClass.getResource("/transcations.csv")
    val directoryFile: URL = getClass.getResource("/directory.csv")

    val sparkConf = new SparkConf()
    val sc = new SparkContext("local[*]", "SQL tests", sparkConf)
    val sqlContext = new SQLContext(sc)



    // **** Creating Datasets ****

    // 1 -  DS from primitives & cases classes
    // DS From DF
    /*For loading data into a Dataset, unless a special API is provided by your data source,
    you can first load your data into a DataFrame and then convert it to a Dataset.
    Since the conversion to the Dataset simply adds information you do not
    have the problem of eagerly evaluating, and future filters and similar
    operations can still be pushed down to the data store.*/

    /*Converting to/from DataFrames is almost “free” in that
      the underlying data does not need to be changed, only extra compile time type information is added/removed.*/

    import sqlContext.implicits._

    val transStr: Dataset[String] = sqlContext.read.text(transactionsFile.getPath).as[String]
    val dirStr: Dataset[String] = sqlContext.read.text(directoryFile.getPath).as[String]
    val transDS: Dataset[order] = transStr.map({ line => {
      val splits: Array[String] = line.split(",")
      order(splits(0), splits(1), splits(2).toInt, splits(3).toDouble, splits(4).toDouble)
    }
    })
    val dirDS: Dataset[companyinfo] = dirStr.map(line => {
      val splits: Array[String] = line.split(",")
      companyinfo(splits(0), splits(1))
    })

    // Dataframes from DS
    /*val transDF: DataFrame = transDS.toDF()
    val dirDF: DataFrame = dirDS.toDF()*/

    // DS from RDD (DS by encoders) :
    /*Converting a Dataset of type T to an RDD of type T can be done by calling .rdd,
    which unlike calling toDF, does involve converting the data from the internal SQL format to the regular types.*/

    // val file: RDD[String] = sc.textFile("/home/abbass/dev/spark/sqldata/transcations.csv")
    // val rdd: RDD[order] = file.map(line => {
    //   val splits: Array[String] = line.split(",")
    //  order(splits(0), splits(1), splits(2).toInt, splits(3).toDouble, splits(4).toDouble)
    //
    // })

    // val file2: RDD[String] = sc.textFile("/home/abbass/dev/spark/sqldata/directory.csv")
    // val rdd2: RDD[companyinfo] = file2.map(line => {
    //  val splits: Array[String] = line.split(",")
    //  companyinfo(splits(0), splits(1))
    // })

    // val ds = rdd.toDS()
    // val ds2 = rdd2.toDS()

    // DS from DF :
    // val df: DataFrame = sqlContext.createDataFrame(rdd)
    // df.registerTempTable("tab1")
    // val df2: DataFrame = sqlContext.createDataFrame(rdd2)
    // df2.registerTempTable("tab2")
    // val ds: Dataset[order] = df.as[order]
    // val ds2: Dataset[companyinfo] = df2.as[companyinfo]

    // 3- DS Maps :
    // val clients: Dataset[String] = ds.map(x => x.client)
    // clients.show(10)
    // ds.select(expr("client"))

    // 4- DS Flat Maps :
    // val companysExtended = ds.flatMap(x => x.company.split('X'))
    // companysExtended.show(10)

    // 5- Group By
    // val groupedClients = ds.groupBy(new org.apache.spark.sql.Column("client"))
    // groupedClients.agg(new TypedColumn[order](new))

    //dirDS.take(1).foreach(println(_))

    // 1- SELECT
    //val averageItemDS: Dataset[Double] = transDS.select(expr("avg(item)").as[Double])
    //averageItemDS.show()

    //transDS.select(col("client").as[String]).show(10)
    // val selectMC: Dataset[(Double, String)] = transDS.select($"qty".as[Double], $"price".as[String])
    // selectMC.show(10)

    // No catalyst optimizer
    //val selecMap: Dataset[(Integer, Double)] = transDS.map({
    //  trans => (trans.item, trans.price)
    //})
    //selecMap.show(10)

    // 2- Aggregations
    /*- Locally aggregate the data on each RDD by your grouping keys
    ( First TungstenAggregate ( you can see the mode=Partial tag ) in mapreduce this would be the combiner.

      - Distribute the data by group key to a new set of RDDs so all values for
       a specific group key will end up in the same target RDD,
       in MapReduce this would be called the shuffle : TungstenExchange

      - Do the final aggregation of the pre aggregated values on the target rdds, :
      second TungstenAggregate with mode=Final

      - Do some casting and type conversion ( ConvertToSave )*/
    val groupedCity: GroupedDataset[String, order] = transDS.groupBy(_.client)
    val aggregatedClientPrices: Dataset[(String, Double)] = groupedCity.agg(sum("price").as[Double])
    aggregatedClientPrices.show()


    // 3- Joins
    //transDS.joinWith(dirDS, $"company=company").show()


    // Spark 2.0
    // transDF.selectExpr("avg(price)").show()

    // Do not quit we need to check webUI
    Thread.sleep(10000000)


  }
}
