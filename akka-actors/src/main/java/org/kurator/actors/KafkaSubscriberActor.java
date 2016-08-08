package org.kurator.actors;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.requests.SyncGroupRequest;
import org.kurator.messages.*;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/26/16.
 */
public class KafkaSubscriberActor extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);
    private ExecutionContext dispatcher = getContext().system().dispatcher();
    private ActorRef consumer;

    private long count = 0;
    private Future<WorkComplete> future;

    public KafkaSubscriberActor(ActorRef consumer) {
        this.consumer = consumer;
    }

    public void preStart() throws Exception {
        logger.debug("Registering a subscription to the consumer.");
        consumer.tell(new RegisterListener(), self());
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof HasMoreData) {

            logger.debug("Consumer has more data." );
            sender().tell(new RequestMoreData(), self());

        }

        else if (message instanceof ConsumerDrained) {
            // wait for has more data
        }

        else if (message instanceof ReceivedMoreData) {

            // do work
            final String data = ((ReceivedMoreData) message).data();

            future = future(callable(data), dispatcher);
            pipe(future, dispatcher).to(self(), sender());

        }

        else if (message instanceof WorkComplete) {

            count++;
            if (count % 1000 == 0) {
                System.out.println(self() + " - Processed " + count + " records. Last record: " + ((WorkComplete) message).result());
            }

            sender().tell(new RequestMoreData(), self());

        }
    }

    public Callable<WorkComplete> callable(final String data) {
        return () -> {
            StringBuffer reverse = new StringBuffer();
            for (int i = data.length(); i > 0; i--) {
                reverse.append(data.charAt(i-1));
            }

            return new WorkComplete(reverse.toString());
        };
    }
}
