package org.kurator.validation.georeference;

import org.filteredpush.kuration.services.exceptions.ServiceException;
import org.filteredpush.kuration.services.geolocate.GeoLocateRequest;
import org.filteredpush.kuration.services.geolocate.GeoLocateResponse;
import org.filteredpush.kuration.services.geolocate.GeoRefResultSet;
import org.filteredpush.kuration.util.*;
import org.nocrala.tools.gis.data.esri.shapefile.exception.InvalidShapeFileException;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.filteredpush.kuration.services.geolocate.GeoLocateService.*;
import static org.filteredpush.kuration.StringUtils.*;

/**
 * Created by lowery on 7/29/16.
 */
public class GeoRefValidator {

    public void validate(String country, String stateProvince, String county, String locality,
                         String latitude, String longitude) {

        // Look up locality in Tulane's GeoLocateService service
        List<GeolocationResult> potentialMatches = queryGeoLocate(country, stateProvince, county, locality);

        if (potentialMatches.isEmpty())
            status("GeoLocateService service can't find coordinates of Locality.", CurationComment.UNABLE_DETERMINE_VALIDITY);

        // Make strings null if they don't contain valid lat/long values
        latitude = isNumeric(latitude) ? latitude : null;
        longitude = isNumeric(longitude) ? longitude : null;

        // Try to fill in missing values
        if ((latitude == null || longitude == null)) {
            fillInMissingValues(potentialMatches, latitude, longitude);
        }

    }

    private void fillInMissingValues(List<GeolocationResult> potentialMatches, String latitude, String longitude) {
        if (potentialMatches.size() > 0 &&
                potentialMatches.get(0).getConfidence() > 80) {

            if (latitude !=null && longitude == null) {
                // Try to fill in the longitude
                if (GeolocationResult.isLocationNearAResult(
                        Double.valueOf(latitude),
                        potentialMatches.get(0).getLongitude(),
                        potentialMatches,
                        (int) Math.round(thresholdDistanceKm * 1000))) {

                    // if latitude plus longitude from best match is near a match, propose
                    // the longitude from the best match.
                    status("Added a longitude from {} as longitude was missing and geolocate had a confident match " +
                            "near the original line of latitude.", CurationStatus.FILLED_IN, serviceName());

                    // TODO: If we do this, then we need to add the datum, georeference source, georeference method, etc
                    correctedLongitude = potentialMatches.get(0).getLongitude();
                }
            }

            if (latitude != null && longitude == null) {
                // Try to fill in the latitude
                if (GeolocationResult.isLocationNearAResult(
                        potentialMatches.get(0).getLatitude(),
                        Double.valueOf(longitude),
                        potentialMatches,
                        (int)Math.round(thresholdDistanceKm * 1000))) {

                    // if latitude plus longitude from best match is near a match, propose
                    // the longitude from the best match.
                    status("Added a latitude from {} as latitude was missing and geolocate had a confident match " +
                            "near the original line of longitude.", CurationStatus.FILLED_IN, serviceName());

                    // TODO: If we do this, then we need to add the datum, georeference source, georeference method, etc
                    correctedLatitude = potentialMatches.get(0).getLatitude();
                }
            }

            //Both coordinates in the original record are missing
            if (latitude == null && longitude == null) {
                status("Added a georeference using cached data or {} service since the original coordinates are " +
                        "missing and geolocate had a confident match.", CurationComment.FILLED_IN, getServiceName());

                // TODO: If we do this, then we need to add the datum, georeference source, georeference method, etc.
                correctedLatitude = potentialMatches.get(0).getLatitude();
                correctedLongitude = potentialMatches.get(0).getLongitude();
            }

        } else {
            status("No latitude and/or longitude provided, and geolocate didn't return a good match.",
                    CurationComment.UNABLE_DETERMINE_VALIDITY);
        }
    }

    public List<GeolocationResult> queryGeoLocate(String country, String stateProvince, String county, String locality) {
        List<GeolocationResult> potentialMatches = new ArrayList<>();

        try {

            locality = locality.toLowerCase();

            boolean hwyX = locality.matches("bridge");
            boolean findWaterbody = locality.matches("(lake|pond|sea|ocean)");

            GeoLocateRequest request = new GeoLocateRequest(
                    country,
                    stateProvince,
                    county,
                    locality,
                    hwyX,
                    findWaterbody
            );

            // TODO: Can this service accept null values for any of the args?
            GeoLocateResponse response = geoLocate2().queryGeoLocateMulti(request);

            comment("Found {} possible georeferences with Geolocate engine: {}",
                    response.numResults(), response.engineVersion());

            if (response != null && response.numResults() > 0) {
                // TODO: add to the list of potentialMatches
            }

        } catch (ServiceException e) {
            status(e.getMessage(), CurationStatus.UNABLE_DETERMINE_VALIDITY);
        }

        return null;
    }

    private String serviceName() {
        return null; // TODO: define service name
    }

    private void comment(String comment, Object... args) {
        // TODO: implement event-souring
        System.out.println("comment: " + MessageFormat.format(comment, args));
    }

    private void status(String comment, CurationStatus status, Object... args) {
        // TODO: implement event-souring
        System.out.println("status: " + MessageFormat.format(comment, args) + "(" + status + ")");
    }

    private void error(String error) {
        // TODO: implement event-souring
        System.out.println("error: " + error);
    }

    public void more() {
          else {
            // calculate the distance from the returned point and original point in the record
            // If the distance is smaller than a certainty, then use the original point --- GEOService,
            // like GeoLocateService can't parse detailed locality. In this case, the original point has
            // higher confidence. Otherwise, use the point returned from GeoLocateService
            addToComment("Latitute and longitude are both present.");

            double originalLat = Double.valueOf(latitude);
            double originalLong = Double.valueOf(longitude);
            double rawLat = originalLat;
            double rawLong = originalLong;

            // Construct a list of alternatives
            List<GeolocationAlternative> alternatives = GeolocationAlternative.constructListOfAlternatives(originalLat, originalLong);

            boolean flagError = false;
            boolean foundGoodMatch = false;

            // Check for possible error conditions

            // (1) Latitude and longitude out of range
            if (Math.abs(originalLat)>90) {
                addToComment("The original latitude is out of range.");
                flagError = true;
            }
            if (Math.abs(originalLong)>180) {
                addToComment("The original longitude is out of range.");
                flagError = true;
            }
            if (!flagError) {
                addToComment("Latitute is within +/-90 and longitude is within +/-180.");
            }

            // Check country and stateProvince
            if (country != null && country.length()>0) {
                //standardize country names
                if (country.toUpperCase().equals("USA")) {
                    country = "United States";
                } else if (country.toUpperCase().equals("U.S.A.")) {
                    country = "United States";
                } else if (country.toLowerCase().equals("united states of america")) {
                    country = "United States";
                } else {
                    country = country.toUpperCase();
                    //System.out.println("not in !##"+country+"##");
                }

                // (2) Locality not inside country?
                if (GEOUtil.isCountryKnown(country)) {
                    if (GEOUtil.isPointInCountry(country, originalLat, originalLong)) {
                        addToComment("Original coordinate is inside country ("+country+").");
                        addToServiceName("Country boundary data from Natural Earth");
                    } else {
                        addToComment("Original coordinate is not inside country ("+country+").");
                        addToServiceName("Country boundary data from Natural Earth");
                        flagError = true;
                    }
                } else {
                    addToComment("Can't find country: " + country + " in country name list");
                }

                if (stateProvince!=null && stateProvince.length()>0) {
                    // (3) Locality not inside primary division?
                    if (GEOUtil.isPrimaryKnown(country, stateProvince)) {
                        if (GEOUtil.isPointInPrimary(country, stateProvince, originalLat, originalLong)) {
                            addToComment("Original coordinate is inside primary division ("+stateProvince+").");
                            addToServiceName("State/province boundary data from Natural Earth");
                        } else {
                            addToComment("Original coordinate is not inside primary division ("+stateProvince+").");
                            addToServiceName("State/province boundary data from Natural Earth");
                            flagError = true;
                        }
                    } else {
                        addToComment("Can't find state/province: " + stateProvince + " in primaryDivision name list");
                    }
                }
            }

            // (4) Is locality marine?
            Set<Path2D> setPolygon = null;
            try {
                setPolygon = ReadLandData();
                //System.out.println("read data");
            } catch (IOException e) {
                logger.error(e.getMessage());
            } catch (InvalidShapeFileException e) {
                logger.error(e.getMessage());
            }
            boolean isMarine = false;
            if ((country==null||country.length()==0) && (stateProvince==null||stateProvince.length()==0) && (county==null||county.length()==0)) {
                addToComment("No country, state/province, or county provided, guessing that this is a marine locality. ");
                // no country provided, assume locality is marine
                isMarine = true;
            } else {
                if (waterBody!=null && waterBody.trim().length()>0) {
                    if (waterBody.matches("(Indian|Pacific|Arctic|Atlantic|Ocean|Sea|Carribean|Mediteranian)")) {
                        isMarine = true;
                        addToComment("A water body name that appears to be an ocean or a sea was provided, guessing that this is a marine locality. ");
                    } else {
                        addToComment("A country, state/province, or county was provided with a water body that doesn't appear to be an ocean or a sea, guessing that this is a non-marine locality. ");
                    }
                } else {
                    addToComment("A country, state/province, or county was provided but no water body, guessing that this is a non-marine locality. ");
                }
            }
            if (!GEOUtil.isInPolygon(setPolygon, originalLong, originalLat, isMarine)) {
                if (isMarine) {
                    addToComment("Coordinate is on land for a supposedly marine locality.");
                    flagError = true;
                } else {
                    addToComment("Coordinate is not on land for a supposedly non-marine locality.");
                    double thresholdDistanceKmFromLand = 44.448d;  // 24 nautical miles, territorial waters plus contigouus zone.
                    if (GEOUtil.isPointNearCountry(country, originalLat, originalLong, thresholdDistanceKmFromLand)) {
                        addToComment("Coordinate is within 24 nautical miles of country boundary, could be a nearshore marine locality.");
                    } else {
                        addToComment("Coordinate is further than 24 nautical miles of country boundary, country in error or marine within EEZ.");
                        flagError = true;
                    }
                }
            }

            // (5) GeoLocateService returned some result, is original locality near that result?
            if (potentialMatches!=null && potentialMatches.size()>0) {
                if (GeolocationResult.isLocationNearAResult(originalLat, originalLong, potentialMatches, (int)Math.round(thresholdDistanceKm * 1000))) {
                    setCurationStatus(CurationComment.CORRECT);
                    correctedLatitude = originalLat;
                    correctedLongitude = originalLong;
                    addToComment("Original coordinates are near (within georeference error radius or " +  thresholdDistanceKm + " km) the georeference for the locality text from the GeoLocateService service.  Accepting the original coordinates. ");
                    flagError = false;
                    foundGoodMatch = true;
                } else {
                    addToComment("Original coordinates are not near (within georeference error radius or " +  thresholdDistanceKm + " km) the georeference for the locality text from the GeoLocateService service. ");
                    flagError = true;
                }
            }


            // Some error condition was found, see if any transposition returns a plausible locality
            boolean matchFound = false;
            if (flagError) {
                Iterator<GeolocationAlternative> i = alternatives.iterator();
                while (i.hasNext() && !matchFound) {
                    GeolocationAlternative alt = i.next();
                    if (potentialMatches !=null && potentialMatches.size()>0) {
                        if (GeolocationResult.isLocationNearAResult(alt.getLatitude(), alt.getLongitude(), potentialMatches, (int)Math.round(thresholdDistanceKm * 1000))) {
                            setCurationStatus(CurationComment.CURATED);
                            correctedLatitude = alt.getLatitude();
                            correctedLongitude = alt.getLongitude();
                            addToComment("Modified coordinates ("+alt.getAlternative()+") are near (within georeference error radius or " +  thresholdDistanceKm + " km) the georeference for the locality text from the GeoLocateService service.  Accepting the " + alt.getAlternative() + " coordinates. ");
                            matchFound = true;
                        }
                    } else {
                        if (isMarine) {
                            if (country!=null && GEOUtil.isCountryKnown(country)) {
                                double thresholdDistanceKmFromLand = 44.448d;  // 24 nautical miles, territorial waters plus contigouus zone.
                                if (GEOUtil.isPointNearCountry(country, originalLat, originalLong, thresholdDistanceKmFromLand)) {
                                    addToComment("Modified coordinate (" + alt.getAlternative() + ") is within 24 nautical miles of country boundary.");
                                    correctedLatitude = alt.getLatitude();
                                    correctedLongitude = alt.getLongitude();
                                    matchFound = true;
                                }
                            }
                        } else {
                            if (GEOUtil.isCountryKnown(country) &&
                                    GEOUtil.isPointInCountry(country, alt.getLatitude(), alt.getLongitude())) {
                                addToComment("Modified coordinate ("+alt.getAlternative()+") is inside country ("+country+").");
                                if (GEOUtil.isPrimaryKnown(country, stateProvince) &&
                                        GEOUtil.isPointInPrimary(country, stateProvince, originalLat, originalLong)) {
                                    setCurationStatus(CurationComment.CURATED);
                                    addToComment("Modified coordinate ("+alt.getAlternative()+") is inside stateProvince ("+stateProvince+").");
                                    correctedLatitude = alt.getLatitude();
                                    correctedLongitude = alt.getLongitude();
                                    matchFound = true;
                                }
                            }
                        }
                    }
                }
            }

            if (flagError) {
                if (matchFound) {
                    setCurationStatus(CurationComment.CURATED);
                    if(useCache) {
                        addNewToCache(correctedLatitude, correctedLongitude, country, stateProvince, county, locality);
                    }
                } else {
                    if (country!=null && GEOUtil.isCountryKnown(country)) {
                        if (isMarine) {
                            addToComment("No transformation of the coordinates is near the provided country.");
                        } else {
                            if (stateProvince!=null && GEOUtil.isPrimaryKnown(country, stateProvince)) {
                                addToComment("No transformation of the coordinates is inside the provided country and state/province.");
                            }
                        }
                    }
                    setCurationStatus(CurationComment.UNABLE_CURATED);
                }
            } else {
                if (foundGoodMatch) {
                    setCurationStatus(CurationComment.CORRECT);
                    if(useCache) {
                        addNewToCache(originalLat, originalLong, country, stateProvince, county, locality);
                    }
                } else {
                    setCurationStatus(CurationComment.UNABLE_DETERMINE_VALIDITY);
                }
            }

            //System.out.println("setCurationStatus(" + curationStatus);
            //System.out.println("comment = " + comment);

            //System.out.println("originalLng = " + originalLng);
            //System.out.println("originalLat = " + originalLat);
            //System.out.println("country = " + country);
            //System.out.println("foundLng = " + foundLng);
            //System.out.println("foundLat = " + foundLat);

        }
    }
}
