package ru.ifmo.ctddev.zban.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class implements {@link info.kgeorgiy.java.advanced.concurrent.ListIP} interface.
 * Class can find minimum, maximum in list. It also can do map, map, filter, concat, all, any functions.
 *
 * It uses {@link ru.ifmo.ctddev.zban.concurrent.Monoid} in code.
 *
 * @author izban
 * @see ru.ifmo.ctddev.zban.concurrent.Monoid
 * @see info.kgeorgiy.java.advanced.concurrent.ListIP
 * @see info.kgeorgiy.java.advanced.mapper.ParallelMapper
 */
public class IterativeParallelism implements ListIP {
    private ParallelMapper parallelMapper;

    /**
     * Constructor from ParallelMapper
     *
     * @param parallelMapper mapper to use.
     */
    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    public IterativeParallelism() {
        this.parallelMapper = null;
    }

    /**
     * Class do a map and fold values.
     * <p>
     * It divides list to list.size / threads, with rather equal numbers of elements in each.
     * Each block is folded separately in new thread. If there is ParallelMapper, no new threads will be created,
     * all work will be done in ParallelMapper.
     * To fold class uses {@link ru.ifmo.ctddev.zban.concurrent.Monoid} conception.
     *
     * @param threads numbers of threads to use
     * @param values values to fold
     * @param function from T to U
     * @param monoid monoid to use
     * @param <T> input type
     * @param <U> output type
     * @return folded value
     * @throws InterruptedException if thread was interrupted
     */
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
        }

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

        if (parallelMapper == null) {
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
        } else {
            parallelMapper.map(new Function<Worker, Void>() {
                @Override
                public Void apply(Worker worker) {
                    worker.work();
                    return null;
                }
            }, Arrays.asList(workers));
        }

        Optional<U> accumulator = monoid.getId();
        for (Optional<U> value : results) {
            accumulator = monoid.op(accumulator, value);
        }
        return accumulator.isPresent() ? accumulator.get() : null;
    }

    /**
     * Concat string representations of values.
     *
     * @param threads number of threads to use
     * @param values values to concat
     * @return string representation of all values
     * @throws InterruptedException if thread was interrupted
     */
    @Override
    public String concat(int threads, List<?> values) throws InterruptedException {
        List<StringBuilder> a = fold(threads,
                values,
                o -> {
                    List<StringBuilder> result = new ArrayList<>();
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

    /**
     * Return list with elements which satisfy given predicate.
     *
     * @param threads number of threads to use
     * @param values values to check
     * @param predicate predicate to check
     * @param <T> type of elements
     * @return list with elements
     * @throws InterruptedException if thread was interrupted
     */
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

    /**
     * Return list with elements after applying given function.
     *
     * @param threads number of threads to use
     * @param values values to map
     * @param f mapping function
     * @param <T> input type
     * @param <U> output type
     * @return mapped list
     * @throws InterruptedException if thread was interrupted
     */
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

    /**
     * Return maximal value between given values by given comparator.
     *
     * @param threads number of threads to use
     * @param values values to check
     * @param comparator comparator to check
     * @param <T> type of values
     * @return maximal value
     * @throws InterruptedException if thread was interrupted
     */
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

    /**
     * Return minimal value between given values by given comparator.
     *
     * @param threads number of threads to use
     * @param values values to check
     * @param comparator comparator to check
     * @param <T> type of values
     * @return minimal value
     * @throws InterruptedException if thread was interrupted
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Check if all given elements satisfy given predicate.
     *
     * @param threads number of threads to use
     * @param values elements to check
     * @param predicate predicate to check
     * @param <T> type of element
     * @return true if all elements satisfy given predicate, false otherwise
     * @throws InterruptedException if thread was interrupted
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return fold(threads,
                values,
                predicate::test,
                new Monoid<Boolean>((b1, b2) -> b1 && b2));
    }

    /**
     * Check if any of given elements satisfy given predicate.
     *
     * @param threads number of threads to use
     * @param values elements to check
     * @param predicate predicate to check
     * @param <T> type of element
     * @return true if any of elements satisfy given predicate, false otherwise
     * @throws InterruptedException if thread was interrupted
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
