import akka.actor.*;
import org.kurator.actors.*;
import org.kurator.actors.io.*;
import org.kurator.actors.io.util.DwcArchiveExtractor;
import org.kurator.messages.ExtractDwcArchive;
import org.kurator.messages.MoreData;
import org.reflections.Reflections;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lowery on 7/20/16.
 */
public class Main {
    public static void main(String[] args) {
        //String archivePath = "/home/lowery/Downloads/dwca-mczbase-v162.23.zip";

        final ActorSystem system = ActorSystem.create("kurator");

        final ActorRef consumer = system.actorOf(Props.create(KafkaConsumerActor.class, "dwca-topic"));
        final ActorRef producer = system.actorOf(Props.create(KafkaProducerActor.class, "dwca-topic"));

        final ActorRef master = system.actorOf(Props.create(MasterActor.class, consumer, producer));
        final ActorRef printer = system.actorOf(Props.create(PrinterActor.class));
        final ActorRef reader = system.actorOf(Props.create(ReaderActor.class));

        final ActorRef test = system.actorOf(Props.create(GenericReaderActor.class));
        final ActorRef dwca = system.actorOf(Props.create(DwcArchiveExtractor.class, producer, new File("/home/lowery/Downloads/dwca-mczbase-v162.23.zip")));
        //final ActorRef reader = system.actorOf(Props.create(DwCaReader.class, archivePath), "reader");

        final ActorRef geolocate = system.actorOf(Props.create(CachingServiceActor.class), "geolocate");

        Map<String, Class<? extends UntypedActor>> props = new HashMap<>();
        props.put("text/csv", CsvReader.class);
        props.put("text/plain", CsvReader.class);
        //props.put("application/xml", XmlReader.class);
        props.put("application/zip", DwCaReader.class);
        props.put("application/json", JsonReader.class);

        Set<Class<?>> readers = new Reflections().getTypesAnnotatedWith(Reader.class);

        for (Class<?> c : readers) {
            System.out.println(c);
        }


        //test.tell("/home/lowery/Downloads/asu-data.zip", null);

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
            //producer.tell("test", reader);


        dwca.tell(new ExtractDwcArchive(), null);
        consumer.tell(new MoreData(), geolocate);
       /* Cancellable cancellable2 = system.scheduler().schedule(Duration.Zero(),
                Duration.create(10, TimeUnit.MILLISECONDS), producer, ,
                system.dispatcher(), null);*/

        //System.out.println("Done.");
        system.awaitTermination();
    }
}
