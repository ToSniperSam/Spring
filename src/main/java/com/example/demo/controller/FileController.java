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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
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
        return "index"; // 返回 Thymeleaf 的 index.html 页面
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        originalData.clear(); // 清空原数据

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    // 去除 BOM
                    line = line.replace("\uFEFF", ""); // 去除 UTF-8 BOM
                    isFirstLine = false;
                }

                String[] row = line.split(",");
                Map<String, String> rowData = new LinkedHashMap<>();
                rowData.put("Order", String.valueOf(originalData.size() + 1));

                for (int j = 0; j < VARIABLE_NAMES.length; j++) {
                    String value = (j < row.length) ? row[j] : ""; // 处理缺失数据

                    // 特别处理第 8 列 (TripInformation)
                    if (j == 7) {
                        value = value.split(";")[0]; // 截取分号之前的内容
                    }

                    rowData.put(VARIABLE_NAMES[j], value);
                }
                originalData.add(rowData);
            }

            // 如果数据超过 100 条，只显示前 100 条
            if (originalData.size() > 100) {
                model.addAttribute("warning", "数据量太大，只显示前 100 条数据");
                model.addAttribute("data", originalData.subList(0, 100));
            } else {
                model.addAttribute("data", originalData);
            }

            model.addAttribute("totalPages", 1); // 默认展示 1 页数据
            model.addAttribute("currentPage", 1); // 当前页码
        } catch (Exception e) {
            model.addAttribute("error", "文件上传失败，请检查文件格式！");
        }

        return "index"; // 返回上传页面并显示数据
    }

    @PostMapping("/sort")
    public String sortData(@RequestParam("column") String column, @RequestParam("order") String order, Model model) {
        List<Map<String, String>> dataToSort = new ArrayList<>(originalData); // 获取原始数据的副本

        // 判断排序方式，升序为 false，降序为 true
        boolean descending = "desc".equals(order);

        try {
            long startTime = System.currentTimeMillis(); // 开始计时

            switch (column) {
                case "VehicleType":
                case "TripLength": {
                    if (column.equals("TripLength")) {
                        // TripLength 是浮动数
                        List<Map<String, String>> sortedData = new ArrayList<>(dataToSort);
                        sortedData.sort((row1, row2) -> {
                            Double val1 = parseDouble(row1.get(column));
                            Double val2 = parseDouble(row2.get(column));
                            return descending ? val2.compareTo(val1) : val1.compareTo(val2);
                        });

                        dataToSort = sortedData; // 更新排序后的数据
                    } else {
                        // VehicleType 是整数
                        List<Map<String, String>> sortedData = new ArrayList<>(dataToSort);
                        sortedData.sort((row1, row2) -> {
                            Integer val1 = parseInt(row1.get(column));
                            Integer val2 = parseInt(row2.get(column));
                            return descending ? val2.compareTo(val1) : val1.compareTo(val2);
                        });

                        dataToSort = sortedData; // 更新排序后的数据
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

                    dataToSort = sortedData; // 更新排序后的数据
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

                    dataToSort = sortedData; // 更新排序后的数据
                    break;
                }

                default:
                    throw new IllegalArgumentException("不支持的列名: " + column);
            }


            sortedData = new ArrayList<>(dataToSort);

            long endTime = System.currentTimeMillis(); // 结束计时
            model.addAttribute("sortTime", endTime - startTime); // 排序耗时
            model.addAttribute("currentColumn", column); // 添加当前排序列的信息
            if(descending){
                model.addAttribute("descending","降序" );
            }
            else{
                model.addAttribute("descending","正序" );
            }


            // 如果排序后的数据超过 100 条，只显示前 100 条
            if (dataToSort.size() > 100) {
                model.addAttribute("warning", "数据量太大，只显示前 100 条数据");
                model.addAttribute("data", dataToSort.subList(0, 100));
            } else {
                model.addAttribute("data", dataToSort);
            }

        } catch (RuntimeException e) {
            model.addAttribute("error", "排序失败：" + e.getMessage());
        }

        return "index"; // 返回上传页面并展示排序后的数据
    }

    // 辅助方法：将字符串解析为 Double
    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.replace("\uFEFF", "").trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    // 辅助方法：将字符串解析为 Integer
    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value.replace("\uFEFF", "").trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    // 辅助方法：将字符串解析为 Long（用于日期）
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
        // 确保搜索的数据基于原始数据，而非已经排序或筛选过的数据
        List<Map<String, String>> dataToSearch = new ArrayList<>(originalData); // originalData 为原始数据的全局变量
        List<Map<String, String>> filteredData = new ArrayList<>();

        try {
            long startTime = System.currentTimeMillis(); // 开始计时

            // 遍历数据，查找包含关键词的记录（忽略大小写）
            for (Map<String, String> row : dataToSearch) {
                String value = row.get(column);
                if (value != null && value.toLowerCase().contains(keyword.toLowerCase())) {
                    filteredData.add(row); // 匹配关键词的记录
                }
            }

            // 搜索完成后的结果处理
            if (filteredData.isEmpty()) {
                model.addAttribute("info", "未找到匹配数据，显示上传的原始数据");
                model.addAttribute("data", dataToSearch.subList(0, Math.min(dataToSearch.size(), 100))); // 显示原始数据的前 100 条
            } else {
                if (filteredData.size() > 100) {
                    model.addAttribute("warning", "搜索结果超过 100 条，仅显示前 100 条");
                    model.addAttribute("data", filteredData.subList(0, 100));
                } else {
                    model.addAttribute("data", filteredData);
                }
                model.addAttribute("searchResultsCount", filteredData.size()); // 搜索结果总数
            }

            // 将搜索结果存储到新的数据集合中，以便后续导出使用
            searchResults = filteredData; // 这里将搜索结果保存到全局变量 searchResults 中

            long endTime = System.currentTimeMillis(); // 结束计时
            model.addAttribute("searchTime", endTime - startTime); // 搜索耗时
            model.addAttribute("currentColumn", column); // 当前搜索列
            model.addAttribute("searchKeyword", keyword); // 当前搜索关键词

        } catch (Exception e) {
            model.addAttribute("error", "搜索失败：" + e.getMessage());
        }

        return "index"; // 返回页面
    }



    @PostMapping("/export")
    public ResponseEntity<?> exportData(@RequestParam(value = "type", required = false) String type) {
        List<Map<String, String>> dataToExport;

        // 根据 type 参数确定导出排序数据还是搜索数据
        if ("search".equalsIgnoreCase(type)) {
            dataToExport = searchResults; // searchResults 为全局变量，保存最新搜索结果
            if (dataToExport == null || dataToExport.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("当前没有搜索结果，无法导出！");
            }
        } else {
            dataToExport = sortedData; // sortedData 为全局变量，保存最新排序结果
            if (dataToExport == null || dataToExport.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("当前没有排序后的数据，无法导出！");
            }
        }

        try {
            // 生成 CSV 内容
            StringBuilder csvContent = new StringBuilder();
            List<String> csvHeaders = new ArrayList<>(dataToExport.get(0).keySet());
            csvContent.append(String.join(",", csvHeaders)).append("\n");

            for (Map<String, String> row : dataToExport) {
                List<String> rowData = csvHeaders.stream()
                        .map(header -> row.getOrDefault(header, ""))
                        .collect(Collectors.toList());
                csvContent.append(String.join(",", rowData)).append("\n");
            }

            // 设置响应头
            HttpHeaders responseHeaders = new HttpHeaders();
            String filename = "search".equalsIgnoreCase(type) ? "search_results.csv" : "sorted_data.csv";
            responseHeaders.add("Content-Disposition", "attachment; filename=" + filename);
            responseHeaders.add("Content-Type", "text/csv; charset=UTF-8");

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(csvContent.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("导出失败：" + e.getMessage());
        }
    }


}
