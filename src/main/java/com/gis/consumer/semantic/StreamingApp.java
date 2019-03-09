package com.gis.consumer.semantic;

import com.gis.consumer.beans.GISRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StreamingApp {
    //default values
    private static String brokers = "localhost:9092";
    private static String groupId = "gis-streaming-semantic";
    private static String topic = "gis-uk-crime-demo";

    public static void main(String[] args) {
        if (args.length == 3) {
            brokers = args[0];
            groupId = args[1];
            topic = args[2];
        }

        SparkConf sparkConf = new SparkConf().setAppName(
                "GIS-Streaming-Semantic").setMaster("local[*]");

        // Create the context with 2 seconds batch size
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf,
                new Duration(2000));


        Collection<String> topics = Arrays.asList(topic);


        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", brokers);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", groupId);
        kafkaParams.put("auto.offset.reset", "earliest"); //change to latest
        kafkaParams.put("enable.auto.commit", false);

        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                );

        /*JavaPairDStream<String, String> pairDStream = stream.mapToPair(record -> new Tuple2<>(record.key(), record.value()));
        JavaDStream<String> gisRecord = pairDStream.map(new Function<Tuple2<String, String>, String>() {
            @Override
            public String call(Tuple2<String, String> tuple2) throws Exception {
                return tuple2._2();
            }
        });*/

        JavaDStream<String> gisRecord = stream.map(ConsumerRecord::value);

        //To check the offset
        /*stream.foreachRDD(new VoidFunction<JavaRDD<ConsumerRecord<String, String>>>() {
            @Override
            public void call(JavaRDD<ConsumerRecord<String, String>> rdd) throws Exception {
                OffsetRange[] offsetRanges = ((HasOffsetRanges) rdd.rdd()).offsetRanges();
                rdd.foreachPartition(consumerRecords -> {
                    OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
                    System.out.println(
                            o.topic() + " " + o.partition() + " " + o.fromOffset() + " " + o.untilOffset());
                });
            }
        });*/

        //gisRecord.print();

        gisRecord.foreachRDD(new VoidFunction<JavaRDD<String>>() {
            @Override
            public void call(JavaRDD<String> gisRecordStrRDD) throws Exception {
                SparkSession session = SparkSession.builder()
                        .config(gisRecordStrRDD.context().getConf()).getOrCreate();
                JavaRDD<GISRecord> gisRecordRDD = gisRecordStrRDD.map(new Function<String, GISRecord>() {

                    @Override
                    public GISRecord call(String gisRecordStr) throws Exception {
                        String[] gisRecordArr = gisRecordStr.split(",");
                        if (gisRecordArr.length == 11) {
                            GISRecord gisRecord = new GISRecord();
                            gisRecord.setCrimeid(gisRecordArr[0]);
                            gisRecord.setMonth(gisRecordArr[1]);
                            gisRecord.setReportedby(gisRecordArr[2]);
                            gisRecord.setFallswithin(gisRecordArr[3]);
                            gisRecord.setLongitude(gisRecordArr[4]);
                            gisRecord.setLatitude(gisRecordArr[5]);
                            gisRecord.setLocation(gisRecordArr[6]);
                            gisRecord.setLsoacode(gisRecordArr[7]);
                            gisRecord.setLsoaname(gisRecordArr[8]);
                            gisRecord.setCrimetype(gisRecordArr[9]);
                            gisRecord.setOutcomecategory(gisRecordArr[10]);
                            return gisRecord;
                        } else {
                            //handle bad data
                            System.out.println("BD: " + gisRecordStr);
                            System.out.println("length : " + gisRecordArr.length);
                            return new GISRecord();
                        }
                    }
                });
                Dataset<Row> wordsDataFrame = session.createDataFrame(gisRecordRDD, GISRecord.class);
                wordsDataFrame.createOrReplaceTempView("gisRecord");
                //wordsDataFrame.write().mode(SaveMode.Append).saveAsTable("gisRecord");
                session.sql("select location, count(*) " +
                        "from gisRecord " +
                        "group by location").show();
                // persist
                // rowRDD.saveAsTextFile("file:///" + outputDir);

            }
        });
        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
            jssc.close();
        }
    }
}
