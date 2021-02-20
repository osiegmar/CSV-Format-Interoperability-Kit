package de.siegmar.fastcsv.cli;

import java.util.Map;

public class Test {

    private String id;
    private String description;
    private Map<String, String> settings;
    private Record[] records;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(final Map<String, String> settings) {
        this.settings = settings;
    }

    public Record[] getRecords() {
        return records;
    }

    public void setRecords(final Record[] records) {
        this.records = records;
    }

}
