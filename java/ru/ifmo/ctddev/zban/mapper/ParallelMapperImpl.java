package ru.ifmo.ctddev.zban.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class implements {@link info.kgeorgiy.java.advanced.mapper.ParallelMapper} interface.
 * It can do a parallel mapping of given arguments.
 * <p>
 * It creates Threads in constructor, and it ends them when {@code .close} is called.
 * After {@code .close} was called, it is prohibited to use this class.
 *
 * @author izban
 * @see info.kgeorgiy.java.advanced.mapper.ParallelMapper
 */
public class ParallelMapperImpl implements ParallelMapper {
    /**
     * Variable that indicates if class is alive.
     */
    private volatile boolean isStopped;

    /**
     * Queue with not proceeded tasks.
     */
    final private Queue<Consumer<Void>> queue = new ArrayDeque<>();

    /**
     * Available threads.
     */
    final private Thread threads[];


    /**
     * Constructor which gets a number of threads to use and creates them.
     * Each thread either works, executing some task, or sleep in passive waiting.
     * After {@code .close} method was called, each of threads will be destroyed.
     *
     * @param threads number of threads to use
     */
    public ParallelMapperImpl(int threads) {
        this.threads = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            this.threads[i] = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        if (isStopped) {
                            return;
                        }
                        Consumer<Void> action = null;
                        synchronized (queue) {
                            if (!queue.isEmpty()) {
                                action = queue.poll();
                            }
                        }
                        if (action == null) {
                            synchronized (queue) {
                                try {
                                    queue.wait();
                                    continue;
                                } catch (InterruptedException e) {
                                    return;
                                }
                            }
                        }
                        action.accept(null);
                    }
                }
            });
            this.threads[i].start();
        }
    }

    /**
     * Maps list of arguments with given function.
     * Each element from list is added to queue, where it will be executed.
     * When all elements from list will be proceeded, function returns list of mapped elements.
     *
     * @param f function to apply
     * @param args list with elements
     * @param <T> initial type
     * @param <R> result type
     * @return list with mapped elements
     * @throws InterruptedException if thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        if (isStopped) {
            throw new IllegalStateException("Mapper has already been closed");
        }
        List<R> result = new ArrayList<>();

        final AtomicInteger counter = new AtomicInteger(0);
        synchronized (queue) {
            for (int i = 0; i < args.size(); i++) {
                final int id = i;
                queue.add(new Consumer<Void>() {
                    @Override
                    public void accept(Void aVoid) {
                        R value = f.apply(args.get(id));
                        synchronized (result) {
                            result.set(id, value);
                        }
                        counter.incrementAndGet();
                        if (counter.get() == args.size()) {
                            synchronized (queue) {
                                queue.notifyAll();
                            }
                        }
                    }
                });
                result.add(null);
            }
            queue.notifyAll();
        }
        synchronized (queue) {
            while (counter.get() < args.size()) {
                queue.wait();
            }
        }

        return result;
    }

    /**
     * Close all threads and stop all works being done.
     *
     * @throws InterruptedException if thread was interrupted.
     */
    @Override
    public void close() throws InterruptedException {
        isStopped = true;
        for (Thread thread : threads) {
            thread.interrupt();
            thread.join();
        }
    }
}
