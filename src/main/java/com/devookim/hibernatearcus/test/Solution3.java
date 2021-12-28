package com.devookim.hibernatearcus.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Solution3 {

    public static void main(String[] args) {
        int[] problem = {4,2,1,3};
        int[] problem3 = {1,3,6,10,15};
        int[] problem1 = {3,8,-10,23,19,-4,-14,27};
        List<List<Integer>> answer = new Solution3().minimumAbsDifference(problem);
        List<List<Integer>> answer1 = new Solution3().minimumAbsDifference(problem1);
        List<List<Integer>> answer3 = new Solution3().minimumAbsDifference(problem3);
        LinkedList<Object> objects = new LinkedList<>();
        System.out.println();
    }
    public List<List<Integer>> minimumAbsDifference(int[] arr) {
        List<Integer> sorted = Arrays.stream(mergeSort(arr, 0, arr.length - 1)).boxed()
                .collect(Collectors.toList());

        int minDiff=-1;
        List<List<Integer>> ans = new ArrayList<>();
        for(int i=0; i < sorted.size() - 1; i++) {
            int curr=sorted.get(i);
            int next=sorted.get(i+1);
            int diff=Math.abs(curr - next);
            List<Integer> el= new ArrayList(2);
            el.add(curr);
            el.add(next);
            if(minDiff == -1){
                minDiff = diff;
                ans.add(el);
            } else if (diff <= minDiff) {
                if(diff < minDiff) {
                    ans.clear();
                }
                minDiff=diff;
                ans.add(el);
            }
        }
        return ans;
    }

    private int[] mergeSort(int[] arr, int begin, int end) {
        if(begin == end) {
            return new int[]{arr[begin]};
        }
        int middleIndex = begin + ((end - begin + 1)/2); //begin index of
        int[] half1 = mergeSort(arr, begin, middleIndex -1);
        int[] half2 = mergeSort(arr, middleIndex, end);

        int[] result = new int[end - begin + 1];

        int idx1=0;
        int idx2=0;
        int retIdx=0;
        while(idx1 < half1.length && idx2 < half2.length) {
            if(half1[idx1] <= half2[idx2]) {
                result[retIdx]=half1[idx1];
                idx1++;
            } else {
                result[retIdx]=half2[idx2];
                idx2++;
            }
            retIdx++;
        }

        while (idx1 < half1.length) {
            result[retIdx] = half1[idx1];
            idx1++;
            retIdx++;
        }

        while (idx2 < half2.length) {
            result[retIdx] = half2[idx2];
            idx2++;
            retIdx++;
        }
        return result;
    }
}
