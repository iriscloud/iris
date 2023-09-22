package cn.iris.cloud.protocols.mqtt;

import io.netty.channel.EventLoopGroup;

public interface IServer {

	void run(EventLoopGroup bossGroup, EventLoopGroup workerGroup)
		throws Exception;

	void close() throws Exception;
}
