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

import java.util.List;

public class DiscoveryResponseWithRecipientEncoder extends MessageToMessageEncoder<DiscoveryResponseWithRecipient> {
    @Override
    protected void encode(ChannelHandlerContext ctx, DiscoveryResponseWithRecipient msg, List<Object> out) throws Exception {
        DiscoveryResponse response = msg.getResponse();
        ByteBuf portBuf = Unpooled.copyInt(response.getServerSocketAddress().getPort());
        ByteBuf urlBuf = Unpooled.copiedBuffer(response.getActorUrl(), CharsetUtil.UTF_8);

        DatagramPacket packet = new DatagramPacket(Unpooled.wrappedBuffer(portBuf, urlBuf), msg.getRecipient());
        out.add(packet);
    }
}
