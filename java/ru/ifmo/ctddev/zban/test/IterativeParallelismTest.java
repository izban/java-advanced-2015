package ru.ifmo.ctddev.zban.test;

import ru.ifmo.ctddev.zban.concurrent.IterativeParallelism;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by izban on 07.04.15.
 */
public class IterativeParallelismTest {
    static void test1() {
        try {
            List<Integer> list = new ArrayList<>();
            list.add(3);
            list.add(-1);
            list.add(5);
            list.add(0);
            System.out.println(new IterativeParallelism().maximum(2, list, Comparator.<Integer>naturalOrder()));
            System.out.println(new IterativeParallelism().map(2, list, a -> 2 * a));
            System.out.println(new IterativeParallelism().concat(2, list));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        test1();
    }
}
