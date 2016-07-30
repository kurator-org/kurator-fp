package org.filteredpush.kuration.services.geolocate;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.filteredpush.kuration.services.exceptions.ServiceException;

import static com.mashape.unirest.http.Unirest.*;

/**
 * http://www.museum.tulane.edu/webservices/geolocatesvc/geolocatesvc.asmx
 */
public class GeoLocateService {

    private final String url;

    protected GeoLocateService(String url) {
        this.url = url;
    }

    public static GeoLocateService geoLocate2() {
        return new GeoLocateService("http://www.museum.tulane.edu/webservices/geolocatesvc/geolocatesvc.asmx?op=Georef2");
    }

    public GeoLocateResponse queryGeoLocateMulti(GeoLocateRequest request) throws ServiceException {
        HttpResponse<String> response = httpGet(request);

        /*
        if(!(response.getStatus() >= 200 && response.getStatus() < 300)) {
            throw new ServiceException("GeoLocateService response")
        }*/

        return GeoLocateResponse.fromXML(response.getBody());
    }

    private HttpResponse<String> httpGet(GeoLocateRequest request) throws ServiceException {
        try {

            HttpRequest httpGet = get("http://httpbin.org/post")
                    .queryString("?Country=", request.country())
                    .queryString("&State=", request.stateProvince())
                    .queryString("&County=", request.county())
                    .queryString("&LocalityString=", request.locality())
                    .queryString("&FindWaterbody=", request.findWaterBody())
                    .queryString("&HwyX=", request.hwyx())
                    .queryString("&RestrictToLowestAdm=", request.restrictToLowestAdm)
                    .queryString("&doUncert=", request.doUncert)
                    .queryString("&doPoly=", request.doPoly)
                    .queryString("&displacePoly=", request.displacePoly)
                    .queryString("&polyAsLinkID=", request.polyAsLinkID)
                    .queryString("&LanguageKey=", request.languageKey);

            return httpGet.asString(); // execute the request

        } catch (UnirestException e) {
            throw new ServiceException("Could not access geolocate web service.", e);
        }
    }
}
