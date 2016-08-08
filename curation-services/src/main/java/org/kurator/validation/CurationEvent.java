package org.kurator.validation;

import org.filteredpush.kuration.util.CurationStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lowery on 7/30/16.
 */
public class CurationEvent {
    private final String comment;
    private final CurationStatus status;
    private final Map<String, Object> updates = new HashMap<>();

    public CurationEvent(final String comment, final CurationStatus status, final Map<String, Object> correction) {
        this.comment = comment;
        this.status = status;

        updates.putAll(correction);
    }

    public CurationEvent(final String comment, final CurationStatus status) {
        this(comment, status, new HashMap<>());
    }

    public CurationEvent(final String comment) {
        this(comment, CurationStatus.NO_CHANGE, new HashMap<>());
    }

    public String getComment() {
        return comment;
    }

    public CurationStatus getStatus() {
        return status;
    }

    public Map<String, Object> getUpdates() {
        return Collections.unmodifiableMap(updates);
    }
}
