package org.kurator.example;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.kurator.actors.KafkaProducerActor;
import org.kurator.messages.NextRecord;
import org.kurator.messages.Start;

/**
 * Created by lowery on 7/25/16.
 */
public class MessageProducer extends UntypedActor {
    private ActorRef producer;
    private long count = 0;

    public MessageProducer() {
        this.producer = getContext().actorOf(Props.create(KafkaProducerActor.class, "akka-topic"), "producer");
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof NextRecord || message instanceof Start) {
            // publish a message to kafka via the producer
            String record = "test-" + count;
            producer.tell(record, self());
        } if (message instanceof KafkaProducerActor.SendSuccessful) {
            count++;

            if (count % 1000 == 0)
                System.out.println(count + " messages produced.");

            // if the message broker acknowledges the sent message publish the next one
            self().tell(new NextRecord(), self());
        }
     }
}
