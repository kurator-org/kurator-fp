package org.kurator.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.network.Send;
import org.kurator.messages.MoreData;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/20/16.
 */
public class KafkaProducerActor extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private ExecutionContext ec = getContext().system().dispatcher();

    private KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaProducerActor(final String topic) {
        this.topic = topic;

        // Load the kafka producer properties via typesafe config and construct the properties argument
        Config config = ConfigFactory.load().getConfig("kafka.producer");
        Properties props = new Properties();

        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            props.put(entry.getKey(), entry.getValue().unwrapped());
        }

        // Create the kafka producer instance
        try {
            producer = new KafkaProducer<String, String>(props);
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.debug("Created kafka producer for topic " + topic);
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {
            // publish message
            Future<SendSuccessful> future = send((String) message);
            pipe(future, ec).to(sender(), self());
        } else {
            unhandled(message); // TODO: producer currently only supports messages serialized as a String
        }
    }

    public Future<SendSuccessful> send(final String data) throws ExecutionException, InterruptedException {
        Future<SendSuccessful> future = future(new Callable<SendSuccessful>() {
            public SendSuccessful call() throws Exception {
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, data);
                RecordMetadata metadata = producer.send(record).get(); // TODO: callbacks for error and success
                return new SendSuccessful(metadata);
            }
        }, ec);

        return future;
    }

    final public class SendSuccessful {
        // sent message has been acknowledged by the kafka message broker

        private RecordMetadata metadata;

        public SendSuccessful(RecordMetadata metadata) {
            this.metadata = metadata;
        }

        public RecordMetadata metadata() {
            return metadata;
        }
    }
}