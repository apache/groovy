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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class DiscoveryClientHandler extends SimpleChannelInboundHandler<DiscoveryResponse> {
    private Map<String, DataflowVariable<InetSocketAddress>> registeredPromises;

    public DiscoveryClientHandler(Map<String, DataflowVariable<InetSocketAddress>> registeredPromises) {
        this.registeredPromises = registeredPromises;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DiscoveryResponse msg) throws Exception {
        DataflowVariable<InetSocketAddress> promise = registeredPromises.get(msg.getActorUrl());
        if (promise != null) {
            promise.bind(msg.getServerSocketAddress());
        }
    }
}
