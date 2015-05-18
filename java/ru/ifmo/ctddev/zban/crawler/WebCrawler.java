package ru.ifmo.ctddev.zban.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;


/**
 * This class is concurent implementation of {@link info.kgeorgiy.java.advanced.crawler.Crawler} interface.
 * It can download your site recursively by BFS for needed depth.
 * <p>
 * Class can be created with different params: max downloaders threads, extractors thread, and max downloads from each
 * host at time.
 * <p>
 * Work is divided by two parts: downloading sites and parsing links from {@link Document}.
 * <p>
 * Class implements {@link AutoCloseable} interface, and it can't be used after it was stopped.
 * <p>
 * You need your copy of {@link info.kgeorgiy.java.advanced.crawler.Downloader} to use the class.
 * {@link info.kgeorgiy.java.advanced.crawler.CachingDownloader} is recommended.
 *
 * @author izban
 * @see info.kgeorgiy.java.advanced.crawler.Crawler
 * @see java.lang.AutoCloseable
 * @see ExecutorService
 * @see Downloader
 */
public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost, downloadsLimit, extractsLimit;
    private final static String USAGE = "interface: WebCrawler url [downloads [extractors [perHost]]]";

    /**
     * Constructor of class.
     *
     * @param downloader object which will download sites.
     * @param downloaders max downloaders threads count
     * @param extractors max extractors threads count
     * @param perHost max downloads from one host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadsLimit = downloaders;
        this.extractsLimit = extractors;
        this.perHost = perHost;
        this.downloaders = Executors.newFixedThreadPool(this.downloadsLimit);
        this.extractors = Executors.newFixedThreadPool(this.extractsLimit);
    }

    /**
     * Downloads site by BFS for depth distance.
     *
     * @param url site to download
     * @param depth depth for download
     * @return Result
     */
    public Result download(String url, int depth) {

        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<String, Boolean> visited = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, IOException> errors = new ConcurrentHashMap<>();

        queue.add(url);
        visited.put(url, true);

        for (int d = 0; d < depth; d++) {
            final int d0 = d;
            ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> hostQueue = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, Integer> semaphores = new ConcurrentHashMap<>();
            ConcurrentLinkedQueue<Future> pending = new ConcurrentLinkedQueue<>();

            ConcurrentLinkedQueue<String> queue1 = new ConcurrentLinkedQueue<>();
            for (String link : queue) {
                String host;
                try {
                    host = URLUtils.getHost(link);
                } catch (MalformedURLException e) {
                    continue;
                }

                synchronized (hostQueue) {
                    hostQueue.putIfAbsent(host, new ConcurrentLinkedQueue<>());
                    hostQueue.get(host).add(link);
                }

                synchronized (semaphores) {
                    semaphores.putIfAbsent(host, 0);
                    int count = semaphores.get(host);
                    if (count < perHost) {
                        semaphores.put(host, semaphores.get(host) + 1);
                        pending.add(downloaders.submit(() -> {
                            String cur = "";
                            try {
                                while (true) {
                                    cur = "";
                                    synchronized (hostQueue) {
                                        if (!hostQueue.get(host).isEmpty()) {
                                            cur = hostQueue.get(host).poll();
                                        }
                                    }
                                    if (!cur.equals("")) {
                                        final String curCopy = cur;
                                        Document document = downloader.download(cur);
                                        if (d0 + 1 == depth) {
                                            continue;
                                        }
                                        pending.add(extractors.submit(() -> {
                                            try {
                                                List<String> links = document.extractLinks();
                                                for (String newLink : links) {
                                                    synchronized (visited) {
                                                        visited.putIfAbsent(newLink, false);
                                                        if (!visited.get(newLink)) {
                                                            visited.put(newLink, true);
                                                            queue1.add(newLink);
                                                        }
                                                    }
                                                }
                                            } catch (IOException e) {
                                                errors.putIfAbsent(curCopy, e);
                                            }
                                        }));
                                    } else {
                                        synchronized (semaphores) {
                                            semaphores.put(host, semaphores.get(host) - 1);
                                        }
                                        return;
                                    }
                                }
                            } catch (IOException e) {
                                errors.putIfAbsent(cur, e);
                            }
                        }));
                    }
                }
            }
            while (!pending.isEmpty()) {
                Future f = pending.poll();
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            queue.clear();
            queue1.stream().distinct().forEach(queue::add);
        }

        for (String s : errors.keySet()) {
            visited.remove(s);
        }
        return new Result(new ArrayList<>(visited.keySet()), new HashMap<>(errors));
    }

    /**
     * Usage: WebCrawler url [downloads [extractors [perHost]]]
     * Prints all found link to stdout.
     *
     * @param args command line args
     */
    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 4) {
            System.err.println(USAGE);
            return;
        }
        String url;
        int downloaders = 10, extractors = 10, perHost = 10;
        try {
            url = args[0];
            if (args.length > 1) {
                downloaders = Integer.parseInt(args[1]);
            }
            if (args.length > 2) {
                extractors = Integer.parseInt(args[2]);
            }
            if (args.length > 3) {
                perHost = Integer.parseInt(args[3]);
            }
        } catch (NullPointerException | NumberFormatException e) {
            System.err.println(USAGE);
            return;
        }

        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(new File("./temp/")), downloaders, extractors, perHost)) {
            Result links = crawler.download(url, 1);
            for (String s : links.getDownloaded()) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close(ExecutorService service) {
        service.shutdown();
        if (!service.isShutdown()) {
            try {
                service.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                service.shutdownNow();
            }
        }
    }

    /**
     * Close object. It can't be used anymore after closing!
     */
    @Override
    public void close() {
        close(downloaders);
        close(extractors);
    }
}
