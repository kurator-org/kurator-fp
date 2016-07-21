package org.kurator.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.kurator.messages.MoreData;
import org.kurator.messages.MoreWork;
import org.kurator.messages.Start;
import org.kurator.messages.WorkComplete;

/**
 * Created by lowery on 7/20/16.
 */
public class MasterActor extends UntypedActor {
    private ActorRef source;
    private ActorRef sink;
    //private ActorRef worker;

    public MasterActor(ActorRef source, ActorRef sink) {
        this.source = source;
        this.sink = sink;

        //worker = getContext().actorOf(Props.create(WorkerActor.class), "worker");
    }

    public void onReceive(Object message) throws Throwable {
        /*if (message instanceof String) {
            sink.tell((String) message, self());
        }*/
        if (message instanceof MoreData) {
            source.tell(new MoreData(), self());
        } else if (message instanceof String) {
            System.out.println(message);
            //source.tell(new MoreData(), self());
        }
        //if (message instanceof Start) {
            //source.tell(new MoreData(), self()); // Ask the source for a new record
        //} else if (message instanceof String) {
            //System.out.println(message);
            //source.tell(new MoreData(), self());
        //}
    }
}
