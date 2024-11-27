package com.example.demo.controller;

import com.example.demo.model.FileData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class FileUploadController {

    private final FileData fileData;

    public FileUploadController(FileData fileData) {
        this.fileData = fileData;
    }

    @RequestMapping("/")
    public String index() {
        return "index"; // Return the Thymeleaf index.html page
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        fileData.getOriginalData().clear(); // Clear the original data

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
                rowData.put("Order", String.valueOf(fileData.getOriginalData().size() + 1));

                for (int j = 0; j < fileData.getVARIABLE_NAMES().length; j++) {
                    String value = (j < row.length) ? row[j] : ""; // Handle missing data

                    // Special handling for the 8th column (TripInformation)
                    if (j == 7) {
                        value = value.split(";")[0]; // Take the content before the semicolon
                    }

                    rowData.put(fileData.getVARIABLE_NAMES()[j], value);
                }
                fileData.getOriginalData().add(rowData);
            }

            // If the data exceeds 100 rows, only display the first 100 rows
            if (fileData.getOriginalData().size() > 100) {
                model.addAttribute("warning", "Dataset is too large, displaying only the first 100 rows");
                model.addAttribute("data", fileData.getOriginalData().subList(0, 100));
            } else {
                model.addAttribute("data", fileData.getOriginalData());
            }

        } catch (Exception e) {
            model.addAttribute("error", "Failed to upload the file. Please check if it is in CSV format");
        }

        return "index";
    }
}
