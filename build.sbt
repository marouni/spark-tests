name := "Spark-tests"

version := "1.0"

scalaVersion := "2.11.7"

val sparkVersion = "2.3.0"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-hive" % sparkVersion

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies += "com.databricks" %% "spark-avro" % "4.0.0"

libraryDependencies += "com.databricks" %% "spark-avro" % "4.0.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.2.0"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.2.0"

libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.2.0"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.0"


