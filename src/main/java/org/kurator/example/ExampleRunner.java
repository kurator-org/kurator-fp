package org.kurator.example;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.kurator.actors.KafkaConsumerActor;
import org.kurator.actors.KafkaProducerActor;
import org.kurator.actors.WorkerActor;
import org.kurator.messages.Start;
import org.kurator.messages.WorkComplete;
import org.kurator.workers.ReverseString;
import org.kurator.workers.StringLength;
import org.kurator.workers.Worker;

/**
 * Created by lowery on 7/25/16.
 */
public class ExampleRunner {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("example");

        final ActorRef producer = system.actorOf(Props.create(MessageProducer.class));
        final ActorRef worker = system.actorOf(Props.create(WorkerActor.class, new ReverseString()));
        final ActorRef test = system.actorOf(Props.create(TestActor.class));
        final ActorRef consumer = system.actorOf(Props.create(MessageConsumer.class, worker));

        producer.tell(new Start(), null);
        consumer.tell(new Start(), null);

        //worker.tell("example", test);
    }
}

class TestActor extends UntypedActor {
    public void onReceive(Object message) throws Throwable {
        if (message instanceof WorkComplete) {
            String result = ((WorkComplete) message).result();
            System.out.println(result);
        }
    }
}
