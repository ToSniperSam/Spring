package com.example.demo.model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FileData {
    private List<Map<String, String>> originalData = new ArrayList<>();
    private List<Map<String, String>> sortedData = new ArrayList<>();
    private List<Map<String, String>> searchResults = new ArrayList<>();

    private String[] VARIABLE_NAMES = {
            "VehicleType", "DirectionTime_O", "GantryID_O",
            "DirectionTime_D", "GantryID_D", "TripLength",
            "TripEnd", "TripInformation"
    };

    // Getters and Setters
    public List<Map<String, String>> getOriginalData() {
        return originalData;
    }

    public void setOriginalData(List<Map<String, String>> originalData) {
        this.originalData = originalData;
    }

    public List<Map<String, String>> getSortedData() {
        return sortedData;
    }

    public void setSortedData(List<Map<String, String>> sortedData) {
        this.sortedData = sortedData;
    }

    public List<Map<String, String>> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<Map<String, String>> searchResults) {
        this.searchResults = searchResults;
    }

    public String[] getVARIABLE_NAMES() {
        return VARIABLE_NAMES;
    }

    public void setVARIABLE_NAMES(String[] VARIABLE_NAMES) {
        this.VARIABLE_NAMES = VARIABLE_NAMES;
    }
}
