package com.devookim.hibernatearcus.test;

import java.util.*;
import java.util.stream.Collectors;

public class Solution6 {
    public static void main(String[] args) {
        int j=1, k= 1;
        System.out.println();

        String input = "my.song.mp3 11b\ngreatSong.flac 1000b\nnot3.txt 5b\nvideo.mp4 200b\ngame.exe 100b\nmov!e.mkv 10000b";
//        System.out.println(new Solution6().solution2(input));
//        System.out.println(new Solution6().solution3(new int[]{3, 2, -2, 5, -3}));
        System.out.println(new Solution6().solution6(new int[]{6}, new int[]{6, 4, 2, 5, 4, 6}));
    }

    int solution6(int[] A, int[] B) {
        int n = A.length;
        int m = B.length;;
        Arrays.sort(A);
        Arrays.sort(B);
        int i = 0;
        for (int k = 0; k < n;) {
            if (i < m - 1 && B[i] < A[k])
                i += 1;
            else k++;
            if (A[k] == B[i])
                return A[k];
        }
        return -1;
    }

    public int solution3(int[] A) {
        // write your code in Java SE 8
        int max = 0;
        Set<Integer> minusTwins = new HashSet<>();
        Arrays.sort(A);

        for(int num: A) {
            if(num < 0) {
                minusTwins.add(num);
            } else if (num > 0 && minusTwins.contains(num)){
                max = num;
            }
        }
        return max;
    }

    public String solution2(String S) {
        String[] files = S.split(System.lineSeparator());
        Map<String, Integer> fileSummary = new HashMap<>();
        fileSummary.put("music", 0);
        fileSummary.put("image", 0);
        fileSummary.put("movie", 0);
        fileSummary.put("other", 0);

        Map<String, String> fileTypes = new HashMap<>();
        fileTypes.put("mp3", "music");
        fileTypes.put("flac", "music");
        fileTypes.put("acc", "music");

        fileTypes.put("avi", "movie");
        fileTypes.put("mkv", "movie");
        fileTypes.put("mp4", "movie");

        fileTypes.put("jpg", "image");
        fileTypes.put("bmp", "image");
        fileTypes.put("gif", "image");

        String fileName;

        String[] nameAndSize;
        for(String file: files) {
            nameAndSize= file.split(" ");
            fileName= nameAndSize[0];

            String[] nameAndExt = fileName.split("\\.");
            String ext = nameAndExt[nameAndExt.length - 1];

            final int size= Integer.parseInt(nameAndSize[1].replace("b", ""));
            fileSummary.compute(fileTypes.getOrDefault(ext, "other"),
                    (k, v) -> v += size);
        }
        return fileSummary.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue() + "b\n")
                .collect(Collectors.joining());
    }

    public String solution(int U, int L, int[] C) {
        int[] arrU= new int[C.length];
        int[] arrL= new int[C.length];

        StringBuilder builderU= new StringBuilder();
        StringBuilder builderL= new StringBuilder();

        for(int i =0; i < C.length; i++) {
            int remain= C[i];
            if(remain == 0) {
                builderL.append("0");
                builderU.append("0");
            }
            while(remain > 0) {
                if(U > 0){
                    builderU.append("1");
                    U--;
                } else {
                    builderL.append("1");
                    L--;
                }
                remain--;
            }
        }
        return builderU + "," + builderL;
    }

    public int majorityElement(int[] nums) {
        int majorCount = nums.length/2 + 1;
        HashMap<Integer, Integer> counts= countNums(nums);
        int majorNum= Integer.MIN_VALUE;
        Iterator<Map.Entry<Integer, Integer>>it =counts.entrySet().iterator();

        Map.Entry<Integer, Integer> entry;
        while(it.hasNext()) {
            entry = it.next();
            if(entry.getValue() >= majorCount) {
                majorNum = entry.getKey();
                break;
            }
        }
        return majorNum;
    }
    public HashMap<Integer, Integer> countNums(int[] nums) {
        HashMap<Integer, Integer> counts= new HashMap<>();

        for(int n: nums) {
            counts.compute(n, (k, v)->{
                if(v == null) {
                    return 1;
                } else {
                    return v+=1;
                }
            });
        }
        return counts;
    }

    public boolean isValid(String s) {

        if(s.length() % 2 != 0) {
            return false;
        }
        Stack<Character> open= new Stack();
        Queue<Character> close= new LinkedList();

        for(char c: s.toCharArray()) {
            if(c=='(' || c=='{' || c=='[') {
                open.push(c);
            } else {
                close.add(c);
                if(open.isEmpty()){
                    return false;
                }
                if((!close.isEmpty() && !open.isEmpty())
                        && ((close.peek() == ')' && open.peek() == '(')
                        ||(close.peek() =='}' && open.peek() =='{')
                        ||(close.peek() ==']' && open.peek() == '['))) {
                    open.pop();
                    close.remove();
                }
            }
        }

        char a;
        char b;
        if(open.size() != close.size()) {
            return false;
        }
        while(!open.isEmpty() && !close.isEmpty()) {
            a=open.pop();
            b=close.poll();

            if(
                    (a=='(' && b!=')')
                            || (a=='{' && b!='}')
                            || (a=='[' && b!=']')
            ) {
                return false;
            }
        }
        return true;
    }
}
