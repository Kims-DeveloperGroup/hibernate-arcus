package com.devookim.hibernatearcus.test;

import java.util.HashMap;
import java.util.Map;

public class Solution4 {

    public static void main(String[] args) {
        int[] problem1= {3, 2 ,4};
        int target1= 6;
        int[] solution1 = new Solution4().twoSum(problem1, target1);
        System.out.println("");
    }

    public int[] twoSum(int[] nums, int target) {

        //Set에 값을 넣어둔다
        //for-loop 안에서 set.contains(target-nums[i])를 조회
        //없으면 다음으로
        Map<Integer, Integer> map = new HashMap();
        for(int i=0;i < nums.length; i++) {
            map.put(nums[i], i);
        }

        for(int i=0; i < nums.length; i++) {
            if(map.containsKey(target- nums[i]) && map.get(target-nums[i]) != i) {
                return new int[]{i, map.get(target-nums[i])};
            }
        }
        return null;
    }
}
