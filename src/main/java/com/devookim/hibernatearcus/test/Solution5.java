package com.devookim.hibernatearcus.test;

import javax.persistence.criteria.CriteriaBuilder;

public class Solution5 {
    public static void main(String[] args) {
        boolean palindrome = new Solution5().isPalindrome(121);
        System.out.println(palindrome);
    }
    public boolean isPalindrome(int x) {
        String xString = String.valueOf(x);
        int middleIdx = xString.length()/2;
        String reversed="";

        int end=end = middleIdx; //121 =>1 1212=>2
        for(int i=xString.length() -1;i >= end;i--) {
            reversed+=xString.charAt(i);
        }

        String prefix="";

        for(int j=0; j <= middleIdx; j++) {
            prefix+=xString.charAt(j);
        }
        return Integer.parseInt(prefix) == Integer.parseInt(reversed);
    }
}
