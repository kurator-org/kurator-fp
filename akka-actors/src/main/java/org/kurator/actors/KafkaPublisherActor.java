package org.kurator.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.kurator.messages.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/26/16.
 */
public class KafkaPublisherActor extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    private ExecutionContext dispatcher = getContext().system().dispatcher();
    private ActorRef producer;

    private long messageId = 0;

    public KafkaPublisherActor(ActorRef producer) {
        this.producer = producer;
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof Start) {
            producer.tell(new PublishData("test-" + messageId++), self());
        }

        // If the producer was successful tell self to produce the next message
        else if (message instanceof KafkaProducerActor.SendSuccessful) {
            messageId++;

            if (messageId % 1000 == 0) {
                System.out.println("Produced " + messageId + " records");
            }

            producer.tell(new PublishData("test-" + messageId++), self());
        }
    }
}
