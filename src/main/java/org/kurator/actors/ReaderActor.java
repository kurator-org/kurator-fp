package org.kurator.actors;

import akka.actor.UntypedActor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.kurator.messages.MoreData;

/**
 * Created by lowery on 7/21/16.
 */
public class ReaderActor extends UntypedActor {
    private long count = 0;

    public void onReceive(Object message) throws Throwable {
        if (message instanceof MoreData) {
            count++;

            sender().tell("test-" + count, self());

            if (count % 1000 == 0) {
                System.out.println(count + " records produced.");
            }
        } else if (message instanceof RecordMetadata) {
            self().tell(new MoreData(), sender());
        }
    }
}
