package com.test.tcpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * 启动入口
 * @author lianliang
 */
public class Program {

	//private static String IP = "";
	private static int PORT = 8999;
	private static int TIMEOUT = 5 * 60;//5分钟，如果5分钟没有接收到数据。服务器主动断连
	private static int BACKLOG = 100;

	public static void main(String[] args) {

		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, BACKLOG)
			.option(ChannelOption.SO_TIMEOUT, TIMEOUT)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			.handler(new LoggingHandler(LogLevel.ERROR))
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast(new ReadTimeoutHandler(TIMEOUT));
					p.addLast(new WriteTimeoutHandler(TIMEOUT));
					p.addLast(new NettyServerHandler());
					p.addLast("bytesDecoder", new ByteArrayDecoder());
					p.addLast("bytesEncoder", new ByteArrayEncoder());
				}
			});

		try {
			// Start the server. on localhost
			ChannelFuture f = b.bind(PORT).sync();
			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}
}
