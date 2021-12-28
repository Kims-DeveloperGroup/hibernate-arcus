package com.devookim.hibernatearcus.test;

public class Solution {

    public static void main(String[] args) throws Exception {
        int[] problem1 = {1, 3, 6, 4, 1, 2};
        int answer1 = new Solution().solution(problem1);
        int[] problem2 = {-1, -3};
        int answer2 = new Solution().solution(problem2);
        System.out.println(answer1);
        System.out.println(answer2);
    }

    public int solution(int[] A) throws Exception {
        Group headGroup= null;
        Group tailGroup= null;

        for (int a: A) {
            if (a < 0) {
                continue;
            }
            Group newGroup = new Group(a);
            if (headGroup != null) {
                Group beforeGroup = null;
                Group currentGroup = headGroup;
                boolean inserted = false;
                while (currentGroup != null) {
                    switch (currentGroup.compare(a)) {
                        case -1:
                            if (currentGroup.nextGroup == null) {//append
                                currentGroup.nextGroup = newGroup;
                                newGroup.prevGroup = currentGroup;
                                tailGroup = newGroup;
                                inserted=true;
                            }
                            break;
                        case 0:
                            inserted=currentGroup.tryInsert(a);
                            if (currentGroup.nextGroup != null && currentGroup.nextGroup.min == (a+1)) {
                                currentGroup.max = currentGroup.nextGroup.max;
                                currentGroup.tail = currentGroup.nextGroup.tail;
                                Group nextGroup = currentGroup.nextGroup;
                                currentGroup.nextGroup = nextGroup.nextGroup;
                                if (nextGroup.nextGroup == null) {
                                    tailGroup = currentGroup;
                                }
                            } else if(beforeGroup != null &&  beforeGroup.max == (a+1)) {
                                currentGroup.head = beforeGroup.head;
                                currentGroup.min = beforeGroup.min;
                                currentGroup.prevGroup.prevGroup.nextGroup = currentGroup;
                                currentGroup.prevGroup = currentGroup.prevGroup.prevGroup;
                            }
                            break;
                        case 1://prepend
                            beforeGroup.nextGroup = newGroup;
                            newGroup.nextGroup = currentGroup;
                            currentGroup.prevGroup = newGroup;
                            inserted=true;
                            break;
                    }
                    beforeGroup = currentGroup;
                    currentGroup = currentGroup.nextGroup;
                    if (inserted) {
                        break;
                    }
                }
            } else {
                headGroup = tailGroup = newGroup;
            }
        }
        return headGroup != null ? headGroup.max +1 : 1;
    }

    class Group {
        public Number head;
        public Number tail;
        private int min;
        private int max;
        private Group nextGroup;
        private Group prevGroup;

        public Group(int initValue) {
            head = new Number(initValue);
            tail = head;
            min = max = initValue;
        }

        public int compare(int value) {
            if (this.min > value + 1) {
                return +1;
            } else if (this.max < value - 1) {
                return -1;
            }
            return 0;
        }

        public boolean tryInsert(int value) throws Exception {
            if (value + 1 == min) {
                prepend(value);
            } else if (value - 1 == max) {
                append(value);
            } else if(min <= value && max >= value) {
                insertInBetween(value);
            } else {
                return false;
            }
            return true;
        }

        private void insertInBetween(int value) {
            Number number = new Number(value);
            number.next = head.next;
            if (head.next == null) {
                tail = number;
            }
            head.next = number;
        }

        private void prepend(int value) {
            Number number = new Number(value);
            number.next = head;
            min = value;
            head = number;
        }

        private void append(int value) {
            Number number = new Number(value);
            tail.next= number;
            tail = number;
            max = value;
        }
    }

    class Number {
        public int value;
        private Number next;
        public Number(int value) {
            this.value = value;
        }

        public void setNext(Number next) {
            this.next = next;
        }
    }
}
