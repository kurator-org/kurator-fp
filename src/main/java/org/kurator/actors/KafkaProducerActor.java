package org.kurator.actors;

import akka.actor.UntypedActor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.kurator.messages.MoreData;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/20/16.
 */
public class KafkaProducerActor extends UntypedActor {
    private ExecutionContext ec = getContext().system().dispatcher();

    private Properties props = new Properties();
    private KafkaProducer<String, String> producer;

    private String topic;

    public KafkaProducerActor(String topic) {
        this.topic = topic;

        props.put("bootstrap.servers", "localhost:9092");

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(props);
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {
            try {
                send((String) message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send(final String data) throws ExecutionException, InterruptedException {
        Future<RecordMetadata> future = future(new Callable<RecordMetadata>() {
            public RecordMetadata call() throws Exception {
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, data);
                RecordMetadata metadata = producer.send(record).get(); // TODO: callbacks for error and success
                return metadata;
            }
        }, ec);

        pipe(future, ec).to(sender(), self());
    }

}
