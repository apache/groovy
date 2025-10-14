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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

public class DiscoveryServerHandler extends SimpleChannelInboundHandler<DiscoveryRequestWithSender> {
    private final InetSocketAddress serverSocketAddress;
    private RemotingContextWithUrls remotingContext;

    public DiscoveryServerHandler(InetSocketAddress serverSocketAddress, RemotingContextWithUrls remotingContext) {
        this.serverSocketAddress = serverSocketAddress;
        this.remotingContext = remotingContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DiscoveryRequestWithSender msg) throws Exception {
        DiscoveryRequest request = msg.getRequest();
        if (remotingContext.has(request.getActorUrl())) {
            DiscoveryResponse response = new DiscoveryResponse(request.getActorUrl(), serverSocketAddress);
            DiscoveryResponseWithRecipient responseWithRecipient = new DiscoveryResponseWithRecipient(response, msg.getSender());
            ctx.write(responseWithRecipient);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }
}
