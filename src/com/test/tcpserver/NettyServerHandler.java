package com.test.tcpserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * netty´¦ÀíÆ÷
 */
@Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
	
	private ParseProtocolData protocolParser = new ParseProtocolData(new ParseProtocolDataAfterImpl());
	
	@Override
	public void channelRead(ChannelHandlerContext context, Object obj) throws Exception {
		try {
			//System.err.println("sssssss");
			ByteBuf buff = (ByteBuf) obj;
			protocolParser.parse(context.channel(), buff);
		} finally {
			ReferenceCountUtil.release(obj);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext context) throws Exception {

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable thx) throws Exception {
		thx.printStackTrace();
		context.close();
	}
}
