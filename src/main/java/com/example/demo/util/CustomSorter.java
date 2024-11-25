package com.example.demo.util;

import java.util.Arrays;

public class CustomSorter {

    // Counting Sort Implementation
    public static class CountingSort {
        public static void countingSort(int[] data, boolean descending) {
            int max = Arrays.stream(data).max().orElse(0);
            int min = Arrays.stream(data).min().orElse(0);
            int range = max - min + 1;

            int[] count = new int[range];
            int[] output = new int[data.length];

            for (int value : data) {
                count[value - min]++;
            }
            for (int i = 1; i < count.length; i++) {
                count[i] += count[i - 1];
            }
            for (int i = data.length - 1; i >= 0; i--) {
                output[count[data[i] - min] - 1] = data[i];
                count[data[i] - min]--;
            }
            System.arraycopy(output, 0, data, 0, data.length);

            // Reverse the array if descending order
            if (descending) {
                reverseArray(data);
            }
        }

        private static void reverseArray(int[] array) {
            int start = 0;
            int end = array.length - 1;
            while (start < end) {
                int temp = array[start];
                array[start] = array[end];
                array[end] = temp;
                start++;
                end--;
            }
        }
    }

    // Quick Sort Implementation (Generic)
    public static class QuickSort {
        public static <T extends Comparable<T>> void quickSort(T[] arr, int low, int high, boolean descending) {
            if (low < high) {
                int pi = partition(arr, low, high, descending);
                quickSort(arr, low, pi - 1, descending);
                quickSort(arr, pi + 1, high, descending);
            }
        }

        private static <T extends Comparable<T>> int partition(T[] arr, int low, int high, boolean descending) {
            T pivot = arr[high];
            int i = low - 1;
            for (int j = low; j < high; j++) {
                if (descending ? arr[j].compareTo(pivot) > 0 : arr[j].compareTo(pivot) <= 0) {
                    i++;
                    swap(arr, i, j);
                }
            }
            swap(arr, i + 1, high);
            return i + 1;
        }

        private static <T> void swap(T[] arr, int i, int j) {
            T temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    // Merge Sort Implementation (Generic)
    public static class MergeSort {
        public static <T extends Comparable<T>> void mergeSort(T[] arr, int left, int right, boolean descending) {
            if (left < right) {
                int mid = (left + right) / 2;
                mergeSort(arr, left, mid, descending);
                mergeSort(arr, mid + 1, right, descending);
                merge(arr, left, mid, right, descending);
            }
        }

        private static <T extends Comparable<T>> void merge(T[] arr, int left, int mid, int right, boolean descending) {
            int n1 = mid - left + 1;
            int n2 = right - mid;

            T[] L = Arrays.copyOfRange(arr, left, mid + 1);
            T[] R = Arrays.copyOfRange(arr, mid + 1, right + 1);

            int i = 0, j = 0, k = left;
            while (i < n1 && j < n2) {
                if (descending ? L[i].compareTo(R[j]) > 0 : L[i].compareTo(R[j]) <= 0) {
                    arr[k++] = L[i++];
                } else {
                    arr[k++] = R[j++];
                }
            }
            while (i < n1) arr[k++] = L[i++];
            while (j < n2) arr[k++] = R[j++];
        }
    }

    // Insertion Sort Implementation
    public static class InsertionSort {
        public static void insertionSort(int[] arr, int low, int high, boolean descending) {
            for (int i = low + 1; i <= high; i++) {
                int key = arr[i];
                int j = i - 1;
                while (j >= low && (descending ? arr[j] < key : arr[j] > key)) {
                    arr[j + 1] = arr[j];
                    j--;
                }
                arr[j + 1] = key;
            }
        }
    }
}
