package fr.marouni.spark.ml.regression

import java.net.URL

import fr.marouni.spark.joins.JoinsDriver._
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, DecisionTreeRegressor, LinearRegression}
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by abbass on 12/04/16.
  */
object DTDriver extends App {

  override def main (args: Array[String]): Unit = {

    // test files
    val trainingFile: URL = getClass.getResource("/training_dt.txt")

    val sparkConf = new SparkConf()
    sparkConf.setAppName("test")
    sparkConf.setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    val sqlContext = new SQLContext(sc)

    val data = sqlContext.read.format("libsvm")
      .load(trainingFile.getPath)

    data.printSchema()
    data.show(10)

    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
      .fit(data)

    // Split the data into training and test sets (30% held out for testing)
    val Array(trainingData, testData) = data.randomSplit(Array(0.7, 0.3))

    // Train a DecisionTree model.
    val dt = new DecisionTreeRegressor()
      .setLabelCol("label")
      .setFeaturesCol("indexedFeatures")

    // Chain indexer and tree in a Pipeline
    val pipeline = new Pipeline()
      .setStages(Array(featureIndexer, dt))

    // Train model.  This also runs the indexer.
    val model = pipeline.fit(trainingData)

    // Make predictions.
    val predictions = model.transform(testData)

    // Select example rows to display.
    predictions.select("prediction", "label", "features").show(5)

    //    // Select (prediction, true label) and compute test error
    //    val evaluator = new RegressionEvaluator()
    //      .setLabelCol("label")
    //      .setPredictionCol("prediction")
    //      .setMetricName("rmse")
    //    val rmse = evaluator.evaluate(predictions)
    //    println("Root Mean Squared Error (RMSE) on test data = " + rmse)
    //
    //    val treeModel = model.stages(1).asInstanceOf[DecisionTreeRegressionModel]
    //    println("Learned regression tree model:\n" + treeModel.toDebugString)








  }

}
