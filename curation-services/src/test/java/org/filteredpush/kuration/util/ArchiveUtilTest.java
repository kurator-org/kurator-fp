package org.filteredpush.kuration.util;

import edu.tulane.museum.www.webservices.GeolocatesvcSoapProxy;
import edu.tulane.museum.www.webservices.Georef_Result_Set;
import org.filteredpush.kuration.exceptions.CurationException;
import org.filteredpush.kuration.services.exceptions.ServiceException;
import org.filteredpush.kuration.services.geolocate.GeoLocateRequest;
import org.filteredpush.kuration.services.geolocate.GeoLocateResult;
import org.filteredpush.kuration.services.geolocate.GeoLocateService;
import org.junit.Test;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by lowery on 8/1/16.
 */
public class ArchiveUtilTest {
    @Test
    public void openArchiveTest() throws CurationException, URISyntaxException, ServiceException, RemoteException {
        //File file = new File(ArchiveUtilTest.class.getResource("/dwca-mczbase-v162.23.zip").toURI());

       // Archive archive = ArchiveUtil.openArchive(file);

       // Iterator<DarwinCoreRecord> iterator = archive.iteratorDwc();

      //  int i = 0;
      //  while (iterator.hasNext() && i++ < 10) {
        //    DarwinCoreRecord record = iterator.next();

       //     System.out.printf("country: %s stateProvince: %s county: %s locality: %s\n",
       //             record.getCountry(), record.getStateProvince(), record.getCounty(), record.getLocality());
            Georef_Result_Set response = GeoLocateService.geoLocate2(new GeoLocateRequest("United States", "Alaska", null, "Barrow", false, false));

            List<GeoLocateResult> results = GeoLocateResult.constructFromGeolocateResultSet(response);
            System.out.println(results);


        final boolean restrictToLowestAdm = true;
        final boolean doUncert = true;  // include uncertainty radius in results
        final boolean doPoly = false;   // include error polygon in results
        final boolean displacePoly = false;  // displace error polygon in results
        final boolean polyAsLinkID = false;
        final int languageKey = 0;


        boolean findWaterBody = false;
        boolean hwyx = false;

        GeolocatesvcSoapProxy geolocateService = new GeolocatesvcSoapProxy();
        Georef_Result_Set res = geolocateService.georef2("United States", "Alaska", "", "Barrow", false, false, restrictToLowestAdm, doUncert, doPoly, displacePoly, polyAsLinkID, languageKey);

        System.out.println(res.getResultSet(0).getScore());

    }
}
