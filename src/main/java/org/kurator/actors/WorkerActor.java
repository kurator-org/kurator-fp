package org.kurator.actors;

import akka.actor.UntypedActor;

import org.kurator.messages.WorkComplete;
import org.kurator.workers.Worker;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.Callable;
import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/20/16.
 */
public class WorkerActor extends UntypedActor {
    private final Worker worker;
    private ExecutionContext ec = getContext().system().dispatcher();

    public WorkerActor(Worker worker) {
        this.worker = worker;
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {
            Future<WorkComplete> future = future(worker.callable((String) message), ec);

            pipe(future, ec).to(sender(), self());
        }
    }
}