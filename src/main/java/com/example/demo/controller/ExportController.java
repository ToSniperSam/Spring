package com.example.demo.controller;

import com.example.demo.model.FileData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ExportController {

    private final FileData fileData;

    public ExportController(FileData fileData) {
        this.fileData = fileData;
    }

    @PostMapping("/export")
    public ResponseEntity<?> exportData(@RequestParam(value = "type", required = false) String type) {
        List<Map<String, String>> dataToExport;

        // Determine whether to export sorted data or search results based on the type parameter
        if ("search".equalsIgnoreCase(type)) {
            dataToExport = fileData.getSearchResults(); // searchResults is the global variable storing the latest search results
            if (dataToExport == null || dataToExport.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No search results available");
            }
        } else {
            dataToExport = fileData.getSortedData(); // sortedData is the global variable storing the latest sorted data
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
