package org.kurator.validation.examples;

import org.filteredpush.kuration.services.geolocate.GeoLocateAlternative;
import org.filteredpush.kuration.services.geolocate.GeoLocateResult;
import org.filteredpush.kuration.util.CurationStatus;
import org.filteredpush.kuration.util.GEOUtil;
import org.kurator.validation.model.Georeference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.filteredpush.kuration.StringUtils.comment;

/**
 * Created by lowery on 8/2/16.
 */
public class CheckAlternatives {
    Georeference georeference;

    public CheckAlternatives() {
        georeference = new Georeference("United States", "Alaska", "", "Barrow", "71.295556", "-156.766389");
    }


    public void checkAlternatives(List<GeoLocateResult> potentialMatches, double originalLat, double originalLong,
                                  String country, String stateProvince, int thresholdDistanceKm, boolean isMarine) {
        // Construct a list of alternatives
        List<GeoLocateAlternative> alternatives = GeoLocateAlternative.constructListOfAlternatives(originalLat,
                originalLong);

        Iterator<GeoLocateAlternative> i = alternatives.iterator();

        boolean matchFound = false;

        while (i.hasNext() && !matchFound) {
            GeoLocateAlternative alt = i.next();
            if (potentialMatches != null && potentialMatches.size() > 0) {
                if (GeoLocateResult.isLocationNearAResult(alt.getLatitude(), alt.getLongitude(), potentialMatches,
                        (int) Math.round(thresholdDistanceKm * 1000))) {
                    Map<String, Object> correctedValues = new HashMap<>();

                    correctedValues.put("latitude", alt.getLatitude());
                    correctedValues.put("longitude", alt.getLongitude());

                    georeference.apply(CurationStatus.CURATED, comment("Modified coordinates ({}) are near (within " +
                                    "georeference error radius or {} km) the georeference for the " +
                                    "locality text from the Geolocate service.  Accepting the {} coordinates. ",
                            alt.getAlternative(), thresholdDistanceKm, alt.getAlternative()));
                    matchFound = true;
                }
            } else if (isMarine) {
                if (country != null && GEOUtil.isCountryKnown(country)) {
                    // 24 nautical miles, territorial waters plus contigouus zone.
                    double thresholdDistanceKmFromLand = 44.448d;
                    if (GEOUtil.isPointNearCountry(country, originalLat, originalLong,
                            thresholdDistanceKmFromLand)) {
                        georeference.apply(CurationStatus.CURATED, comment("Modified coordinate ({}) is within " +
                                "24 nautical miles of country boundary.", alt.getAlternative()));
                        Map<String, Object> correctedValues = new HashMap<>();

                        correctedValues.put("latitude", alt.getLatitude());
                        correctedValues.put("longitude", alt.getLongitude());

                        matchFound = true;
                    }
                }
            } else {
                if (GEOUtil.isCountryKnown(country) &&
                        GEOUtil.isPointInCountry(country, alt.getLatitude(), alt.getLongitude())) {
                    georeference.apply(comment("Modified coordinate ({}) is inside country ({}).",
                            alt.getAlternative(), country));
                    if (GEOUtil.isPrimaryKnown(country, stateProvince) &&
                            GEOUtil.isPointInPrimary(country, stateProvince, originalLat, originalLong)) {

                        Map<String, Object> correctedValues = new HashMap<>();

                        correctedValues.put("latitude", alt.getLatitude());
                        correctedValues.put("longitude", alt.getLongitude());

                        georeference.apply(CurationStatus.CURATED, correctedValues, comment("Modified coordinate " +
                                "({}) is inside stateProvince ({}).", alt.getAlternative(), stateProvince));

                        matchFound = true;
                    }
                }
            }
        }
    }
}
