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

package groovyx.gpars.remote.message;

import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.remote.RemoteHost;
import groovyx.gpars.serial.SerialMsg;

import java.util.UUID;

/**
 * Message sent by NetTransportProvider immediately after connection to another host is set up
 *
 * @author Alex Tkachman, Vaclav Pech
 */
public class HostIdMsg extends SerialMsg {
    private static final long serialVersionUID = -7805772642034504624L;

    /**
     * Construct message representing current state of the transport provider
     * @param id Local host id
     */
    public HostIdMsg(final UUID id) {
        super(id);
    }

    @Override
    public void execute(RemoteConnection conn) {
        conn.setHost((RemoteHost) conn.getLocalHost().getSerialHost(hostId, conn));
        conn.onConnect();
    }
}
