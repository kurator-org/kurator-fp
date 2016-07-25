package org.kurator.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.apache.axis.utils.ArrayUtil;
import org.apache.axis.utils.ByteArray;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.kurator.GeoLocateRequest;
import org.kurator.messages.MoreData;
import org.kurator.messages.WorkComplete;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/20/16.
 */
public class KafkaConsumerActor<T> extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private static final long CONSUMER_TIMEOUT = 1000; // Consumer timeout in ms

    private KafkaConsumer<String, String> consumer;
    private String groupId;

    private String topic;

    private Queue<String> buffer = new LinkedBlockingQueue<>();

    public KafkaConsumerActor(String topic, String groupId) {
        this.topic = topic;
        this.groupId = groupId;

        // Load the kafka consumer properties via typesafe config and construct the properties argument
        Config config = ConfigFactory.load().getConfig("kafka.consumer");
        Properties props = new Properties();

        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            props.put(entry.getKey(), entry.getValue().unwrapped());
        }

        // Add the kafka consumer group id
        props.put("group.id", groupId);

        // Create the kafka consumer instance and subscribe to this actor's topic
        try {
            consumer = new KafkaConsumer<String, String>(props);
        } catch (Exception e) {
            e.printStackTrace();
        }

        consumer.subscribe(Collections.singletonList(topic));

        logger.debug("Created kafka consumer with group id " + groupId + ". Subscribed to topic " + topic);
    }

    public void preStart() throws Exception {

    }

    public void onReceive(Object message) throws Throwable {
        // TODO: Implement this logic as a FSM where the consumer is either busy polling for more data or ready to send
        if (message instanceof MoreData) {
            if (buffer.isEmpty()) { // poll the kafka consumer and fill the buffer
                poll();
                self().tell(new MoreData(), sender());
            } else {
                // send the next message in the buffer
                String response = buffer.remove();
                sender().tell(response, self());
            }
        }
    }

    public void poll() throws IOException {
        ConsumerRecords<String, String> records = consumer.poll(CONSUMER_TIMEOUT);

        for (ConsumerRecord<String, String> record : records) {
            buffer.add(record.value());
            //logger.debug("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());
        }
    }
}
