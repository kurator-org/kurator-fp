package org.filteredpush.kuration.util;

/**
 * Created by lowery on 7/29/16.
 */
public enum CurationStatus {

    //Indicates that no issues were found in the data per the specification.

    CORRECT,

    // Indicates that a change to the data has been proposed.

    CURATED,

    // Indicates that a change to the data has been proposed.

    FILLED_IN,

    /*
     * Indicates that it was possible to perform the tests of the specification on the
     * data, but that it was not possible to validate the provided data to the specification.
     * This tends to indicate a Solve_With_More_Data outcome.
     */

    UNABLE_CURATE,

    /*
     * Some prerequisite for performing the tests in the specification was not met.  This could
     * be internal to the data (some required field was missing), or external (a webservice
     * was down and unable to be consulted).
     */

    UNABLE_DETERMINE_VALIDITY

}
