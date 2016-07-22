package org.kurator.actors.io;

import akka.actor.UntypedActor;

/**
 * Created by lowery on 7/21/16.
 */
@Reader(types = {"text/json"})
public class JsonReader extends AbstractReader {
    protected void read() {

    }
}
