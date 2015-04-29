package info.kgeorgiy.java.advanced.hello;

import info.kgeorgiy.java.advanced.base.BaseTester;
import info.kgeorgiy.java.advanced.implementor.ClassImplementorTest;
import info.kgeorgiy.java.advanced.implementor.InterfaceImplementorTest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Tester extends BaseTester {
    public static void main(final String[] args) throws NoSuchAlgorithmException, IOException {
        new Tester()
                .add("server", HelloServerTest.class)
                .add("client", HelloClientTest.class)
                .run(args);
    }
}
