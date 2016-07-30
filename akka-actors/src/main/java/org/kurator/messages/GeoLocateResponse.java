package org.kurator.messages;

import org.kurator.services.geolocate.model.Georef_Result_Set;

/**
 * Created by lowery on 7/22/16.
 */
public class GeoLocateResponse {
    private Georef_Result_Set results;

    public GeoLocateResponse(Georef_Result_Set results) {
        this.results = results;
    }

    public Georef_Result_Set get() {
        return results;
    }
}
