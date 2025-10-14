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

package groovyx.gpars.remote.netty.discovery;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.List;

public class DiscoveryRequestEncoder extends MessageToMessageEncoder<DiscoveryRequest> {
    private final int broadcastPort;

    public DiscoveryRequestEncoder(int broadcastPort) {
        this.broadcastPort = broadcastPort;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DiscoveryRequest msg, List<Object> out) throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer(msg.getActorUrl(), CharsetUtil.UTF_8);
        DatagramPacket packet = new DatagramPacket(buf, new InetSocketAddress("255.255.255.255", broadcastPort));
        out.add(packet);
    }
}
