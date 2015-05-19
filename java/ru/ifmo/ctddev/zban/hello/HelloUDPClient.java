package ru.ifmo.ctddev.zban.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by izban on 18.05.15.
 */
public class HelloUDPClient implements HelloClient {
    @Override
    public void start(String host, int port, String prefix, int requests, int threads) {
        ExecutorService service = Executors.newFixedThreadPool(threads);
        for (int it = 0; it < threads; it++) {
            final int threadId = it;
            service.submit(() -> {
                try (final DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(100);
                    int sent = 0;
                    while (sent < requests) {
                        try {
                            String message = prefix + threadId + "_" + sent;
                            System.out.println(message);
                            byte[] data = message.getBytes("UTF-8");
                            socket.send(new DatagramPacket(data, data.length, InetAddress.getByName(host), port));
                            byte[] temp = new byte[socket.getReceiveBufferSize()];
                            DatagramPacket packet = new DatagramPacket(temp, temp.length);
                            socket.receive(packet);
                            String s = new String(packet.getData(), 0, packet.getLength(), Charset.forName("UTF-8"));
                            if (!s.equals("Hello, " + message)) {
                                continue;
                            }
                            System.out.println(s);
                            sent++;
                        } catch (IOException ignored) {
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        service.shutdown();
        try {
            service.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            service.shutdownNow();
        }
    }
}
