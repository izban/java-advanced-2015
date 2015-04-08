package ru.ifmo.ctddev.zban.test;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import ru.ifmo.ctddev.zban.concurrent.IterativeParallelism;
import ru.ifmo.ctddev.zban.mapper.ParallelMapperImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by izban on 08.04.15.
 */
public class ParallelMapperTest {
    static void test1() {
        try {
            ParallelMapper mapper = new ParallelMapperImpl(2);
            List<Integer> list = new ArrayList<>();
            list.add(3);
            list.add(-1);
            list.add(5);
            list.add(0);
            IterativeParallelism parallelism = new IterativeParallelism(mapper);
            System.out.println(parallelism.maximum(2, list, Comparator.<Integer>naturalOrder()));
            mapper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        test1();
    }
}
