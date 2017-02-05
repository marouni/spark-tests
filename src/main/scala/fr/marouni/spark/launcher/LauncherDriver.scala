package fr.marouni.spark.launcher

import org.apache.spark.launcher.SparkLauncher

/**
  * Created by abbass on 13/11/16.
  */
object LauncherDriver extends App{

  val spark = new SparkLauncher()
    .setSparkHome("/home/abbass/dev/spark/spark-1.6.0-bin-hadoop2.6")
    .setAppResource("/home/abbass/dev/spark/spark-1.6.0-bin-hadoop2.6/lib/spark-examples-1.6.0-hadoop2.6.0.jar")
    .setMainClass("org.apache.spark.examples.SparkPi")
    .setMaster("local[*]")
    .launch();
  spark.waitFor();

}
