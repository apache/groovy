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

package groovyx.gpars.serial;

import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.remote.RemoteHost;

import java.io.Serializable;
import java.util.UUID;

/**
 * Base class for all messages
 *
 * @author Alex Tkachman, Vaclav Pech, Rafal Slawik
 */
public abstract class SerialMsg implements Serializable {
    private static final long serialVersionUID = 1L ;
    public UUID hostId;

    protected SerialMsg() {
    }

    protected SerialMsg(final UUID hostId) {
        this.hostId = hostId;
    }

    public abstract void execute(final RemoteConnection conn);

    protected void updateRemoteHost(RemoteConnection connection) {
        if (connection.getHost() == null) {
            connection.setHost((RemoteHost) connection.getLocalHost().getSerialHost(hostId, connection));
        }
    }
}
