package fr.marouni.spark.ml.regression

import java.net.URL

import fr.marouni.spark.ml.regression.DTDriver._
import org.apache.spark.ml.classification.LogisticRegressionModel
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkFiles, SparkContext, SparkConf}

/**
  * Created by abbass on 12/04/16.
  */
object LRDriver extends App {

  override def main (args: Array[String]): Unit = {

    // test files
    val trainingFile: URL = getClass.getResource("/training_lr.txt")

    val sparkConf = new SparkConf()
    sparkConf.setAppName("test")
    sparkConf.setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    val sqlContext = new SQLContext(sc)

    val training = sqlContext.read.format("libsvm")
      .load(trainingFile.getPath)

    training.printSchema()

    training.show(10)

    /*    val dataFrame = sqlContext.createDataFrame(Seq(
      (0.1, 1.3),
      (0.2, 1.4))).toDF("label", "features")

        val lr = new LinearRegression()
        .setMaxIter(10)
        .setRegParam(0.3)
        .setElasticNetParam(0.8)
          .setLabelCol("label")
          .setFeaturesCol("features")
          .setPredictionCol("PPP")*/

    /*
        val lrModel = lr.fit(dataFrame)

        lrModel.summary*/

  }

}
