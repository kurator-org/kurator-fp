package org.kurator;

import akka.actor.*;
import org.kurator.actors.KafkaConsumerActor;
import org.kurator.actors.KafkaProducerActor;
import org.kurator.actors.WorkerActor;
import org.kurator.messages.*;
import org.kurator.workers.ReverseString;
import org.kurator.workers.Worker;
import scala.concurrent.duration.Duration;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

/**
 * Created by lowery on 7/25/16.
 */
public class Tester {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("kurator");

        final ActorRef consumer = system.actorOf(Props.create(KafkaConsumerActor.class, "dwca-topic", "dwca-consumer-group"));
        final ActorRef producer = system.actorOf(Props.create(KafkaProducerActor.class, "dwca-topic"));
        final ActorRef worker = system.actorOf(Props.create(WorkerActor.class, new ReverseString()));

        final ActorRef simple = system.actorOf(Props.create(SimpleTesterActor.class, producer, consumer));
        final ActorRef slowstep = system.actorOf(Props.create(SlowStepTesterActor.class, producer, consumer, 10, 100));
        final ActorRef workflow = system.actorOf(Props.create(WorkerTesterActor.class, worker));

        workflow.tell(new Start(), null);

        //Cancellable produceMessages = system.scheduler().schedule(Duration.Zero(),
        //        Duration.create(10, TimeUnit.MILLISECONDS), producer, new PublishData("test-" + count++), system.dispatcher(), ActorRef.noSender());

        //Cancellable consumeMessages = system.scheduler().schedule(Duration.Zero(),
                //Duration.create(1, TimeUnit.MILLISECONDS), consumer, new RequestMoreData(), system.dispatcher(), ActorRef.noSender());
    }
}

class SlowStepTesterActor extends UntypedActor {
    private final ActorSystem system = getContext().system();

    private final ActorRef producer;
    private final ActorRef consumer;

    private long messageId = 0;

    private long producerCount = 0;
    private long consumerCount = 0;

    private int consumerTaskDuration;
    private int producerTaskDuration;

    public SlowStepTesterActor(ActorRef producer, ActorRef consumer, int producerTaskDuration, int consumerTaskDuration) {
        this.producer = producer;
        this.consumer = consumer;

        this.producerTaskDuration = producerTaskDuration;
        this.consumerTaskDuration = consumerTaskDuration;
    }

    public void onReceive(Object message) throws Throwable {
        // Start the consumer and producer. Tell self to start producing and consuming messages
        if (message instanceof Start) {
            producer.tell(new Start(), self());
            consumer.tell(new Start(), self());

            self().tell(new Produce(), self());
            self().tell(new Consume(), self());
        }

        // Consume a message
        else if (message instanceof Consume) {
            // Simulate a processing step that introduces a delay
            system.scheduler().scheduleOnce(Duration.create(consumerTaskDuration, TimeUnit.MILLISECONDS),
                    consumer, new RequestMoreData(), system.dispatcher(), self());
        }

        // Process the consumer response and tell self to consume the next message
        else if (message instanceof ReceivedMoreData) {
            consumerCount++;

            if (consumerCount % 1000 == 0) {
                System.out.println("Consumed " + consumerCount + " records");
            }

            self().tell(new Consume(), self());
        }

        // Retry until a message is received. The consumer might be polling for more records
        else if (message instanceof ConsumerDrained) {
            consumer.tell(new RequestMoreData(), self());
        }

        // Produce a message
        else if (message instanceof Produce) {
            // Simulate a pre-processing step that introduces a delay
            system.scheduler().scheduleOnce(Duration.create(producerTaskDuration, TimeUnit.MILLISECONDS),
                    producer, new PublishData("test-" + messageId++), system.dispatcher(), self());
        }

        // If the producer was successful tell self to produce the next message
        else if (message instanceof KafkaProducerActor.SendSuccessful) {
            producerCount++;

            if (producerCount % 1000 == 0) {
                System.out.println("Produced " + producerCount + " records");
            }

            self().tell(new Produce(), self());
        }
    }

    class Produce {}
    class Consume {}
}

class SimpleTesterActor extends UntypedActor {

    private final ActorRef producer;
    private final ActorRef consumer;

    private long messageId = 0;

    private long producerCount = 0;
    private long consumerCount = 0;

    public SimpleTesterActor(ActorRef producer, ActorRef consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    public void onReceive(Object message) throws Throwable {
        // Start the consumer and producer. Tell self to start producing and consuming messages
        if (message instanceof Start) {
            producer.tell(new Start(), self());
            consumer.tell(new Start(), self());

            self().tell(new Produce(), self());
            self().tell(new Consume(), self());
        }

        // Consume a message
        else if (message instanceof Consume) {
            consumer.tell(new RequestMoreData(), self());
        }

        // Process the consumer response and tell self to consume the next message
        else if (message instanceof ReceivedMoreData) {
            consumerCount++;

            if (consumerCount % 1000 == 0) {
                System.out.println("Consumed " + consumerCount + " records");
            }

            self().tell(new Consume(), self());
        }

        // Retry until a message is received. The consumer might be polling for more records
        else if (message instanceof ConsumerDrained) {
            consumer.tell(new RequestMoreData(), self());
        }

        // Produce a message
        else if (message instanceof Produce) {
            producer.tell(new PublishData("test-" + messageId++), self());
        }

        // If the producer was successful tell self to produce the next message
        else if (message instanceof KafkaProducerActor.SendSuccessful) {
            producerCount++;

            if (producerCount % 1000 == 0) {
                System.out.println("Produced " + producerCount + " records");
            }

            self().tell(new Produce(), self());
        }
    }

    class Produce {}
    class Consume {}
}

class WorkerTesterActor extends UntypedActor {

    private final ActorRef producer;
    private final ActorRef consumer;

    private final ActorRef worker;

    private long messageId = 0;

    private long producerCount = 0;
    private long consumerCount = 0;

    public WorkerTesterActor(ActorRef worker) {
        this.worker = worker;

        this.producer = getContext().actorOf(Props.create(KafkaProducerActor.class, "test-topic"));
        this.consumer = getContext().actorOf(Props.create(KafkaConsumerActor.class, "test-topic", "test-consumer-group"));
    }

    public void onReceive(Object message) throws Throwable {
        // Start the consumer and producer. Tell self to start producing and consuming messages
        if (message instanceof Start) {
            producer.tell(new Start(), self());
            consumer.tell(new Start(), self());

            self().tell(new Produce(), self());
            self().tell(new Consume(), self());
        }

        // Consume a message
        else if (message instanceof Consume) {
            consumer.tell(new RequestMoreData(), self());
        }

        // Process the consumer response and invoke the child worker instance
        else if (message instanceof ReceivedMoreData) {
            consumerCount++;

            if (consumerCount % 1000 == 0) {
                System.out.println("Consumed " + consumerCount + " records");
            }

            worker.tell(((ReceivedMoreData) message).data(), self());
        }

        // When the worker completes tell self to consume the next record
        else if (message instanceof WorkComplete) {
            if (messageId % 1000 == 0) {
                System.out.println("Worker processed " + messageId + " messages. Latest result: " + ((WorkComplete) message).result());
            }

            self().tell(new Consume(), self());
        }

        // Retry until a message is received. The consumer might be polling for more records
        else if (message instanceof ConsumerDrained) {
            consumer.tell(new RequestMoreData(), self());
        }

        // Produce a message
        else if (message instanceof Produce) {
            producer.tell(new PublishData("test-" + messageId++), self());
        }

        // If the producer was successful tell self to produce the next message
        else if (message instanceof KafkaProducerActor.SendSuccessful) {
            producerCount++;

            if (producerCount % 1000 == 0) {
                System.out.println("Produced " + producerCount + " records");
            }

            self().tell(new Produce(), self());
        }
    }

    class Produce {}
    class Consume {}
}