package ru.ifmo.ctddev.zban.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.*;

/**
 * This class implements {@link HelloServer}. It can be started at some port, and it will answer at any UDP-request
 * to this port backward with prefix "Hello, %message".
 *
 * @see info.kgeorgiy.java.advanced.hello.HelloServer
 * @author izban
 */
public class HelloUDPServer implements HelloServer {
    private final boolean[] running = new boolean[1 << 16];
    private final ConcurrentHashMap<Integer, ExecutorService> workers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, DatagramSocket> sockets = new ConcurrentHashMap<>();

    /**
     * Runs given number of threads at given port to answer any incoming UDP-packet.
     *
     * @param port port
     * @param threads threads
     */
    @Override
    public void start(int port, int threads) {
        synchronized (running) {
            if (running[port]) {
                throw new IllegalStateException("server is running at this port");
            }
            running[port] = true;
        }

        workers.put(port, Executors.newFixedThreadPool(threads));

        try {
            sockets.put(port, new DatagramSocket(port, InetAddress.getByName("localhost")));
            DatagramSocket socket = sockets.get(port);
            socket.setSoTimeout(100);
            for (int i = 0; i < threads; i++) {
                workers.get(port).submit(() -> {
                    byte[] buf;
                    try {
                        buf = new byte[socket.getReceiveBufferSize()];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        while (running[port]) {
                            try {
                                socket.receive(packet);
                                String message = new String(packet.getData(), 0, packet.getLength(), Charset.forName("UTF-8"));
                                String answer = "Hello, " + message;
                                byte[] data = answer.getBytes("UTF-8");
                                socket.send(new DatagramPacket(data,
                                        data.length,
                                        packet.getAddress(),
                                        packet.getPort()));
                            } catch (IOException ignored) {
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }


    /**
     * Closes server. After closing it can be reused.
     */
    @Override
    public synchronized void close() {
        for (int i = 0; i < running.length; i++) {
            running[i] = false;
        }
        workers.forEach((port, worker) -> {
            worker.shutdown();
            worker.shutdownNow();
        });
        workers.clear();
        sockets.forEach((port, socket) -> socket.close());
        sockets.clear();
    }
}
