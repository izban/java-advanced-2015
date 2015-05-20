package ru.ifmo.ctddev.zban.copy;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiFunction;

/**
 * Created by izban on 20.05.15.
 */
public class Copyrator {
    static private final int BUF_SIZE = 4096;

    private BiFunction<Long, Long, Void> function;
    private final long[] sizes = {0, 0};

    public boolean running = false;

    public Copyrator(BiFunction<Long, Long, Void> biFunction) {
        this.function = biFunction;
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
                if (!running) {
                    return;
                }
                writer.write(buffer, 0, readed);
                sizes[0] += readed;
                function.apply(sizes[0], sizes[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copy(String _from, String _to) {
        running = true;
        function.apply(0L, 1L);
        Path from = Paths.get(_from);
        Path to = Paths.get(_to);
        try {
            Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!running) {
                        return FileVisitResult.TERMINATE;
                    }
                    sizes[1] += Files.size(file);
                    return FileVisitResult.CONTINUE;
                }
            });
            sizes[1] = Math.max(sizes[1], 1L);

            Files.walkFileTree(from, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!running) {
                        return FileVisitResult.TERMINATE;
                    }
                    copy(file, from, to);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = false;
    }
}
