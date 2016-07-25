package org.kurator.messages;

/**
 * Created by lowery on 7/25/16.
 */
public class ReceivedMoreData {
    private String data;

    public ReceivedMoreData(String data) {
        this.data = data;
    }

    public String data() {
        return data;
    }
}
