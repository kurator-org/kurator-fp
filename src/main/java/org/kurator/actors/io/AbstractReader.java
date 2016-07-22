package org.kurator.actors.io;

import akka.actor.UntypedActor;
import org.kurator.messages.InitSignal;

import java.lang.annotation.Annotation;

/**
 * Created by lowery on 7/21/16.
 */
public abstract class AbstractReader extends UntypedActor {
    public void onReceive(Object message) throws Throwable {
        if (message instanceof InitSignal) {


            Annotation annotation = getClass().getAnnotation(Reader.class);
            String[] types = ((Reader) annotation).types();

            for (String type : types) {
                if (type.equalsIgnoreCase((String) message)) {
                    read();
                }
            }
        }
    }

    protected abstract void read();
}
