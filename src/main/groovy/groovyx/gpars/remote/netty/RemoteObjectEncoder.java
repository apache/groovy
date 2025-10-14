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

import java.io.Serializable;

import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.remote.RemoteHost;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class RemoteObjectEncoder extends ObjectEncoder {
    private final RemoteConnection connection;

    /**
     * Creates a new encoder.
     *
     * @param connection connection handling serialization details
     */
    public RemoteObjectEncoder(final RemoteConnection connection) {
        super();
        this.connection = connection;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
        final RemoteHost remoteHost = connection.getHost();

        if (remoteHost != null) {
            remoteHost.enter();
        }
        try {
            super.encode(ctx, msg, out);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            if (remoteHost != null) {
                remoteHost.leave();
            }
        }
    }
}
