package com.example.demo.controller;

import com.example.demo.util.CustomSorter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FileController {

    private List<Map<String, String>> data = new ArrayList<>();
    private static final String[] VARIABLE_NAMES = {
            "VehicleType", "DirectionTime_O", "GantryID_O",
            "DirectionTime_D", "GantryID_D", "TripLength",
            "TripEnd", "TripInformation"
    };

    @RequestMapping("/file")
    public String filePage(Model model) {
        model.addAttribute("data", getPagedData(1, 500)); // 初始显示前500条数据
        model.addAttribute("totalPages", getTotalPages(500)); // 总页数
        model.addAttribute("currentPage", 1); // 当前页码
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        data.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> rawData = reader.lines()
                    .map(line -> line.split(","))
                    .collect(Collectors.toList());

            // 处理每一行数据
            for (int i = 0; i < rawData.size(); i++) {
                String[] row = rawData.get(i);

                // 仅保留前 8 列并赋予变量名
                Map<String, String> rowData = new LinkedHashMap<>();
                rowData.put("序号", String.valueOf(i + 1)); // 序号
                for (int j = 0; j < VARIABLE_NAMES.length; j++) {
                    String value = (j < row.length) ? row[j] : ""; // 处理可能的缺失数据

                    // 特别处理第 8 列 (TripInformation)
                    if (j == 7) {
                        value = value.split(";")[0]; // 截取分号之前的内容
                    }

                    rowData.put(VARIABLE_NAMES[j], value);
                }
                data.add(rowData);
            }

            // 仅返回前 500 条数据
            List<Map<String, String>> pagedData = getPagedData(1, 500);
            model.addAttribute("data", pagedData);
            model.addAttribute("totalPages", getTotalPages(500));
            model.addAttribute("currentPage", 1);

            if (data.size() > 500) {
                model.addAttribute("warning", "数据量较大，仅显示前 500 条记录！");
            }
        } catch (Exception e) {
            model.addAttribute("error", "文件上传失败，请检查文件格式！");
        }
        return "upload";
    }

    @PostMapping("/sort")
    public String sortData(@RequestParam("column") String column, @RequestParam("page") int page, Model model) {
        try {
            System.out.println("排序列: " + column);
            long startTime = System.currentTimeMillis(); // 开始计时

            switch (column) {
                case "VehicleType":
                case "TripLength": {
                    if (column.equals("TripLength")) {
                        // TripLength 是浮点数
                        Double[] doubleArray = data.stream()
                                .map(row -> row.get(column))
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(value -> !value.isEmpty())
                                .map(value -> {
                                    try {
                                        return Double.parseDouble(value.replace("\uFEFF", ""));
                                    } catch (NumberFormatException e) {
                                        throw new RuntimeException("无法解析为浮点数: " + value);
                                    }
                                })
                                .toArray(Double[]::new);

                        CustomSorter.MergeSort.mergeSort(doubleArray, 0, doubleArray.length - 1);

                        for (int i = 0; i < data.size(); i++) {
                            // 确保使用小数点分隔符
                            data.get(i).put(column, String.format("%.3f", doubleArray[i]).replace(",", "."));
                        }

                    } else {
                        // VehicleType 是整数
                        int[] intArray = data.stream()
                                .map(row -> row.get(column))
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(value -> !value.isEmpty())
                                .mapToInt(value -> {
                                    try {
                                        return Integer.parseInt(value.replace("\uFEFF", ""));
                                    } catch (NumberFormatException e) {
                                        throw new RuntimeException("无法解析为整数: " + value);
                                    }
                                })
                                .toArray();

                        CustomSorter.CountingSort.countingSort(intArray);

                        for (int i = 0; i < data.size(); i++) {
                            data.get(i).put(column, String.valueOf(intArray[i]));
                        }
                    }
                    break;
                }

                case "DirectionTime_O":
                case "DerectionTime_D": {
                    Long[] timestampArray = data.stream()
                            .map(row -> row.get(column))
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(value -> !value.isEmpty())
                            .map(value -> {
                                try {
                                    return java.time.LocalDateTime.parse(value, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toInstant()
                                            .toEpochMilli();
                                } catch (Exception e) {
                                    throw new RuntimeException("无法解析为日期时间: " + value);
                                }
                            })
                            .toArray(Long[]::new);

                    CustomSorter.MergeSort.mergeSort(timestampArray, 0, timestampArray.length - 1);

                    for (int i = 0; i < data.size(); i++) {
                        String formattedDate = java.time.Instant.ofEpochMilli(timestampArray[i])
                                .atZone(java.time.ZoneId.systemDefault())
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        data.get(i).put(column, formattedDate);
                    }
                    break;
                }

                case "GantryID_O":
                case "GantryID_D": {
                    String[] stringArray = data.stream()
                            .map(row -> row.get(column))
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(value -> !value.isEmpty())
                            .toArray(String[]::new);

                    CustomSorter.QuickSort.quickSort(stringArray, 0, stringArray.length - 1);

                    for (int i = 0; i < data.size(); i++) {
                        data.get(i).put(column, stringArray[i]);
                    }
                    break;
                }

                default:
                    throw new IllegalArgumentException("不支持的列名: " + column);
            }

            long endTime = System.currentTimeMillis(); // 结束计时
            model.addAttribute("sortTime", endTime - startTime); // 排序耗时

            // 仅返回前 500 条数据
            List<Map<String, String>> pagedData = getPagedData(1, 500);
            model.addAttribute("data", pagedData);
            model.addAttribute("totalPages", getTotalPages(500));
            model.addAttribute("currentPage", page);

            if (data.size() > 500) {
                model.addAttribute("warning", "数据量较大，仅显示前 500 条记录！");
            }

        } catch (RuntimeException e) {
            model.addAttribute("error", "排序失败：" + e.getMessage());
        }

        return "upload";
    }

    private List<Map<String, String>> getPagedData(int page, int pageSize) {
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, data.size());
        return data.subList(fromIndex, toIndex);
    }

    private int getTotalPages(int pageSize) {
        int effectiveSize = Math.min(data.size(), 500); // 限制最大返回数据为 500 条
        return (effectiveSize + pageSize - 1) / pageSize;
    }
}
