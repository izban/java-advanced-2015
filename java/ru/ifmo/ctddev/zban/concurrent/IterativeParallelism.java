package ru.ifmo.ctddev.zban.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by izban on 07.04.15.
 */
public class IterativeParallelism implements ListIP {
    private <T, U> U fold(int threads, List<? extends T> values, Function<T, U> function, Monoid<U> monoid) throws InterruptedException {
        if (threads < 1) {
            throw new IllegalArgumentException("can't work with " + threads + " threads");
        }
        if (threads > values.size()) {
            threads = values.size();
        }

        Function<List<? extends T>, Optional<U>> ffold = list -> {
            Optional<U> accumulator = monoid.getId();
            for (T value : list) {
                accumulator = monoid.op(accumulator, Optional.of(function.apply(value)));
            }
            return accumulator;
        };

        Optional<U> results[] = new Optional[threads];
        class Worker {
            final int id;
            final List<? extends T> list;

            Worker(int id, List<? extends T> list) {
                this.id = id;
                this.list = list;
            }

            void work() {
                results[id] = ffold.apply(list);
            }
        };

        int block = values.size() / threads;
        Worker workers[] = new Worker[threads];
        int l = 0;
        for (int i = 0; i < threads; i++) {
            int r = l + block;
            if (i < values.size() % threads) {
                r++;
            }
            workers[i] = new Worker(i, values.subList(l, r));
            l = r;
        }

        Thread threadList[] = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int _id = i;
            threadList[i] = new Thread(new Runnable() {
                final int id = _id;
                @Override
                public void run() {
                    workers[id].work();
                }
            });
        }
        for (Thread thread : threadList) {
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }

        Optional<U> accumulator = monoid.getId();
        for (Optional<U> value : results) {
            accumulator = monoid.op(accumulator, value);
        }
        return accumulator.isPresent() ? accumulator.get() : null;
    }

    @Override
    public String concat(int threads, List<?> values) throws InterruptedException {
        List<StringBuilder> a = fold(threads,
                values,
                o -> {
                    List<StringBuilder> result = new ArrayList<StringBuilder>();
                    result.add(new StringBuilder(o.toString()));
                    return result;
                },
                Monoid.<StringBuilder>monoidConcatList());
        StringBuilder result = new StringBuilder();
        for (StringBuilder element : a) {
            result.append(element);
        }
        return result.toString();
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return fold(threads,
                values,
                t -> {
                    List<T> result = new ArrayList<T>();
                    if (predicate.test(t)) {
                        result.add(t);
                    }
                    return result;
                },
                Monoid.<T>monoidConcatList());
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return fold(threads,
                values,
                t -> {
                    List<U> result = new ArrayList<U>();
                    result.add(f.apply(t));
                    return result;
                },
                Monoid.<U>monoidConcatList());
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return fold(threads,
                values,
                Function.identity(),
                new Monoid<T>((t1, t2) -> {
                    if (t1 == null || t2 == null) {
                        throw new NullPointerException();
                    }
                    if (comparator.compare(t1, t2) >= 0) {
                        return t1;
                    }
                    return t2;
                }));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return fold(threads,
                values,
                predicate::test,
                new Monoid<Boolean>((b1, b2) -> b1 && b2));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
