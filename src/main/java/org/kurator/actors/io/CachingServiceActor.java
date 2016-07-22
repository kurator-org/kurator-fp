package org.kurator.actors.io;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.thoughtworks.xstream.XStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kurator.GeoLocateRequest;
import org.kurator.messages.GeoLocateResponse;
import org.kurator.messages.MoreData;
import org.kurator.services.geolocate.model.Georef_Result_Set;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

import static akka.dispatch.Futures.future;
import static akka.pattern.Patterns.pipe;

/**
 * Created by lowery on 7/21/16.
 */
public class CachingServiceActor extends UntypedActor {
    final ExecutionContext ec = getContext().system().dispatchers().lookup("service-call-dispatcher");
    //final ExecutionContext ec = getContext().system().dispatcher();
    private String baseUrl = "http://www.museum.tulane.edu//webservices/geolocatesvcv2/geolocatesvc.asmx/Georef2";

    private long count = 0;

    private long futures; // count of the uncompleted futures this actor has spawned

    public void onReceive(Object message) throws Throwable {
        if (message instanceof GeoLocateRequest) {
            GeoLocateRequest request = (GeoLocateRequest) message;
            //System.out.println(request);

            String country = request.getCountry();
            String state = request.getState();
            String county = request.getCounty();
            String locality = request.getLocality();

            // TODO: Implement some form of validation instead
            if (country != null && state != null && county != null && locality != null) {
                String url = baseUrl + request.constructQueryString(request.getCountry(), request.getState(), request.getCounty(), request.getLocality());
                fetch(url); // TODO: These futures need a separate thread pool executor service
            } else {
                //System.out.println("Skipping..");
            }

            // limit number of futures running at one time
            //if (futures < 10) {
                sender().tell(new MoreData(), self());
            //}
        } if (message instanceof GeoLocateResponse) {
            System.out.println("Response: " + message);
            Georef_Result_Set result = ((GeoLocateResponse) message).get();
            if (Integer.parseInt(result.getNumResults()) > 0) System.out.println(result.getNumResults());

            count++;
            futures--;
            //if (count % 1000 == 0)
                System.out.println(result + ": " + count + " requests processed. (" + futures + " futures)" );

            sender().tell(new MoreData(), self());
        }
    }

    private void fetch(String url) {
        //System.out.println(url);
        futures++;

        final ActorRef sender = sender();

        Future<GeoLocateResponse> future = future(new Callable<GeoLocateResponse>() {
            @Override
            public GeoLocateResponse call() throws Exception {
                HttpClient client = HttpClientBuilder.create().build();
                HttpGet request = new HttpGet(url);

                HttpResponse response = client.execute(request);

                //System.out.println("Response Code : "
                 //       + response.getStatusLine().getStatusCode());

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuffer responseXml = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    responseXml.append(line);
                }

                // parse the response
                XStream xstream = new XStream();
                xstream.alias("Georef_Result_Set", Georef_Result_Set.class);
                Georef_Result_Set result = (Georef_Result_Set) xstream.fromXML(responseXml.toString());

                return new GeoLocateResponse(result);
            }
        }, ec);

        pipe(future, ec).to(self(), sender());
    }
}
