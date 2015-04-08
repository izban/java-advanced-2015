package ru.ifmo.ctddev.zban.test;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import ru.ifmo.ctddev.zban.implementor.Implementor;

import javax.annotation.processing.Completions;
import javax.imageio.IIOException;
import javax.management.ImmutableDescriptor;
import javax.management.remote.rmi.RMIServerImpl;
import java.io.File;
import java.io.PrintWriter;

/**
 * Created by izban on 06.04.15.
 */
public class ImplementorTest {
    static void test(Class cls, File file) {
        try {
            new Implementor().implement(cls, file);
        } catch (ImplerException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws ImplerException {
        test(java.util.List.class, new File("trash"));
        test(java.util.ArrayList.class, new File("trash"));
        test(IIOException.class, new File("trash"));
        test(ImmutableDescriptor.class, new File("trash"));
        test(Completions.class, new File("trash"));
        test(RMIServerImpl.class, new File("trash"));
        test(PrintWriter.class, new File("trash"));
    }
}
