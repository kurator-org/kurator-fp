package org.kurator.actors;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.kurator.messages.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lowery on 7/20/16.
 */
public class KafkaConsumerActor<T> extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    private ExecutionContext dispatcher = getContext().system().dispatcher();

    private static final long CONSUMER_TIMEOUT = 1000; // Consumer timeout in ms

    private Cancellable pollingScheduler;

    private KafkaConsumer<String, String> consumer;
    private String groupId;

    private String topic;

    private Queue<String> buffer = new LinkedList<>();
    private List<ActorRef> listeners = new LinkedList<>();

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
    }

    public void onReceive(Object message) throws Throwable {
        // TODO: Implement this logic as a FSM where the consumer is either busy polling for more data or ready to send

        if (message instanceof Start) {

            // Start polling periodically for incoming messages
            pollingScheduler = getContext().system().scheduler().schedule(Duration.Zero(),
                    Duration.create(1000, TimeUnit.MILLISECONDS), self(), new PollConsumer(), dispatcher, ActorRef.noSender());

            logger.debug("Started kafka consumer with group id: {}. Subscribed to topic: {}", groupId, topic);

        } else if (message instanceof PollConsumer) {

            if (buffer.isEmpty()) {
                logger.debug("Consumer polling for messages...");

                ConsumerRecords<String, String> records = consumer.poll(CONSUMER_TIMEOUT);

                for (ConsumerRecord<String, String> record : records) {
                    buffer.add(record.value());
                }

                logger.debug("Consumed {} messages from topic {} to buffer", records.count(), topic);

                // TODO: Handle failure

                // alert listeners that may be idle that the consumer has more data
                if (!buffer.isEmpty()) {
                    for (ActorRef listener : listeners) {
                        listener.tell(new HasMoreData(), self());
                    }
                }
            }

        } else if (message instanceof RequestMoreData) {

            if (buffer.isEmpty()) {
                sender().tell(new ConsumerDrained(), self());
            } else {
                // send the next message in the buffer
                ReceivedMoreData response = new ReceivedMoreData(buffer.remove());
                sender().tell(response, self());
            }

        } else if (message instanceof RegisterListener) {

            final ActorRef listener = sender();
            listeners.add(listener);
            logger.debug("registered listener {}", listener);

        } else if (message instanceof Stop) {

            logger.debug("Stopping kafka consumer with group id: {}", groupId, topic);
            // stop polling and terminate this actor
            pollingScheduler.cancel();
            context().stop(self());

        }
    }
}
