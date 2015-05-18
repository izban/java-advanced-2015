package ru.ifmo.ctddev.zban.test;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.ReplayDownloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import ru.ifmo.ctddev.zban.crawler.WebCrawler;

import java.io.File;
import java.io.IOException;

/**
 * Created by izban on 18.05.15.
 */
public class CrawlerTest {
    static void test1() {
        System.out.println("test 1:");
        try {
            WebCrawler crawler = new WebCrawler(new CachingDownloader(new File("./temp/")), 20, 20, 20);
            Result result = crawler.download("http://neerc.ifmo.ru/trains/information/index.html", 2);
            for (String s : result.getDownloaded()) {
                System.out.println(s);
            }
            System.out.println(result.getErrors().size());
            for (String s : result.getErrors().keySet()) {
                System.out.println(s);
            }
            crawler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("test 1 completed");
    }

    static void test2() {
        System.out.println("test 2:");
        try {
            WebCrawler crawler = new WebCrawler(new CachingDownloader(new File("./temp/")), 20, 20, 20);
            Result result = crawler.download("http://vk.com/wall991549_147?post_add", 1);
            for (String s : result.getDownloaded()) {
                System.out.println(s);
            }
            System.out.println(result.getErrors().size());
            for (String s : result.getErrors().keySet()) {
                System.out.println(s);
            }
            crawler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("test 2 completed");
    }

    static void test3() {
        System.out.println("test 3:");
        try {
            WebCrawler crawler = new WebCrawler(new CachingDownloader(new File("./temp/")), 20, 20, 20);
            Result result = crawler.download("http://www.kgeorgiy.info", 2);
            for (String s : result.getDownloaded()) {
                System.out.println(s);
            }
            System.out.println(result.getErrors().size());
            for (String s : result.getErrors().keySet()) {
                System.out.println(s);
            }
            System.out.println("GOSHA:");
            for (String s : new ReplayDownloader("http://www.kgeorgiy.info", 2, 10, 10).expected(2).getDownloaded()) {
                System.out.println(s);
            }
            crawler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("test 3 completed");
    }

    static void test4() {
        System.out.println("test 4:");
        try {
            WebCrawler crawler = new WebCrawler(new CachingDownloader(new File("./temp/")), 20, 20, 20);
            Result result = crawler.download("http://validator.w3.org/check?uri=referer", 1);
            for (String s : result.getDownloaded()) {
                System.out.println(s);
            }
            System.out.println(result.getErrors().size());
            for (String s : result.getErrors().keySet()) {
                System.out.println(s);
            }
            crawler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("test 4 completed");
    }

    public static void main(String[] args) throws IOException {
        test1();
        test2();
        test3();
        test4();
    }
}
