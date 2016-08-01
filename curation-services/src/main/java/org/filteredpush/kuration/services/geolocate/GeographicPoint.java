package org.filteredpush.kuration.services.geolocate;

import com.thoughtworks.xstream.XStream;

public class GeographicPoint {

    private double latitude;
    private double longitude;

    // For serializing an instance of this class as xml
    private static final XStream xstream = new XStream();

    public GeographicPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    public static GeographicPoint fromXML(String xml) {
        return (GeographicPoint) xstream.fromXML(xml);
    }
}