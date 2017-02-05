package fr.marouni.spark.contexts

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by abbass on 13/11/16.
  */
object COntextDriver extends App {

  val sparkConf = new SparkConf()
  val sc = new SparkContext("local[*]", "SQL tests", sparkConf)
  val sc1 = new SparkContext("local[*]", "SQL tests 2", sparkConf)

}
