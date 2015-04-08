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
 * Created by izban on 07.04.15.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private volatile boolean isStopped;
    final private Queue<Consumer<Void>> queue = new ArrayDeque<>();
    final private Thread threads[];

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

    @Override
    public void close() throws InterruptedException {
        isStopped = true;
        for (Thread thread : threads) {
            thread.interrupt();
            thread.join();
        }
    }
}
