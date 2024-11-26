package com.example.demo.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomSorter {

    // 对整行数据按指定列进行排序
    public static void sortRowsByColumn(List<Map<String, String>> data, String column, boolean descending) {
        // 使用 Comparator 来进行稳定排序
        data.sort((row1, row2) -> {
            // 获取该列的值
            String val1 = row1.get(column);
            String val2 = row2.get(column);

            // 处理 null 值的情况
            if (val1 == null && val2 == null) return 0;
            if (val1 == null) return descending ? 1 : -1;
            if (val2 == null) return descending ? -1 : 1;

            // 对比两个值，返回升序或降序结果
            int compareResult = val1.compareTo(val2);
            return descending ? -compareResult : compareResult;
        });
    }

    // Counting Sort (适用于数字列如 VehicleType)
    public static void countingSort(List<Map<String, String>> data, String column, boolean descending) {
        Integer[] values = data.stream()
                .map(row -> Integer.parseInt(row.get(column)))
                .toArray(Integer[]::new);

        int max = Arrays.stream(values).max(Integer::compare).orElse(0);
        int min = Arrays.stream(values).min(Integer::compare).orElse(0);
        int range = max - min + 1;

        Integer[] count = new Integer[range];
        Integer[] output = new Integer[values.length];

        Arrays.fill(count, 0);

        for (int value : values) {
            count[value - min]++;
        }
        for (int i = 1; i < count.length; i++) {
            count[i] += count[i - 1];
        }
        for (int i = values.length - 1; i >= 0; i--) {
            output[count[values[i] - min] - 1] = values[i];
            count[values[i] - min]--;
        }

        System.arraycopy(output, 0, values, 0, values.length);

        if (descending) {
            reverseArray(values);
        }

        for (int i = 0; i < values.length; i++) {
            data.get(i).put(column, String.valueOf(values[i]));
        }
    }

    private static void reverseArray(Integer[] array) {
        int start = 0;
        int end = array.length - 1;
        while (start < end) {
            Integer temp = array[start];
            array[start] = array[end];
            array[end] = temp;
            start++;
            end--;
        }
    }

    // Merge Sort (适用于浮动数列如 TripLength 和 DirectionTime)
    public static void mergeSort(List<Map<String, String>> data, String column, boolean descending) {
        data.sort((row1, row2) -> {
            Comparable val1 = parseValue(row1.get(column));
            Comparable val2 = parseValue(row2.get(column));
            return descending ? val2.compareTo(val1) : val1.compareTo(val2);
        });
    }

    private static Comparable parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return value; // If it's not a number, treat it as a string
        }
    }

    // QuickSort (适用于 GantryID_O 和 GantryID_D)
    private static final int INSERTION_SORT_THRESHOLD = 1000;

    public static void quickSort(List<Map<String, String>> data, String column, boolean descending) {
        quickSortHelper(data, column, 0, data.size() - 1, descending);
    }

    private static void quickSortHelper(List<Map<String, String>> data, String column, int low, int high, boolean descending) {
        if (high - low + 1 <= INSERTION_SORT_THRESHOLD) {
            insertionSort(data, column, low, high, descending);
        } else {
            if (low < high) {
                int pi = partition(data, column, low, high, descending);
                quickSortHelper(data, column, low, pi - 1, descending);
                quickSortHelper(data, column, pi + 1, high, descending);
            }
        }
    }

    private static int partition(List<Map<String, String>> data, String column, int low, int high, boolean descending) {
        String pivot = data.get(high).get(column);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            String currentValue = data.get(j).get(column);
            boolean condition = descending ? currentValue.compareTo(pivot) > 0 : currentValue.compareTo(pivot) < 0;
            if (condition) {
                i++;
                Collections.swap(data, i, j);
            }
        }
        Collections.swap(data, i + 1, high);
        return i + 1;
    }

    private static void insertionSort(List<Map<String, String>> data, String column, int low, int high, boolean descending) {
        for (int i = low + 1; i <= high; i++) {
            Map<String, String> key = data.get(i);
            int j = i - 1;

            String keyValue = key.get(column);
            while (j >= low && (descending ? data.get(j).get(column).compareTo(keyValue) < 0 : data.get(j).get(column).compareTo(keyValue) > 0)) {
                data.set(j + 1, data.get(j));
                j--;
            }
            data.set(j + 1, key);
        }
    }
}
