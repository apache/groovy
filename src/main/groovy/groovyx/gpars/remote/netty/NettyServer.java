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

package groovyx.gpars.remote.netty;

import groovyx.gpars.remote.LocalHost;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Represents a server that waits for connections from clients.
 * @see NettyClient
 *
 * @author Rafal Slawik
 */
public class NettyServer {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ServerBootstrap bootstrap;

    private ChannelFuture channelFuture;

    /**
     * Creates a server listening on specified address.
     */
    public NettyServer(LocalHost localHost, String address, int port, ConnectListener connectListener) {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyChannelInitializer(localHost, connectListener))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .localAddress(new InetSocketAddress(address, port));
    }

    /**
     * Starts the server.
     * Note: method does not block
     */
    public void start() {
        if (channelFuture == null) {
            channelFuture = bootstrap.bind();
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    future.channel().closeFuture().addListener(f -> {
                        bossGroup.shutdownGracefully();
                        workerGroup.shutdownGracefully();
                    });
                }
            });
        }
    }

    /**
     * Stops the server.
     * Note: method does not block
     */
    public void stop() {
        if (channelFuture == null) {
            throw new IllegalStateException("Server has not been started");
        }
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().close();
            }
        });
    }
}
