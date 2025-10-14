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
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.remote.netty.NettyHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Sets up Netty's communication channel.
 *
 * @author Rafal Slawik
 */
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {
    protected final LocalHost localHost;
    protected final ConnectListener connectListener;

    public NettyChannelInitializer(LocalHost localHost, ConnectListener connectListener) {
        this.localHost = localHost;
        this.connectListener = connectListener;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        RemoteConnection remoteConnection = getRemoteConnection(channel);

        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("decoder", new RemoteObjectDecoder(remoteConnection));
        pipeline.addLast("encoder", new RemoteObjectEncoder(remoteConnection));

        pipeline.addLast("handler", new NettyHandler(remoteConnection));
    }

    protected RemoteConnection getRemoteConnection(Channel channel) {
        return new NettyRemoteConnection(localHost, channel, connectListener);
    }
}
