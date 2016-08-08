package org.filteredpush.kuration.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 06.05.2013
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class CurationException extends Exception {
    public CurationException() {
        super();
    }

    public CurationException(String message) {
        super(message);
    }

    public CurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CurationException(Throwable cause) {
        super(cause);
    }

    protected CurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
