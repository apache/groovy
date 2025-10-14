// GPars - Groovy Parallel Systems
//
// Copyright © 2014  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.remote.netty.discovery;

import groovyx.gpars.remote.RemotingContextWithUrls;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * Represents a server that waits for {@link groovyx.gpars.remote.netty.discovery.DiscoveryRequest}s.
 */
public class DiscoveryServer {
    public static final int DEFAULT_BROADCAST_PORT = 11553;

    private final EventLoopGroup group;

    private final Bootstrap bootstrap;

    private final ChannelFuture channelFuture;

    public DiscoveryServer(int broadcastPort, final InetSocketAddress serverSocketAddress, final RemotingContextWithUrls remotingContext) {
        group = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new DiscoveryRequestDecoder());
                        pipeline.addLast("encoder", new DiscoveryResponseWithRecipientEncoder());
                        pipeline.addLast("handler", new DiscoveryServerHandler(serverSocketAddress, remotingContext));
                    }
                });

        channelFuture = bootstrap.bind(broadcastPort);
    }

    public void start() {
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().closeFuture().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        group.shutdownGracefully();
                    }
                });
            }
        });
    }

    public void stop() {
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().close();
            }
        });
    }
}
