package com.devookim.hibernatearcus.test;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

import java.nio.channels.SelectionKey;

public class ReactorNettyServerMain {

    public static void main(String[] args) {
        System.out.println("main: " + Thread.currentThread());
        DisposableServer server = TcpServer.create()
                .port(9999) // 서버가 사용할 포트
                .doOnConnection(conn -> { // 클라이언트 연결시 호출
                    System.out.println("ChannelId:" + conn.channel().id());
                    // conn: reactor.netty.Connection
                    conn.addHandler(new LineBasedFrameDecoder(1024));
                    conn.addHandler(new ChannelHandlerAdapter() {
                        @Override
                        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                            System.out.println(Thread.currentThread() + " " + "client added" + ctx.name());
                        }

                        @Override
                        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                            System.out.println(Thread.currentThread() + " " +"client removed");
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                throws Exception {
                            System.out.println("exception:" + cause.toString());
                            ctx.close();
                        }
                    });
                    conn.onReadIdle(1_000 * 60 * 30, () -> {
                        System.out.println("client read timeout");
                        conn.outbound().sendString(Mono.just("What are you doin?")
                                .doOnNext(msg -> System.out.println("sent on " + Thread.currentThread())))
                                ;
//                        conn.dispose();
                    });
                })
                .handle((in, out) -> // 연결된 커넥션에 대한 IN/OUT 처리
                        // reactor.netty (NettyInbound, NettyOutbound)
                        in.receive() // 데이터 읽기 선언, ByteBufFlux 리턴
                                .asString()  // 문자열로 변환 선언, Flux<String> 리턴
                                .flatMap(msg -> {
                                            System.out.println(Thread.currentThread() + " " + "doOnNext: " + msg);
                                            if (msg.equals("exit")) {
                                                return out.withConnection(conn -> conn.dispose());
                                            } else if (msg.equals("SHUTDOWN")) {
                                                return out;
                                            } else {
                                                return out.sendString(Mono.just("echo: " + msg + "\r\n")
                                                                .publishOn(Schedulers.single())
                                                        .doOnNext(m -> System.out.println("sent on " + Thread.currentThread())))
                                                        .sendString(Mono.just("async echo"));
                                            }
                                        }
                                )
                )
                .bind() // Mono<DisposableServer> 리턴
                .block();
        System.out.println("end");

        while (true) {
        }
    }
}
