package org.kurator.validation.examples;

import edu.tulane.museum.www.webservices.Georef_Result_Set;
import org.filteredpush.kuration.services.exceptions.ServiceException;
import org.filteredpush.kuration.services.geolocate.GeoLocateAlternative;
import org.filteredpush.kuration.services.geolocate.GeoLocateRequest;
import org.filteredpush.kuration.services.geolocate.GeoLocateResult;
import org.filteredpush.kuration.services.geolocate.GeoLocateService;
import org.filteredpush.kuration.util.CurationStatus;
import org.filteredpush.kuration.util.GEOUtil;
import org.kurator.validation.model.Georeference;

import java.util.*;

import static org.filteredpush.kuration.StringUtils.comment;

/**
 * Created by lowery on 8/2/16.
 */
public class CheckAlternativesFunctional {
    Georeference georef;

    public static void main(String[] args) throws ServiceException {
        CheckAlternativesFunctional geolocate = new CheckAlternativesFunctional();
        geolocate.validate("United States", "Alaska", "", "Barrow", "71.295556", "-156.766389", 20d);
    }

    public void validate(Georeference georef, double thresholdDistanceKm) throws ServiceException {

        // query geolocate service
        GeoLocateRequest request = new GeoLocateRequest(georef.country(), georef.stateProvince(), georef.county(),
                georef.locality(), false, false);

        List<GeoLocateResult> potentialMatches =
                GeoLocateResult.constructFromGeolocateResultSet(GeoLocateService.geoLocate2(request));

        // check isMarine
        boolean isMarine = (georef.country() == null || georef.country().length() == 0) &&
                (georef.stateProvince() == null || georef.stateProvince().length() == 0) &&
                (georef.county() == null || georef.county().length() == 0);

        // Construct a list of alternatives
        List<GeoLocateAlternative> alternatives = GeoLocateAlternative.constructListOfAlternatives(
                Double.valueOf(georef.latitude()), Double.valueOf(georef.longitude()));

        // check alternatives
        if (isMarine) {
            alternatives.forEach((alt, originalLat, originalLong) -> new MatchForIsMarine(georef).match(alt, originalLat, originalLong));
        }
    }

    public void checkAlternatives(CheckForMatch checkForMatch, double originalLat, double originalLong) {



    }

}

interface CheckForMatch {
    boolean match(GeoLocateAlternative alt, double originalLat, double originalLong);
}

class MatchForIsMarine implements CheckForMatch {

    private final Georeference georef;

    public MatchForIsMarine(Georeference georef) {
        this.georef = georef;
    }

    public boolean match(GeoLocateAlternative alt, double originalLat, double originalLong) {

        if (georef.country() != null && GEOUtil.isCountryKnown(georef.country())) {

            // 24 nautical miles, territorial waters plus contigouus zone.
            double thresholdDistanceKmFromLand = 44.448d;

            if (GEOUtil.isPointNearCountry(georef.country(), originalLat, originalLong,
                    thresholdDistanceKmFromLand)) {

                Map<String, Object> correctedValues = new HashMap<>();

                correctedValues.put("latitude", alt.getLatitude());
                correctedValues.put("longitude", alt.getLongitude());

                georef.apply(CurationStatus.CURATED, comment("Modified coordinate ({}) is within " +
                        "24 nautical miles of country boundary.", alt.getAlternative()));

                return true;
            }
        }

        return false;
    }
}

class MatchForTerrestrial implements CheckForMatch {
    private final Georeference georef;

    public MatchForTerrestrial(Georeference georef) {
        this.georef = georef;
    }

    public boolean match(GeoLocateAlternative alt, double originalLat, double originalLong) {

        if (GEOUtil.isCountryKnown(georef.country()) &&
                GEOUtil.isPointInCountry(georef.country(), alt.getLatitude(), alt.getLongitude())) {

            georef.apply(comment("Modified coordinate ({}) is inside country ({}).",
                    alt.getAlternative(), georef.country()));

            if (GEOUtil.isPrimaryKnown(georef.country(), georef.stateProvince()) &&
                    GEOUtil.isPointInPrimary(georef.country(), georef.stateProvince(), originalLat, originalLong)) {

                Map<String, Object> correctedValues = new HashMap<>();

                correctedValues.put("latitude", alt.getLatitude());
                correctedValues.put("longitude", alt.getLongitude());

                georef.apply(CurationStatus.CURATED, correctedValues, comment("Modified coordinate " +
                        "({}) is inside stateProvince ({}).", alt.getAlternative(), georef.stateProvince()));

                return true;
            }
        }

        return false;
    }
}