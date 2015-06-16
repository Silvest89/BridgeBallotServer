package eu.silvenia.bridgeballot.server;


import eu.silvenia.bridgeballot.network.Bridge;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class BridgeBallotServer{

    static final int PORT = 8010;
    public static HashMap<Integer, Bridge> bridgeMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println(HelperTools.getCurrentTimeStamp() + "Bridge Ballot Server Build 22");

        Database.getDataSource();
        System.out.println(HelperTools.getCurrentTimeStamp() + "MySQL Connection: Done");

        new Database().loadBridges();
        System.out.println(HelperTools.getCurrentTimeStamp() + "Loaded " + bridgeMap.size() + " bridges.");

        System.out.println(HelperTools.getCurrentTimeStamp() + "Starting server...");

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //.handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.softCachingResolver(ClassLoader.getSystemClassLoader())),
                                    new ClientHandler());
                        }
                    });

            System.out.println(HelperTools.getCurrentTimeStamp() + "Listening on " + PORT);
            // Bind and start to accept incoming connections.
            b.bind(PORT).sync().channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
