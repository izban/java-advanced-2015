package ru.ifmo.ctddev.zban.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by izban on 19.05.15.
 */
public class CopyUtils {
    static private void create(String file) {
        try {
            Path cpath = Paths.get(file);
            Files.createDirectories(cpath.getParent());
            Files.deleteIfExists(cpath);
            Path path = Files.createFile(cpath);

            final int MAXN = 300_000_000;
            byte[] data = new byte[MAXN];
            Random random = new Random();
            random.nextBytes(data);
            Files.write(path, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void createFile() {
        create("trash/in/deep/a");
        create("trash/in/deep/b");
    }
}
