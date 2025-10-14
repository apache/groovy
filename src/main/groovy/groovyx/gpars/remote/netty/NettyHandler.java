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
import groovyx.gpars.serial.SerialMsg;

import static io.netty.channel.ChannelHandler.Sharable;

import io.netty.channel.*;

import java.util.List;

/**
 * @author Alex Tkachman, Rafal Slawik
 */
public class NettyHandler extends ChannelInboundHandlerAdapter {
    private final RemoteConnection remoteConnection;

    public NettyHandler(RemoteConnection remoteConnection) {
        this.remoteConnection = remoteConnection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        remoteConnection.onConnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        remoteConnection.onDisconnect();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.err.println("Message received: " + msg);
        if (msg instanceof SerialMsg) {
            ((SerialMsg) msg).execute(remoteConnection);
        }
    }
}
