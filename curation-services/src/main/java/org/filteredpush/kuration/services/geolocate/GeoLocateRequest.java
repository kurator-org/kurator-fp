package org.filteredpush.kuration.services.geolocate;

/**
 * Created by lowery on 7/22/16.
 */
public class GeoLocateRequest {

    private final String encoding = "UTF-8";

    // Constants

    final boolean restrictToLowestAdm = true;
    final boolean doUncert = true;  // include uncertainty radius in results
    final boolean doPoly = false;   // include error polygon in results
    final boolean displacePoly = false;  // displace error polygon in results
    final boolean polyAsLinkID = false;
    final int languageKey = 0;

    // Request params

    private String country;
    private String stateProvince;
    private String county;
    private String locality;

    private boolean findWaterBody = false;
    private boolean hwyx = false;

    public GeoLocateRequest(String country, String stateProvince, String county, String locality, boolean findWaterBody, boolean hwyx) {

        this.country = country;
        this.stateProvince = stateProvince;
        this.county = county;
        this.locality = locality;

        this.findWaterBody = findWaterBody;
        this.hwyx = hwyx;

    }

    public String country() {
        return country;
    }

    public String stateProvince() {
        return stateProvince;
    }

    public String county() {
        return county;
    }

    public String locality() {
        return locality;
    }

    public boolean findWaterBody() {
        return findWaterBody;
    }

    public boolean hwyx() {
        return hwyx();
    }

}
