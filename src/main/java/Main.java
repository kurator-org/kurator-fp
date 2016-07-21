import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.actor.Props;
import org.kurator.actors.*;
import org.kurator.messages.MoreData;
import org.kurator.messages.Start;
import scala.concurrent.duration.Duration;
import scala.sys.Prop;

import java.util.concurrent.TimeUnit;

/**
 * Created by lowery on 7/20/16.
 */
public class Main {
    public static void main(String[] args) {
        //String archivePath = "/home/lowery/Downloads/dwca-mczbase-v162.23.zip";

        final ActorSystem system = ActorSystem.create("kurator");

        final ActorRef consumer = system.actorOf(Props.create(KafkaConsumerActor.class, "akka-topic"));
        final ActorRef producer = system.actorOf(Props.create(KafkaProducerActor.class, "akka-topic"));

        final ActorRef master = system.actorOf(Props.create(MasterActor.class, consumer, producer));
        final ActorRef printer = system.actorOf(Props.create(PrinterActor.class));
        final ActorRef reader = system.actorOf(Props.create(ReaderActor.class));
        //final ActorRef reader = system.actorOf(Props.create(DwCaReader.class, archivePath), "reader");

        //consumer.tell(new Curate(), null);

        //for (int i = 3; i < 1000; i++) {
        //    master.tell("test-" + i, null);
        //}

        //consumer.tell(new Start(), null);

        //Cancellable slowStep = system.scheduler().schedule(Duration.Zero(),
        //        Duration.create(10, TimeUnit.MILLISECONDS), consumer, new MoreData(),
        //        system.dispatcher(), printer);

        /* fast step
        for (int i = 0; i < 1000000; i++) {
            producer.tell("test-" + i, null);
        }*/
            producer.tell("test", reader);
            consumer.tell(new MoreData(), printer);

       /* Cancellable cancellable2 = system.scheduler().schedule(Duration.Zero(),
                Duration.create(10, TimeUnit.MILLISECONDS), producer, ,
                system.dispatcher(), null);*/

        System.out.println("Done.");
        system.awaitTermination();
    }
}
