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

import groovyx.gpars.dataflow.DataflowChannel;
import groovyx.gpars.dataflow.DataflowQueue;
import groovyx.gpars.dataflow.remote.RemoteDataflowQueue;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.serial.SerialMsg;

/**
 * Message that carry DataflowQueue.
 *
 * @author Rafal Slawik
 */
public class RemoteDataflowQueueReplyMsg extends SerialMsg {
    final String name;
    final DataflowChannel<?> queue;

    public RemoteDataflowQueueReplyMsg(String name, DataflowQueue<?> queue) {
        this.name = name;
        this.queue = queue;
    }

    @Override
    public void execute(RemoteConnection conn) {
        conn.getLocalHost().registerProxy(RemoteDataflowQueue.class, name, ((RemoteDataflowQueue) queue));
    }
}
