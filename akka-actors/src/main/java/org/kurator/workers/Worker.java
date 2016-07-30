package org.kurator.workers;

import org.kurator.messages.WorkComplete;

import java.util.concurrent.Callable;

public interface  Worker {
    public Callable<WorkComplete> callable(final String data);
}