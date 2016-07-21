package org.kurator.messages;

import org.kurator.actors.WorkerActor;

/**
 * Created by lowery on 7/20/16.
 */
public class WorkComplete {
    private String result;

    public WorkComplete(String result) {
        this.result = result;
    }
}
