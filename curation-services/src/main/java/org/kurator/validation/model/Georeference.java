package org.kurator.validation.model;

import org.filteredpush.kuration.util.CurationStatus;
import org.kurator.validation.CurationEvent;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by lowery on 7/30/16.
 */
public class Georeference {

    private List<CurationEvent> curationEvents = new LinkedList<>();

    // Current state

    private Map<String, Object> currentValues;
    private CurationStatus curationStatus;

    public Georeference(final String country, final String stateProvince, final String county, final String locality,
                        final String latitude, final String longitude) {

        CurationStatus initialStatus = CurationStatus.INITIALIZED;

        Map<String, Object> initialValues = new HashMap<>();

        initialValues.put("country", country);
        initialValues.put("stateProvince", stateProvince);
        initialValues.put("county", county);
        initialValues.put("locality", locality);

        initialValues.put("latitude", latitude);
        initialValues.put("longitude", longitude);

        CurationEvent initialEvent = new CurationEvent(
                "Georeference initial values.",
                initialStatus,
                initialValues
        );

        // Initial event
        curationEvents.add(initialEvent);

        // Initial state
        currentValues = initialValues;
        curationStatus = initialStatus;
    }

    public void apply(CurationEvent event) {

        // "Persist" event
        curationEvents.add(event);

        // Update state
        currentValues.putAll(event.getUpdates());
        curationStatus = event.getStatus();

    }

    public void apply(String comment) {
        apply(new CurationEvent(comment));
    }

    public void apply(CurationStatus status, String comment) {
        apply(new CurationEvent(comment, status));
    }

    public void apply(CurationStatus status, Map<String, Object> values, String comment) {
        apply(new CurationEvent(comment, status, values));
    }

    public Object get(String key) {
        return currentValues.get(key); // only ok for now because String is immutable
    }

    public CurationStatus status() {
        return curationStatus; // this is also only ok because it is an enum
    }

    public String country() {
        return String.valueOf(get("country"));
    }

    public String stateProvince() {
        return String.valueOf(get("stateProvince"));
    }

    public String county() {
        return String.valueOf(get("county"));
    }

    public String locality() {
        return String.valueOf(get("locality"));
    }

    public String latitude() {
        return String.valueOf(get("latitude"));
    }

    public String longitude() {
        return String.valueOf(get("longitude"));
    }
}
