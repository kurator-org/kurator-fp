package org.kurator.example;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.kurator.actors.KafkaConsumerActor;
import org.kurator.messages.RequestMoreData;
import org.kurator.messages.Start;
import org.kurator.messages.WorkComplete;

import java.io.FileNotFoundException;

/**
 * Created by lowery on 7/25/16.
 */
public class MessageConsumer extends UntypedActor {
    private ActorRef consumer;
    private ActorRef worker;

    private long count = 0;

    public MessageConsumer(ActorRef worker) throws FileNotFoundException {
        this.consumer = getContext().actorOf(Props.create(KafkaConsumerActor.class, "akka-topic", "test-group"), "consumer");
        this.worker = worker;
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof Start) {
            consumer.tell(new RequestMoreData(), self());
        } else if (message instanceof String) {
            count++;

            if (count % 1000 == 0)
                System.out.println(count + " messages consumed.");

            // do some work via worker with the message the consumer responded with
            worker.tell(message, self());
        } else if (message instanceof WorkComplete) {
            String result = ((WorkComplete) message).result();
            if (count % 1000 == 0)
                System.out.println("batch of work complete, last result = " + result);

            // request another record from the consumer when the worker is finished
            consumer.tell(new RequestMoreData(), self());
        }
    }
}
