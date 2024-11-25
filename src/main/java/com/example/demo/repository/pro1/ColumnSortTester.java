package com.example.demo.repository.pro1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class ColumnSortTester {

    public static void main(String[] args) {

        // Load Data from CSV，227569 lines
        int[] vehicleTypeData = loadData(0); // VehicleType
        int[] directionTimeData = loadData(1); // DirectionTime_O
        int[] gantryIDData = loadData(2); // GantryID_O
        int[] tripLengthData = loadData(5); // TripLength

        if (vehicleTypeData == null || directionTimeData == null || gantryIDData == null || tripLengthData == null) {
            System.out.println("Error!");
            return;
        }

        // Create different subsets for sizes
        int[][] vehicleTypeSubsets = createSubsets(vehicleTypeData);
        int[][] directionTimeSubsets = createSubsets(directionTimeData);
        int[][] gantryIDSubsets = createSubsets(gantryIDData);
        int[][] tripLengthSubsets = createSubsets(tripLengthData);

        // Select different sorting strategy
        System.out.println("Use Count Sort with VehicleType :");
        for (int[] subset : vehicleTypeSubsets) {
            runAndTimeSort(subset, "CountingSort");
        }

        System.out.println("\nUse Merge Sort DirectionTime_O :");
        for (int[] subset : directionTimeSubsets) {
            runAndTimeSort(subset, "MergeSort");
        }

        System.out.println("\nUse Quick Sort with GantryID_O :");
        for (int[] subset : gantryIDSubsets) {
            runAndTimeSort(subset, "QuickSort");
        }

        System.out.println("\nUse Merge Sort with TripLength :");
        for (int[] subset : tripLengthSubsets) {
            runAndTimeSort(subset, "MergeSort");
        }
    }

    // Loading the data
    private static int[] loadData(int columnIndex) {
        try (BufferedReader br = new BufferedReader(new FileReader("D:\\javaProject\\src\\ust\\TDCS_M06A_20231204_080000.csv"))) {
            String line;
            int[] data = new int[227569];
            int index = 0;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > columnIndex) {
                    try {
                        data[index++] = Integer.parseInt(values[columnIndex].trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return Arrays.copyOf(data, index);
        } catch (IOException e) {
            return null;
        }
    }

    // Create arrays for different sizes
    private static int[][] createSubsets(int[] data) {
        return new int[][]{
                data,
                Arrays.copyOfRange(data, 0, data.length / 2),
                Arrays.copyOfRange(data, 0, data.length / 3),
                Arrays.copyOfRange(data, 0, data.length / 4),
                Arrays.copyOfRange(data, 0, data.length / 5)
        };
    }

    // Choosing Sort Function
    private static void runAndTimeSort(int[] data, String sortType) {
        int[] sortData = data.clone();
        long startTime = System.currentTimeMillis();

        switch (sortType) {
            case "QuickSort":
                QuickSort.quickSort(sortData, 0, sortData.length - 1);
                break;
            case "MergeSort":
                MergeSort.mergeSort(sortData, 0, sortData.length - 1);
                break;
            case "InsertionSort":
                if (sortData.length <= 5000) {
                    InsertionSort.insertionSort(sortData, 0, sortData.length - 1);
                } else {
                    return;
                }
                break;
            case "CountingSort":
                CountingSort.countingSort(sortData);
                break;
            default:
                return;
        }

        long endTime = System.currentTimeMillis();
        System.out.println(sortType + " Sorting Time: (" + sortData.length + " Line): " + (endTime - startTime) + " ms");
    }

    // CountingSort
    static class CountingSort {
        public static void countingSort(int[] data) {
            // Find the max and the min
            int max = Arrays.stream(data).max().getAsInt();
            int min = Arrays.stream(data).min().getAsInt();
            int range = max - min + 1;

            int[] count = new int[range];
            int[] output = new int[data.length];

            // Count the time of each data
            for (int value : data) {
                count[value - min]++;
            }
            for (int i = 1; i < count.length; i++) {
                count[i] += count[i - 1];
            }

            // Construct the output array
            for (int i = data.length - 1; i >= 0; i--) {
                output[count[data[i] - min] - 1] = data[i];
                count[data[i] - min]--;
            }

            System.arraycopy(output, 0, data, 0, data.length);
        }
    }

    // QuickSort
    static class QuickSort {
        private static final int MAX_DEPTH = 1000; // Max Recursion Depth
        private static final Random random = new Random();

        public static void quickSort(int[] arr, int low, int high) {
            quickSort(arr, low, high, 0);
        }

        private static void quickSort(int[] arr, int low, int high, int depth) {
            if (depth > MAX_DEPTH) { // Change to Insertion Sort
                InsertionSort.insertionSort(arr, low, high);
                return;
            }
            if (low < high) {
                int pi = partition(arr, low, high);
                quickSort(arr, low, pi - 1, depth + 1);
                quickSort(arr, pi + 1, high, depth + 1);
            }
        }

        private static int partition(int[] arr, int low, int high) {
            int pivot = arr[high];
            int i = (low - 1);
            for (int j = low; j < high; j++) {
                if (arr[j] <= pivot) {
                    i++;
                    swap(arr, i, j);
                }
            }
            swap(arr, i + 1, high);
            return i + 1;
        }

        private static void swap(int[] arr, int i, int j) {
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    // InsertSort
    static class InsertionSort {
        public static void insertionSort(int[] arr, int low, int high) {
            for (int i = low + 1; i <= high; i++) {
                int key = arr[i];
                int j = i - 1;
                while (j >= low && arr[j] > key) {
                    arr[j + 1] = arr[j];
                    j = j - 1;
                }
                arr[j + 1] = key;
            }
        }
    }

    // Merge Sort
    static class MergeSort {
        public static void mergeSort(int[] arr, int left, int right) {
            if (left < right) {
                int mid = (left + right) / 2;
                mergeSort(arr, left, mid);
                mergeSort(arr, mid + 1, right);
                merge(arr, left, mid, right);
            }
        }

        private static void merge(int[] arr, int left, int mid, int right) {
            int n1 = mid - left + 1;
            int n2 = right - mid;
            int[] L = new int[n1];
            int[] R = new int[n2];

            System.arraycopy(arr, left, L, 0, n1);
            System.arraycopy(arr, mid + 1, R, 0, n2);

            int i = 0, j = 0, k = left;
            while (i < n1 && j < n2) {
                if (L[i] <= R[j]) {
                    arr[k] = L[i];
                    i++;
                } else {
                    arr[k] = R[j];
                    j++;
                }
                k++;
            }

            while (i < n1) arr[k++] = L[i++];
            while (j < n2) arr[k++] = R[j++];
        }
    }

}
