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

import groovyx.gpars.remote.BroadcastDiscovery;
import groovyx.gpars.remote.LocalHost;
import groovyx.gpars.remote.netty.ConnectListener;
import groovyx.gpars.remote.netty.NettyChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents client that connects to server
 * @see groovyx.gpars.remote.netty.NettyServer
 *
 * @author Rafal Slawik
 */
public class NettyClient {
    private final EventLoopGroup workerGroup;
    private final Bootstrap bootstrap;

    private ChannelFuture channelFuture;

    /**
     * Creates client that connect to server on specified host and port.
     * @param host the host where server listens on
     * @param port the port that server listens on
     */
    public NettyClient(LocalHost localHost, String host, int port, ConnectListener connectListener) {
        workerGroup = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyChannelInitializer(localHost, connectListener))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .remoteAddress(host, port);
    }

    /**
     * Connects the client to server
     * Note: method does not block
     */
    public void start() {
        if (channelFuture == null) {
            channelFuture = bootstrap.connect();
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    future.channel().closeFuture().addListener(f -> {
                        workerGroup.shutdownGracefully();
                    });
                }
            });
        }
    }

    /**
     * Closes connection to server
     * Note: method does not block
     */
    public void stop() {
        if (channelFuture == null) {
            throw new IllegalStateException("Client has not been started");
        }
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().close();
            }
        });
    }
}
