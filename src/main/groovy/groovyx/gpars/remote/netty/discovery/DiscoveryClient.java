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

import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.Promise;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DiscoveryClient {
    private final EventLoopGroup group;

    private final Bootstrap bootstrap;

    private final ChannelFuture channelFuture;

    private final ConcurrentMap<String, DataflowVariable<InetSocketAddress>> registeredPromises;

    public DiscoveryClient(final int broadcastPort) {
        registeredPromises = new ConcurrentHashMap<String, DataflowVariable<InetSocketAddress>>();

        group = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    protected void initChannel(DatagramChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new DiscoveryResponseDecoder());
                        pipeline.addLast("encoder", new DiscoveryRequestEncoder(broadcastPort));
                        pipeline.addLast("handler", new DiscoveryClientHandler(registeredPromises));
                    }
                });

        channelFuture = bootstrap.bind(0);
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

    public Promise<InetSocketAddress> ask(String actorUrl) {
        DataflowVariable<InetSocketAddress> promise = new DataflowVariable<InetSocketAddress>();
        registeredPromises.putIfAbsent(actorUrl, promise);
        final DiscoveryRequest request = new DiscoveryRequest(actorUrl);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().writeAndFlush(request);
            }
        });
        return registeredPromises.get(actorUrl);
    }
}
