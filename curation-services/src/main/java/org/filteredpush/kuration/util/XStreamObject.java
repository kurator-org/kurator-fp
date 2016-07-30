package org.filteredpush.kuration.util;

import com.thoughtworks.xstream.XStream;
import org.filteredpush.kuration.services.geolocate.GeoLocateResponse;

/**
 * Created by lowery on 7/30/16.
 */
public abstract class XStreamObject {

    // For serializing an instance of this class as xml
    private static final XStream xstream = new XStream();

    public static Object fromXML(String xml) {
        return xstream.fromXML(xml);
    }
}
