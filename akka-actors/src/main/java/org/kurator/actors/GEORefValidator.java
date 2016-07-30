package org.kurator.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.kurator.GeoLocateRequest;
import org.kurator.messages.RequestMoreData;
import org.kurator.messages.Validate;

import java.io.FileNotFoundException;

/**
 * Created by lowery on 7/24/16.
 */
public class GEORefValidator extends UntypedActor {
    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private static final String TOPIC = "georeference";

    private ActorRef consumer;
    private JSONParser parser;

    public GEORefValidator() throws FileNotFoundException {
        this.consumer = getContext().actorOf(Props.create(KafkaConsumerActor.class, TOPIC), "consumer");
        this.parser = new JSONParser();
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof Validate) {
            // Start requesting records from the consumer
            consumer.tell(new RequestMoreData(), self());
        } else if (message instanceof String) {
            // Parse the data coming back from the consumer and perform the validation step
            JSONObject json = (JSONObject) parser.parse((String) message);

            GeoLocateRequest request = new GeoLocateRequest((String) json.get("country"),
                    (String) json.get("stateProvince"),
                    (String) json.get("county"),
                    (String) json.get("locality"));

            // TODO: invoke an instance of CachingServiceActor service with the request here

            logger.debug(request.toString());

            // done, request another record
            consumer.tell(new RequestMoreData(), self());
        }
    }
}