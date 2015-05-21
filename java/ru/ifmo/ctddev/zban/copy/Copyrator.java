package ru.ifmo.ctddev.zban.copy;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by izban on 20.05.15.
 */
public class Copyrator {
    static private final int BUF_SIZE = 16384;

    public final AtomicLong[] sizes = {new AtomicLong(0), new AtomicLong(0)};

    public Thread thread;
    public AtomicBoolean running = new AtomicBoolean(true);

    public Copyrator() {
    }

    private void copy(Path file, Path from, Path to) {
        Path destination = Paths.get(to.toString() + "/" + from.relativize(file).toString());

        try {
            Files.deleteIfExists(destination);
            Files.createDirectories(destination.getParent());
            Files.createFile(destination);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (InputStream reader = Files.newInputStream(file);
             OutputStream writer = Files.newOutputStream(destination)) {
            byte[] buffer = new byte[BUF_SIZE];
            int readed;
            while ((readed = reader.read(buffer)) != -1) {
                if (!running.get()) {
                    break;
                }
                writer.write(buffer, 0, readed);
                sizes[0].addAndGet(readed);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copy(String _from, String _to) {
        running.set(true);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Path from = Paths.get(_from);
                Path to = Paths.get(_to);
                try {
                    Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (!running.get()) {
                                return FileVisitResult.TERMINATE;
                            }
                            sizes[1].addAndGet(Files.size(file));
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    if (sizes[1].longValue() < 1L) {
                        sizes[1].set(1L);
                    }

                    Files.walkFileTree(from, new SimpleFileVisitor<Path>(){
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (!running.get()) {
                                return FileVisitResult.TERMINATE;
                            }
                            copy(file, from, to);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                running.set(false);
            }
        });
        thread.start();
    }
}
