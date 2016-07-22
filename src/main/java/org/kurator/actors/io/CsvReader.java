package org.kurator.actors.io;

import akka.actor.UntypedActor;

/**
 * Created by lowery on 7/21/16.
 */
@Reader(types = {"text/csv", "text/plain"})
public class CsvReader extends AbstractReader {

    @Override
    protected void read() {

    }
}
