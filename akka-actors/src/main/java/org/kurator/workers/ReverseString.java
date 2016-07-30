package org.kurator.workers;

import org.kurator.messages.WorkComplete;

import java.util.concurrent.Callable;

/**
 * Created by lowery on 7/24/16.
 */
public class ReverseString implements Worker {

    public Callable<WorkComplete> callable(String data) {
        return () -> {
            StringBuffer reverse = new StringBuffer();
            for (int i = data.length(); i > 0; i--) {
                reverse.append(data.charAt(i-1));
            }

            return new WorkComplete(reverse.toString());
        };
    }
}
