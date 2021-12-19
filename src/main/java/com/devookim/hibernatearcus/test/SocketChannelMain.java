package com.devookim.hibernatearcus.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class SocketChannelMain {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 3000));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(256);

        while (true) {
            System.out.println("selected: " + selector.select());
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            System.out.println("keySize: " + selectedKeys.size());
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                System.out.println("key: " + key.readyOps());
                if (key.isAcceptable()) {
                    SocketChannel channel = serverSocket.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                    System.out.println("new client connected...");
                }
                if (key.isWritable()) {
                    System.out.println("writable");
                    SocketChannel ch = (SocketChannel) key.channel();
                    String returnMsg = "Nice";
                    buffer.get(returnMsg.getBytes(StandardCharsets.UTF_8));
                    buffer.flip();
                    ch.write(buffer);
                    buffer.clear();
                }
                if (key.isReadable()) {
                    System.out.println("key readable");
                    SocketChannel client = (SocketChannel) key.channel();
                    client.read(buffer);
                    String msg = new String(buffer.array()).trim();
                    if (msg.equals("EXIT")) {
                        client.close();
                        System.out.println("Not accepting client messages anymore");
                    } else {
                        System.out.println("MSG" + msg);
                    }

                    buffer.flip();
                    client.write(buffer);
                    buffer.clear();
                }
                iter.remove();
            }
        }
    }
}
