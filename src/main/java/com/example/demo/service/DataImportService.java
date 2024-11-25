package com.example.demo.service;

import com.example.demo.entity.TrafficData;
import com.example.demo.repository.TrafficDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataImportService {

    @Autowired
    private TrafficDataRepository trafficDataRepository;

    /**
     * 从文件流中导入数据，仅处理第一列的前 7 个字段
     */
    public void importDataFromReader(BufferedReader reader) throws IOException {
        List<TrafficData> trafficDataList = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            // 解析 CSV，每行只处理第一列
            String[] firstColumnFields = line.split(",", 8)[0].split(",", 7);
            if (firstColumnFields.length < 7) { // 确保有 7 个字段
                continue; // 跳过无效行
            }

            try {
                TrafficData trafficData = new TrafficData();
                trafficData.setId(Long.parseLong(firstColumnFields[0])); // ID
                trafficData.setStartTime(LocalDateTime.parse(firstColumnFields[1])); // 开始时间
                trafficData.setStartLocation(firstColumnFields[2]); // 起始位置
                trafficData.setEndTime(LocalDateTime.parse(firstColumnFields[3])); // 结束时间
                trafficData.setEndLocation(firstColumnFields[4]); // 结束位置
                trafficData.setDistance(new BigDecimal(firstColumnFields[5])); // 距离
                trafficData.setIsValid(firstColumnFields[6].equalsIgnoreCase("Y")); // 是否有效

                trafficDataList.add(trafficData);
            } catch (Exception e) {
                // 忽略无法解析的行
                System.err.println("Failed to parse line: " + line);
            }
        }

        // 批量保存数据
        trafficDataRepository.saveAll(trafficDataList);
    }
}
