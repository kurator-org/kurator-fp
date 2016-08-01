package org.filteredpush.kuration.services.geolocate;

import com.thoughtworks.xstream.XStream;

public class GeoRefResult {

    private GeographicPoint wgs84Coordinate;

    private String parsePattern;
    private String precision;
    private int score;
    private String uncertaintyRadiusMeters;
    private String uncertaintyPolygon;
    private String referenceLocation;
    private double displacedDistanceMiles;
    private double displacedHeadingDegrees;
    private String debug;

    // For serializing an instance of this class as xml
    private static final XStream xstream = new XStream();

    static {
        xstream.alias("Georef_Result", GeoRefResult.class);
        xstream.aliasField("wgs84Coordinate", GeoRefResult.class, "wgs84Coordinate");

        // Other fields are automatically aliased based on field variable names
    }

    public GeoRefResult(GeographicPoint wgs84Coordinate, String parsePattern, String precision, int score,
                        String uncertaintyRadiusMeters, String uncertaintyPolygon, String referenceLocation,
                        double displacedDistanceMiles, double displacedHeadingDegrees, String debug) {

        this.wgs84Coordinate = wgs84Coordinate;
        this.parsePattern = parsePattern;
        this.precision = precision;
        this.score = score;
        this.uncertaintyRadiusMeters = uncertaintyRadiusMeters;
        this.uncertaintyPolygon = uncertaintyPolygon;
        this.referenceLocation = referenceLocation;
        this.displacedDistanceMiles = displacedDistanceMiles;
        this.displacedHeadingDegrees = displacedHeadingDegrees;
        this.debug = debug;

    }

    public GeographicPoint wgs84Coordinate() {
        return wgs84Coordinate;
    }

    public String parsePattern() {
        return parsePattern;
    }

    public String precision() {
        return precision;
    }

    public int score() {
        return score;
    }

    public String uncertaintyRadiusMeters() {
        return uncertaintyRadiusMeters;
    }

    public String uncertaintyPolygon() {
        return uncertaintyPolygon;
    }

    public String referenceLocation() {
        return referenceLocation;
    }

    public double displacedDistanceMiles() {
        return displacedDistanceMiles;
    }

    public double displacedHeadingDegrees() {
        return displacedHeadingDegrees;
    }

    public String debug() {
        return debug;
    }

    public static GeoRefResult fromXML(String xml) {
        return (GeoRefResult) xstream.fromXML(xml);
    }

}