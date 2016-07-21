package org.kurator.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.kurator.messages.MoreData;
import org.kurator.messages.MoreWork;
import org.kurator.messages.WorkComplete;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.Stack;
import java.util.concurrent.Callable;
import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/20/16.
 */
public class WorkerActor extends UntypedActor {
    private ExecutionContext ec = getContext().system().dispatcher();

    public void onReceive(Object message) throws Throwable {
        if (message instanceof String) {
            final String data = (String) message;

            Future<WorkComplete> future = future(new DoWork(data), ec);
            pipe(future, ec).to(self());
        } if (message instanceof WorkComplete) {
            sender().tell(message, self());
        }
    }
}

class DoWork implements Callable<WorkComplete> {
    private String data;

    public DoWork(String data) {
        this.data = data;
    }

    public WorkComplete call() {
        // return a String after completing some work
        StringBuffer reverse = new StringBuffer();
        for (int i = data.length(); i > 0; i--) {
            reverse.append(data.charAt(i+1));
        }

        WorkComplete result = new WorkComplete(reverse.toString());
        return result;
    }
}