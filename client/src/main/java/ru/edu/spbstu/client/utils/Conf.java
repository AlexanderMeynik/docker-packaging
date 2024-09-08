package ru.edu.spbstu.client.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Conf {

    public static Boolean isRunningInsideDocker() {

            try (Stream< String > stream =
                         Files.lines(Paths.get("/proc/1/cgroup"))) {
                return stream.anyMatch(line -> line.contains("/docker"));
            } catch (IOException e) {
                return false;
            }
    }

    public static  String ip="http://"+((isRunningInsideDocker())?"serverjar":"localhost");
}
