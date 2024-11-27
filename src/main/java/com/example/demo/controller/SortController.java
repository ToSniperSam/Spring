package com.example.demo.controller;

import com.example.demo.model.FileData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class SortController {

    private final FileData fileData;

    public SortController(FileData fileData) {
        this.fileData = fileData;
    }

    @PostMapping("/sort")
    public String sortData(@RequestParam("column") String column, @RequestParam("order") String order, Model model) {
        List<Map<String, String>> dataToSort = new ArrayList<>(fileData.getOriginalData()); // Get a copy of the original data

        // Determine sort order: ascending is false, descending is true
        boolean descending = "desc".equals(order);

        try {
            long startTime = System.currentTimeMillis(); // Start timing

            switch (column) {
                case "VehicleType":
                case "TripLength": {
                    if (column.equals("TripLength")) {
                        // TripLength is a floating-point number
                        List<Map<String, String>> sortedData = new ArrayList<>(dataToSort);
                        sortedData.sort((row1, row2) -> {
                            Double val1 = parseDouble(row1.get(column));
                            Double val2 = parseDouble(row2.get(column));
                            return descending ? val2.compareTo(val1) : val1.compareTo(val2);
                        });

                        dataToSort = sortedData; // Update the sorted data
                    } else {
                        // VehicleType is an integer
                        List<Map<String, String>> sortedData = new ArrayList<>(dataToSort);
                        sortedData.sort((row1, row2) -> {
                            Integer val1 = parseInt(row1.get(column));
                            Integer val2 = parseInt(row2.get(column));
                            return descending ? val2.compareTo(val1) : val1.compareTo(val2);
                        });

                        dataToSort = sortedData; // Update the sorted data
                    }
                    break;
                }

                case "DirectionTime_O":
                case "DirectionTime_D": {
                    List<Map<String, String>> sortedData = new ArrayList<>(dataToSort);
                    sortedData.sort((row1, row2) -> {
                        Long val1 = parseDate(row1.get(column));
                        Long val2 = parseDate(row2.get(column));
                        return descending ? val2.compareTo(val1) : val1.compareTo(val2);
                    });

                    dataToSort = sortedData; // Update the sorted data
                    break;
                }

                case "GantryID_O":
                case "GantryID_D": {
                    List<Map<String, String>> sortedData = new ArrayList<>(dataToSort);
                    sortedData.sort((row1, row2) -> {
                        String val1 = row1.get(column);
                        String val2 = row2.get(column);
                        return descending ? val2.compareTo(val1) : val1.compareTo(val2);
                    });

                    dataToSort = sortedData; // Update the sorted data
                    break;
                }

                default:
                    throw new IllegalArgumentException("Invalid Column: " + column);
            }

            fileData.setSortedData(dataToSort);

            long endTime = System.currentTimeMillis(); // End timing
            model.addAttribute("sortTime", endTime - startTime); // Sorting time
            model.addAttribute("currentColumn", column); // Add current sort column information
            model.addAttribute("descending", descending ? "DESC" : "ASC");

            // If the sorted data exceeds 100 rows, only display the first 100 rows
            if (dataToSort.size() > 100) {
                model.addAttribute("warning", "Dataset too large, displaying only the first 100 rows");
                model.addAttribute("data", dataToSort.subList(0, 100));
            } else {
                model.addAttribute("data", dataToSort);
            }

        } catch (RuntimeException e) {
            model.addAttribute("error", "Failed to sort! Error: " + e.getMessage());
        }

        return "index"; // Return to the upload page and display the sorted data
    }


    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace("\uFEFF", "").trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    // Helper method: Parse a string to an Integer
    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value.replace("\uFEFF", "").trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    // Helper method: Parse a string to a Long (for date)
    private Long parseDate(String value) {
        try {
            return java.time.LocalDateTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } catch (Exception e) {
            return null;
        }
    }


}
