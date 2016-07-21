package org.kurator.actors;

import akka.actor.UntypedActor;
import org.kurator.messages.MoreData;

/**
 * Created by lowery on 7/21/16.
 */
public class PrinterActor extends UntypedActor {
    private int count = 0;

    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {
            count++;
            if (count % 1000 == 0)
                System.out.println(message + ": " + count + " messages processed.");
            sender().tell(new MoreData(), self());
        }
    }
}
