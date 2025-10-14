// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10, 2014  The original author or authors
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
import groovyx.gpars.remote.message.CloseConnectionMsg;
import groovyx.gpars.remote.message.HostIdMsg;
import groovyx.gpars.serial.SerialMsg;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection using Netty
 *
 * @author Alex Tkachman, Rafal Slawik
 */
public class NettyRemoteConnection extends RemoteConnection {
    private final Channel channel;
    private final ConnectListener connectListener;

    public NettyRemoteConnection(final LocalHost provider, final Channel channel, ConnectListener connectListener) {
        super(provider);
        this.channel = channel;
        this.connectListener = connectListener;
    }

    @Override
    public void write(SerialMsg msg) {
        if (channel.isActive()) {
            channel.writeAndFlush(msg);
        }
    }

    @Override
    public void disconnect() {
        channel.close();
    }

    @Override
    public void onConnect() {
        System.err.println(this + ".onConnect()");
        if (connectListener != null) {
            connectListener.onConnect(this);
        }
    }
}
