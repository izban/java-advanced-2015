package ru.ifmo.ctddev.zban.arrayset;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        arrayList.add(50);
        arrayList.add(70);
        arrayList.add(60);
        ArraySet<Integer> set = new ArraySet<Integer>(arrayList, new Comparator<Integer>() {
            @Override
            public int compare(Integer integer, Integer t1) {
                return Integer.compare(integer / 10, t1 / 10);
            }
        });
        System.err.println(set.ceiling(70));
        System.err.println(set.higher(55));
        System.err.println(set.descendingSet().higher(70));
        System.err.println(set.ceiling(71));
        System.err.println(set.ceiling(81));


        for (Integer a : set) {
            System.err.println(a);
        }
    }
}
