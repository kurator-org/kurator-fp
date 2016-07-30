package org.kurator.actors.io.readers;

import akka.actor.UntypedActor;
import org.kurator.messages.InitSignal;
import org.kurator.messages.ReadInput;

import java.lang.annotation.Annotation;

/**
 * Created by lowery on 7/21/16.
 */
public abstract class AbstractReader extends UntypedActor {
    private String[] types;

    public void onReceive(Object message) throws Throwable {
        if (message instanceof InitSignal) {
            Annotation annotation = getClass().getAnnotation(Reader.class);
            String[] types = ((Reader) annotation).types();
        } else if (message instanceof ReadInput) {
            for (String type : types) {
                if (type.equalsIgnoreCase((String) message)) {
                    read();
                }
            }
        }
    }

    protected abstract void read();
}
