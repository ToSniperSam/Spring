package com.example.demo.repository.pro1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CSVAnalyzer {
    public static void main(String[] args) {
        String filePath = "D:\\javaProject\\src\\ust\\TDCS_M06A_20231204_080000.csv";
        // print data for testing
        int sampleRows = 1;

        analyzeCSV(filePath, sampleRows);
    }

    public static void analyzeCSV(String filePath, int sampleRows) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = {"VehicleType", "DirectionTime_O", "GantryID_O", "DirectionTime_D", "GantryID_D", "TripLength", "TripEnd", "TripInformation"};
            Map<String, Set<String>> uniqueValuesMap = new HashMap<>();

            // Initialize unique values map for each column
            for (String header : headers) {
                uniqueValuesMap.put(header, new HashSet<>());
            }

            int rowCount = 0;
            while ((line = br.readLine()) != null) {
                // Assume data is comma-separated
                String[] values = line.split(",");

                // Ensure row has correct number of columns
                if (values.length != headers.length) {
                    System.out.println("Skipping row " + (rowCount + 1) + " due to unexpected column count.");
                    continue;
                }

                // Process each column only for the specified headers
                if (rowCount < sampleRows) {
                    System.out.println("Row " + (rowCount + 1) + ":");
                    System.out.println("  VehicleType: " + values[0].trim());
                    System.out.println("  DirectionTime_O: " + values[1].trim());
                    System.out.println("  GantryID_O: " + values[2].trim());
                    System.out.println("  TripLength: " + values[5].trim());
                    System.out.println();
                }

                // Add unique values for analysis
                uniqueValuesMap.get("VehicleType").add(values[0].trim());
                uniqueValuesMap.get("DirectionTime_O").add(values[1].trim());
                uniqueValuesMap.get("GantryID_O").add(values[2].trim());
                uniqueValuesMap.get("TripLength").add(values[5].trim());

                rowCount++;
            }

            // Print summary of unique values for specified columns
            System.out.println("Column Summary:");
            System.out.println("VehicleType - Unique Values Count: " + uniqueValuesMap.get("VehicleType").size());
            System.out.println("DirectionTime_O - Unique Values Count: " + uniqueValuesMap.get("DirectionTime_O").size());
            System.out.println("GantryID_O - Unique Values Count: " + uniqueValuesMap.get("GantryID_O").size());
            System.out.println("TripLength - Unique Values Count: " + uniqueValuesMap.get("TripLength").size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
