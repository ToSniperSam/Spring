package com.example.demo.controller;

import com.example.demo.util.CustomSorter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;

@Controller
public class FileController {

    private List<Map<String, String>> originalData = new ArrayList<>();
    private List<Map<String, String>> sortedData = new ArrayList<>();

    private List<Map<String, String>> searchResults;
    private static final String[] VARIABLE_NAMES = {
            "VehicleType", "DirectionTime_O", "GantryID_O",
            "DirectionTime_D", "GantryID_D", "TripLength",
            "TripEnd", "TripInformation"
    };

    @RequestMapping("/")
    public String index() {
        return "index"; // Return the Thymeleaf index.html page
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        originalData.clear(); // Clear the original data

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    // Remove BOM
                    line = line.replace("\uFEFF", ""); // Remove UTF-8 BOM
                    isFirstLine = false;
                }

                String[] row = line.split(",");
                Map<String, String> rowData = new LinkedHashMap<>();
                rowData.put("Order", String.valueOf(originalData.size() + 1));

                for (int j = 0; j < VARIABLE_NAMES.length; j++) {
                    String value = (j < row.length) ? row[j] : ""; // Handle missing data

                    // Special handling for the 8th column (TripInformation)
                    if (j == 7) {
                        value = value.split(";")[0]; // Take the content before the semicolon
                    }

                    rowData.put(VARIABLE_NAMES[j], value);
                }
                originalData.add(rowData);
            }

            // If the data exceeds 100 rows, only display the first 100 rows
            if (originalData.size() > 100) {
                model.addAttribute("warning", "Dataset is too large, displaying only the first 100 rows");
                model.addAttribute("data", originalData.subList(0, 100));
            } else {
                model.addAttribute("data", originalData);
            }

        } catch (Exception e) {
            model.addAttribute("error", "Failed to upload the file. Please check if it is in CSV format");
        }

        return "index"; // Return to the upload page and display the data
    }

    @PostMapping("/sort")
    public String sortData(@RequestParam("column") String column, @RequestParam("order") String order, Model model) {
        List<Map<String, String>> dataToSort = new ArrayList<>(originalData); // Get a copy of the original data

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

            sortedData = new ArrayList<>(dataToSort);

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

    // Helper method: Parse a string to a Double
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

    @PostMapping("/search")
    public String searchData(@RequestParam("column") String column,
                             @RequestParam("keyword") String keyword,
                             Model model) {
        // Ensure the search data is based on the original data, not sorted or filtered data
        List<Map<String, String>> dataToSearch = new ArrayList<>(originalData); // originalData is the global variable
        List<Map<String, String>> filteredData = new ArrayList<>();

        try {
            long startTime = System.currentTimeMillis(); // Start timing

            // Iterate through the data, looking for records that contain the keyword (case-insensitive)
            for (Map<String, String> row : dataToSearch) {
                String value = row.get(column);
                if (value != null && value.toLowerCase().contains(keyword.toLowerCase())) {
                    filteredData.add(row); // Add records that match the keyword
                }
            }

            // Handle the search results
            if (filteredData.isEmpty()) {
                model.addAttribute("info", "No relevant data found");
                model.addAttribute("data", dataToSearch.subList(0, Math.min(dataToSearch.size(), 100))); // Display the first 100 rows of original data
            } else {
                if (filteredData.size() > 100) {
                    model.addAttribute("warning", "Data exceeds 100 rows, displaying only the first 100 results");
                    model.addAttribute("data", filteredData.subList(0, 100));
                } else {
                    model.addAttribute("data", filteredData);
                }
                model.addAttribute("searchResultsCount", filteredData.size()); // Total number of search results
            }

            // Save the search results to the global variable searchResults
            searchResults = filteredData;

            long endTime = System.currentTimeMillis(); // End timing
            model.addAttribute("searchTime", endTime - startTime); // Search time
            model.addAttribute("currentColumn", column); // Current search column
            model.addAttribute("searchKeyword", keyword); // Current search keyword

        } catch (Exception e) {
            model.addAttribute("error", "Error! " + e.getMessage());
        }

        return "index"; // Return to the page
    }

    @PostMapping("/export")
    public ResponseEntity<?> exportData(@RequestParam(value = "type", required = false) String type) {
        List<Map<String, String>> dataToExport;

        // Determine whether to export sorted data or search results based on the type parameter
        if ("search".equalsIgnoreCase(type)) {
            dataToExport = searchResults; // searchResults is the global variable storing the latest search results
            if (dataToExport == null || dataToExport.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No search results available");
            }
        } else {
            dataToExport = sortedData; // sortedData is the global variable storing the latest sorted data
            if (dataToExport == null || dataToExport.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No data available");
            }
        }

        try {
            // Generate CSV content
            StringBuilder csvContent = new StringBuilder();
            List<String> csvHeaders = new ArrayList<>(dataToExport.get(0).keySet());
            csvContent.append(String.join(",", csvHeaders)).append("\n");

            for (Map<String, String> row : dataToExport) {
                List<String> rowData = csvHeaders.stream()
                        .map(header -> row.getOrDefault(header, ""))
                        .collect(Collectors.toList());
                csvContent.append(String.join(",", rowData)).append("\n");
            }

            // Set response headers
            HttpHeaders responseHeaders = new HttpHeaders();
            String filename = "search".equalsIgnoreCase(type) ? "search_results.csv" : "sorted_data.csv";
            responseHeaders.add("Content-Disposition", "attachment; filename=" + filename);
            responseHeaders.add("Content-Type", "text/csv; charset=UTF-8");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(csvContent.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error exporting data: " + e.getMessage());
        }
    }
}
