package com.example.demo.controller;


import com.example.demo.model.FileData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ResearchController {

    private final FileData fileData;

    public ResearchController(FileData fileData) {
        this.fileData = fileData;
    }

    @PostMapping("/search")
    public String searchData(@RequestParam("column") String column,
                             @RequestParam("keyword") String keyword,
                             Model model) {
        // Ensure the search data is based on the original data, not sorted or filtered data
        List<Map<String, String>> dataToSearch = new ArrayList<>(fileData.getOriginalData()); // originalData is the global variable
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
            fileData.setSearchResults(filteredData);

            long endTime = System.currentTimeMillis(); // End timing
            model.addAttribute("searchTime", endTime - startTime); // Search time
            model.addAttribute("currentColumn", column); // Current search column
            model.addAttribute("searchKeyword", keyword); // Current search keyword

        } catch (Exception e) {
            model.addAttribute("error", "Error! " + e.getMessage());
        }

        return "index"; // Return to the page
    }
}
