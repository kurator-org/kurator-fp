package org.filteredpush.kuration.services.geolocate;

import com.thoughtworks.xstream.XStream;

/**
 * Created by lowery on 7/22/16.
 */
public class GeoLocateResponse {

    private String engineVersion;
    private int numResults;
    private long executionTimems;

    private GeoRefResult[] resultSet;

    // For serializing an instance of this class as xml
    private static final XStream xstream = new XStream();

    static {
        xstream.alias("Georef_Result_Set", GeoLocateResponse.class);

        xstream.aliasField("EngineVersion", GeoLocateResponse.class, "engineVersion");
        xstream.aliasField("NumResults", GeoLocateResponse.class, "numResults");
        xstream.aliasField("ExecutionTimems", GeoLocateResponse.class, "executionTimems");
    }

    public GeoLocateResponse(String engineVersion, int numResults, long executionTimems) {
        this.engineVersion = engineVersion;
        this.numResults = numResults;
        this.executionTimems = executionTimems;
    }

    public String engineVersion() {
        return engineVersion;
    }

    public int numResults() {
        return numResults;
    }

    public long executionTimems() {
        return executionTimems;
    }

    public GeoRefResult[] resultSet() {
        return resultSet;
    }

    public static GeoLocateResponse fromXML(String xml) {
        return (GeoLocateResponse) xstream.fromXML(xml);
    }

}