package ru.ifmo.ctddev.zban.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {
    private static String format(int value, String path) {
        return String.format("%08x %s", Integer.toUnsignedLong(value), path);
    }

    private static final int MAX = 4096;
    private static final int p = 16_777_619;
    private static final int x0 = 0x811c9dc5;
    private static int calcHash(Path path) {
        int curHash = x0;
        try (InputStream inputStream = Files.newInputStream(path)) {
            byte[] bytes = new byte[MAX];
            int k;
            while ((k = inputStream.read(bytes)) != -1) {
                for (int i = 0; i < k; i++) {
                    curHash = (curHash * p) ^ (bytes[i] & 0xff);
                }
            }
        } catch (IOException e) {
            return 0;
        }
        return curHash;
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Invalid arguments");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[1]), StandardCharsets.UTF_8)) {
            String path1;
            while ((path1 = reader.readLine()) != null) {
                Path path = Paths.get(path1);
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        writer.write(format(calcHash(file), file.toString()));
                        writer.newLine();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        writer.write(format(0, file.toString()));
                        writer.newLine();
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (NoSuchFileException e) {
            System.err.println("File " + e.getMessage() + " doesn't exist");
        } catch (AccessDeniedException e) {
            System.err.println("Can't get access to file " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
}
