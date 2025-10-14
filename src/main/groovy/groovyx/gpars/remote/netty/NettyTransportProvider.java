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
import groovyx.gpars.remote.message.HostIdMsg;


/**
 * Transport provider using Netty.
 */
public class NettyTransportProvider {

    /**
     * Factory method for creating server instances.
     * @param host
     * @param port
     * @param localHost the serialization context for connection
     * @return new instance of {@link groovyx.gpars.remote.netty.NettyServer}
     */
    public static NettyServer createServer(String host, int port, LocalHost localHost) {
        return new NettyServer(localHost, host, port, connection -> connection.write(new HostIdMsg(localHost.getId())));
    }

    /**
     * Factory method for creating client instances.
     * @param host the address of host to connect
     * @param port the destination port
     * @param localHost the serialization context for connection
     * @param listener the action performed on connect
     * @return new instance of {@link NettyClient}
     */
    public static NettyClient createClient(String host, int port, LocalHost localHost, ConnectListener listener) {
        return new NettyClient(localHost, host, port, listener);
    }
}
