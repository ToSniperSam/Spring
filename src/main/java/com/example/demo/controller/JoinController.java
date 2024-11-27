package com.example.demo.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/file")
public class JoinController {

    // 上传并合并两个 CSV 文件
    @PostMapping("/merge")
    public String mergeCsvFiles(@RequestParam("file1") MultipartFile file1,
                              @RequestParam("file2") MultipartFile file2,
                              HttpServletResponse response) throws IOException {

        // 设置响应类型为 CSV 文件
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=merged_data.csv");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()))) {

            // 读取并合并第一个文件
            mergeFile(file1, writer);
            // 读取并合并第二个文件
            mergeFile(file2, writer);

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error writing merged CSV file.");
        }
        return "index"; // Return to the page
    }

    // 读取文件并将其内容写入响应流
    private void mergeFile(MultipartFile file, BufferedWriter writer) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;  // 用于控制标题行是否重复

            while ((line = reader.readLine()) != null) {
                // 只在第一次写入时添加标题行
                if (firstLine) {
                    firstLine = false;
                } else {
                    writer.newLine();  // 添加换行符，避免在文件开头加上换行
                }

                // 直接将当前行写入响应流
                writer.write(line);
            }
        }
    }
}
