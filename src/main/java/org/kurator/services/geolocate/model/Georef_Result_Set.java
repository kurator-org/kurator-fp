package org.kurator.services.geolocate.model;

/**
 * Created by lowery on 7/22/16.
 */
public class Georef_Result_Set {
    private String EngineVersion;
    private String NumResults;
    private String ExecutionTimems;

    public String getEngineVersion() {
        return EngineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        EngineVersion = engineVersion;
    }

    public String getNumResults() {
        return NumResults;
    }

    public void setNumResults(String numResults) {
        NumResults = numResults;
    }

    public String getExecutionTimems() {
        return ExecutionTimems;
    }

    public void setExecutionTimems(String executionTimems) {
        ExecutionTimems = executionTimems;
    }

    @Override
    public String toString() {
        return "Georef_Result_Set{" +
                "EngineVersion='" + EngineVersion + '\'' +
                ", NumResults='" + NumResults + '\'' +
                ", ExecutionTimems='" + ExecutionTimems + '\'' +
                '}';
    }
}
