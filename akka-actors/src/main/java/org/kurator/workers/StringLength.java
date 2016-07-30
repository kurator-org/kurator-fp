package org.kurator.workers;

import org.kurator.messages.WorkComplete;
import scala.Int;

import java.util.concurrent.Callable;

/**
 * Created by lowery on 7/25/16.
 */
public class StringLength implements Worker {

    public Callable<WorkComplete> callable(String data) {
        return () -> {
            return new WorkComplete(Integer.toString(data.length()));
        };
    }
}
