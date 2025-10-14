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

import groovyx.gpars.dataflow.DataflowBroadcast;
import groovyx.gpars.dataflow.remote.RemoteDataflowBroadcast;
import groovyx.gpars.dataflow.stream.DataflowStreamWriteAdapter;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.serial.SerialMsg;

/**
 * Message used to carry Broadcast stream.
 *
 * @author Rafal Slawik
 */
public class RemoteDataflowBroadcastReplyMsg extends SerialMsg {
    private final String name;
    private final DataflowStreamWriteAdapter stream;

    public RemoteDataflowBroadcastReplyMsg(String name, DataflowBroadcast stream) {
        this.name = name;
        this.stream = stream;
    }

    @Override
    public void execute(RemoteConnection conn) {
        conn.getLocalHost().registerProxy(RemoteDataflowBroadcast.class, name, ((RemoteDataflowBroadcast) stream));
    }
}
