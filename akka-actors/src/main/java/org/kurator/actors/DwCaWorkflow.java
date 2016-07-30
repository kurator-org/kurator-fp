package org.kurator.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.kurator.actors.io.DwCaExtractor;
import org.kurator.messages.Curate;
import org.kurator.messages.ExtractDwcArchive;
import org.kurator.messages.Validate;

import java.io.File;

/**
 * Created by lowery on 7/24/16.
 */
public class DwCaWorkflow extends UntypedActor {
    private ActorRef producer;
    private ActorRef consumer;

    public DwCaWorkflow() {
        ActorSystem system = getContext().system();

        producer = system.actorOf(Props.create(DwCaExtractor.class), "reader");
        consumer = system.actorOf(Props.create(GEORefValidator.class), "validator");
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof Curate) {
            File archive = ((Curate) message).file();

            producer.tell(new ExtractDwcArchive(archive), self());

            consumer.tell(new Validate(), self());
        }
    }
}
