/**
 * 
 */
package org.filteredpush.kuration.services.geolocate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import edu.tulane.museum.www.webservices.Georef_Result;
import edu.tulane.museum.www.webservices.Georef_Result_Set;
import org.filteredpush.kuration.util.GEOUtil;

/**
 * Representation of a single georeference assertion as made by GeoLocate's service.
 * 
 * @author mole
 *
 */
public class GeoLocateResult implements Serializable {

	private static final long serialVersionUID = 5250497528282353757L;

	private static final Log logger = LogFactory.getLog(GeoLocateResult.class);

	public static final int MIN_SCORE_THRESHOLD = 25;

	private Double latitude;
	private Double longitude;
	private Integer coordinateUncertainty;
	private Integer confidence;
	private String parseString;
	/**
	 * @param latitude
	 * @param longitude
	 * @param confidence
	 * @param parseString
	 */
	public GeoLocateResult(double latitude, double longitude, int coordinateUncertainty, int confidence,
						   String parseString) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.confidence = confidence;
		this.parseString = parseString;
		this.coordinateUncertainty = coordinateUncertainty;
	}
	
	public static List<GeoLocateResult> constructFromXML(Document geolocateXmlResult) {
		ArrayList<GeoLocateResult> result = new ArrayList<GeoLocateResult>();
		
		return result;
	}
	
	public static List<GeoLocateResult> constructFromGeolocateResultSet(Georef_Result_Set results) {
		ArrayList<GeoLocateResult> resultList = new ArrayList<GeoLocateResult>();
		if (results !=null && results.getNumResults()>0) {
		    int numResults = results.getNumResults(); 
			for (int i=0; i<numResults; i++) { 
			   Georef_Result row = results.getResultSet(i);
			   if (row.getScore()>MIN_SCORE_THRESHOLD) { 
				   int uncertainty = 0;
				   try { 
					   if (row.getUncertaintyRadiusMeters()!=null && !row.getUncertaintyRadiusMeters().equals("Unavailable")) { 
					      uncertainty = Integer.parseInt(row.getUncertaintyRadiusMeters());
					   }
				   } catch (NumberFormatException ex) {
					   logger.debug(ex.getMessage());
				   }
				   GeoLocateResult result = new GeoLocateResult(
						   row.getWGS84Coordinate().getLatitude(),
						   row.getWGS84Coordinate().getLongitude(),
						   uncertainty,
						   row.getScore(),
						   row.getParsePattern()
						   );
				   resultList.add(result);
			   }
			}
		}
		return resultList;
	}
	
	public static boolean isLocationNearAResult(double latitude, double longitude, List<GeoLocateResult> toCompare, int thresholdDistanceMeters) {
		boolean result = false;
		if (toCompare!=null && toCompare.size()>0) { 
			Iterator<GeoLocateResult> i = toCompare.iterator();
			while (!result && i.hasNext()) {
				GeoLocateResult candidate = i.next();
				long distance = GEOUtil.calcDistanceHaversineMeters(latitude, longitude, candidate.getLatitude(), candidate.getLongitude());
				if (candidate.getCoordinateUncertaintyMeters()>0) { 
					if (distance<=candidate.getCoordinateUncertaintyMeters() || distance<=thresholdDistanceMeters) { 
						result = true;
					}
				} else { 
				    if (distance<=thresholdDistanceMeters) { 
				    	result = true;
				    }
				}
			}
		}
		return result;
	}

	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	/**
	 * @return the coordinateUncertainty
	 */
	public Integer getCoordinateUncertaintyMeters() {
		return coordinateUncertainty;
	}
	/**
	 * @param coordinateUncertainty the coordinateUncertainty to set
	 */
	public void setCoordinateUncertaintyMeters(Integer coordinateUncertainty) {
		this.coordinateUncertainty = coordinateUncertainty;
	}
	/**
	 * @return the confidence
	 */
	public Integer getConfidence() {
		return confidence;
	}
	/**
	 * @param confidence the confidence to set
	 */
	public void setConfidence(Integer confidence) {
		this.confidence = confidence;
	}
	/**
	 * @return the parseString
	 */
	public String getParseString() {
		return parseString;
	}
	/**
	 * @param parseString the parseString to set
	 */
	public void setParseString(String parseString) {
		this.parseString = parseString;
	}
	
	
}
