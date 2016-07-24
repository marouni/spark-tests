package fr.marouni.spark.skew

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by abbass on 19/06/16.
  */
object Driver extends App{

  override def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
    val sc = new SparkContext("local[*]", "SQL tests", sparkConf)

    val num_parts = 16
    val largeRDD: RDD[(Int, Int)] = sc.parallelize(0 until (num_parts), num_parts).flatMap({
      x =>
        val range: Range = 0 until (Math.exp(x.toDouble).toInt)
        range.map({
          y => (x, y)
        })
    })

    val smallRDD: RDD[(Int, Int)] = sc.parallelize(0 until (num_parts), num_parts).map({
      x => (x, x)
    })

    //val join1: RDD[(Int, (Int, Option[Int]))] = largeRDD.leftOuterJoin(smallRDD)
    //join1.saveAsTextFile("file:///tmp/join_skewed")

    //Index  ▴	ID	Attempt	Status	Locality Level	Executor ID / Host	Launch Time	Duration	GC Time	Shuffle Read Size / Records	Errors
    //  0	32	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	66 ms		984.0 B / 2
    //  1	33	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	74 ms		999.0 B / 3
    //  2	34	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	70 ms		1027.0 B / 8
    //  3	35	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	90 ms		1094.0 B / 21
    //  4	36	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	31 ms		1270.0 B / 55
    //  5	37	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	47 ms		1742.0 B / 149
    //  6	38	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	0.1 s		3.0 KB / 404
    //  7	39	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	0.2 s		6.4 KB / 1097
    //  8	40	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	0.2 s		15.7 KB / 2981
    //  9	41	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	0.3 s		41.2 KB / 8104
    //  10	42	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:20	0.4 s		110.2 KB / 22027
    //  11	43	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:21	0.6 s		297.9 KB / 59875
    //  12	44	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:21	2 s	53 ms	808.2 KB / 162755
    //  13	45	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:21	2 s	0.1 s	2.1 MB / 442414
    //  14	46	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:21	3 s	0.2 s	5.8 MB / 1202605
    //  15	47	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:41:21	5 s	0.9 s	15.8 MB / 3269018
    // 	Time : 11 s


    // Optimization

    val N = 100
    val transformedSmallRDD: RDD[((Int, Int), Int)] = smallRDD.cartesian(sc.parallelize(0 until (N))).map({
      x => ((x._1._1, x._2), x._1._2)
    }).coalesce(num_parts)

    val transformedLargeRDD: RDD[((Int, Int), Int)] = largeRDD.map({
      val r = scala.util.Random
      x => ((x._1, r.nextInt(N)), x._2)
    })

    val join: RDD[((Int, Int), (Int, Option[Int]))] = transformedLargeRDD.leftOuterJoin(transformedSmallRDD)

    join.map({
      x => (x._1._1, x._2)
    }).saveAsTextFile("/tmp/join_no_skew")

    // 7	39	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:45	0.8 s	40 ms	1071.2 KB / 169757
    // 13	45	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:47	0.9 s	14 ms	1244.8 KB / 195722
    // 9	41	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:46	0.9 s	41 ms	1245.7 KB / 196253
    // 4	36	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:44	1 s	84 ms	1401.5 KB / 219827
    // 3	35	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:41	3 s	41 ms	1478.6 KB / 231703
    // 8	40	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:45	1 s	68 ms	1594.8 KB / 249441
    // 10	42	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:46	1 s	41 ms	1597.0 KB / 249849
    // 14	46	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:47	1 s	40 ms	1858.0 KB / 289543
    // 12	44	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:46	2 s	53 ms	2.2 MB / 342101
    // 6	38	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:45	2 s	96 ms	2.3 MB / 372173
    // 1	33	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:41	3 s	69 ms	2.5 MB / 389982
    // 5	37	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:44	2 s	96 ms	2.5 MB / 393164
    // 15	47	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:48	1 s	48 ms	2.6 MB / 414252
    // 2	34	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:41	3 s	97 ms	2.7 MB / 431640
    // 11	43	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:46	2 s	75 ms	3.2 MB / 506045
    // 0	32	0	SUCCESS	PROCESS_LOCAL	driver / localhost	2016/06/19 19:44:41	4 s	0.1 s	3.3 MB / 521650



    Thread.sleep(100000000)
  }



}
