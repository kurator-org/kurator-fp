package org.kurator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by lowery on 7/22/16.
 */
public class GeoLocateRequest {
    private String country;
    private String state;
    private String county;
    private String locality;

    private boolean findWaterBody = false;
    private boolean hwyx = false;
    private boolean restrictToLowestAdm = true;
    private boolean doUncert = true;  // include uncertainty radius in results
    private boolean doPoly = false;   // include error polygon in results
    private boolean displacePoly = false;  // displace error polygon in results
    private boolean polyAsLinkID = false;
    private int languageKey = 0;

    public GeoLocateRequest(String country, String state, String county, String locality) {
        this.country = country;
        this.state = state;
        this.county = county;
        this.locality = locality;
    }

    public String getCountry() {
        return country;
    }

    public String getState() {
        return state;
    }

    public String getCounty() {
        return county;
    }

    public String getLocality() {
        return locality;
    }

    public boolean isRestrictToLowestAdm() {
        return restrictToLowestAdm;
    }

    public boolean isDoUncert() {
        return doUncert;
    }

    public boolean isDoPoly() {
        return doPoly;
    }

    public boolean isDisplacePoly() {
        return displacePoly;
    }

    public boolean isPolyAsLinkID() {
        return polyAsLinkID;
    }

    public String constructQueryString(String country, String state, String county, String locality) {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("?Country=")
                    .append(URLEncoder.encode(country, "UTF-8"))
                    .append("&State=").append(URLEncoder.encode(state, "UTF-8"));
            if (county != null) {
                sb.append("&County=").append(URLEncoder.encode(county, "UTF-8"));
            }

            sb.append("&LocalityString=").append(URLEncoder.encode(locality, "UTF-8"))
                    .append("&FindWaterbody=").append(findWaterBody)
                    .append("&HwyX=").append(hwyx)
                    .append("&RestrictToLowestAdm=").append(restrictToLowestAdm)
                    .append("&doUncert=").append(doUncert)
                    .append("&doPoly=").append(doPoly)
                    .append("&displacePoly=").append(displacePoly)
                    .append("&polyAsLinkID=").append(polyAsLinkID)
                    .append("&LanguageKey=").append(languageKey);
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            // Not thrown unless args to URLEncoder are invalid
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "GeoLocateRequest{" +
                "country='" + country + '\'' +
                ", state='" + state + '\'' +
                ", county='" + county + '\'' +
                ", locality='" + locality + '\'' +
                '}';
    }
}
