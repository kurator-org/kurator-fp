package org.kurator.actors;

import akka.actor.UntypedActor;
import org.apache.axis.utils.ArrayUtil;
import org.apache.axis.utils.ByteArray;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.kurator.GeoLocateRequest;
import org.kurator.messages.MoreData;
import org.kurator.messages.Start;
import org.kurator.messages.WorkComplete;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/20/16.
 */
public class KafkaConsumerActor extends UntypedActor {
    private JSONParser parser = new JSONParser();
    private ExecutionContext ec = getContext().system().dispatcher();

    private Properties props = new Properties();
    private KafkaConsumer<String, String> consumer;

    private String topic;

    private long count;

    private Queue<String> buffer = new LinkedList<String>();

    public KafkaConsumerActor(String topic) {
        this.topic = topic;

        props.put("bootstrap.servers", "localhost:9092");

        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "test");
        props.put("max.poll.records", 10000);

        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof MoreData) {
            if (buffer.isEmpty()) {
                poll();
                self().tell(new MoreData(), sender());
            } else {
                String record = buffer.remove();

                JSONObject json = (JSONObject) parser.parse(record);
                GeoLocateRequest request = new GeoLocateRequest((String) json.get("country"),
                        (String) json.get("stateProvince"),
                        (String) json.get("county"),
                        (String) json.get("locality"));

                sender().tell(request, self());
            }

            count++;
            if (count % 1000 == 0)
                System.out.println(count + " messages received.");
        }
    }

    public void poll() throws IOException {
        ConsumerRecords<String, String> records = consumer.poll(1000);

        for (ConsumerRecord<String, String> record : records) {
            buffer.add(record.value());
            //System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());
        }
    }

}
