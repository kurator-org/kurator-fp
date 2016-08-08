package org.filteredpush.kuration.services.geolocate;

import edu.tulane.museum.www.webservices.GeolocatesvcSoapProxy;
import edu.tulane.museum.www.webservices.Georef_Result_Set;
import org.filteredpush.kuration.services.exceptions.ServiceException;

import java.rmi.RemoteException;
import java.util.List;

/**
 * http://www.museum.tulane.edu/webservices/geolocatesvc/geolocatesvc.asmx
 */
public class GeoLocateService {
    public static Georef_Result_Set geoLocate2(GeoLocateRequest request) throws ServiceException {
        GeolocatesvcSoapProxy geolocateService = new GeolocatesvcSoapProxy();
        try {
            Georef_Result_Set response = geolocateService.georef2(request.country(), request.stateProvince(),
                    request.county(), request.locality(), request.hwyx(), request.findWaterBody(),
                    request.restrictToLowestAdm, request.doUncert, request.doPoly, request.displacePoly,
                    request.polyAsLinkID, request.languageKey);


            return response;
        } catch (RemoteException e) {
            throw new ServiceException("Could not query geolocate2 service", e);
        }
    }
}
