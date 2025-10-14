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

package groovyx.gpars.remote.message;

import groovyx.gpars.dataflow.DataflowQueue;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.serial.SerialMsg;

import java.util.UUID;

/**
 * Message that carry remote Queue request.
 *
 * @author Rafal Slawik
 */
public class RemoteDataflowQueueRequestMsg extends SerialMsg {
    final String name;

    public RemoteDataflowQueueRequestMsg(UUID id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public void execute(RemoteConnection conn) {
        updateRemoteHost(conn);

        DataflowQueue<?> queue = conn.getLocalHost().get(DataflowQueue.class, name);
        conn.write(new RemoteDataflowQueueReplyMsg(name, queue));
    }
}
