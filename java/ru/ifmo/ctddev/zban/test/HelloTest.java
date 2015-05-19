package ru.ifmo.ctddev.zban.test;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import ru.ifmo.ctddev.zban.hello.HelloUDPClient;
import ru.ifmo.ctddev.zban.hello.HelloUDPServer;

/**
 * Created by izban on 19.05.15.
 */
public class HelloTest {
    static void test1() {
        System.out.println("test 1:");

        HelloUDPClient client = new HelloUDPClient();
        HelloUDPServer server = new HelloUDPServer();
        server.start(59893, 2);
        client.start("localhost", 59893, "lol", 2, 2);
        server.close();

        System.out.println("test 1 is ended");
    }

    public static void main(String[] args) {
        test1();
    }
}
