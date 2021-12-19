package com.devookim.hibernatearcus.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChannelMain {

    public static void main(String[] args) throws IOException {
        InputStream inputStream = new FileInputStream(new File("test.txt"));
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        int read = readableByteChannel.read(byteBuffer);
//        byteBuffer.flip();
        System.out.println("read: " + read + " bytes");
        System.out.println(byteBuffer.array());
        System.out.println("read text: " + new String(byteBuffer.array(), StandardCharsets.UTF_8));
    }
}
