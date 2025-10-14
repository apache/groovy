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

package groovyx.gpars.dataflow.remote;

import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.stream.DataflowStream;
import groovyx.gpars.dataflow.stream.DataflowStreamReadAdapter;
import groovyx.gpars.dataflow.stream.DataflowStreamWriteAdapter;
import groovyx.gpars.remote.RemoteHost;

/**
 * Proxy object for remote instance of Broadcast.
 * @param <T> the type of the broadcast
 *
 * @author Rafal Slawik
 */
public class RemoteDataflowBroadcast<T> extends DataflowStreamWriteAdapter<T> {
    public RemoteDataflowBroadcast(RemoteHost remoteHost) {
        super(new DataflowStream<T>());
    }

    public synchronized String toString() {
        return "RemoteDataflowBroadcast for " + super.toString();
    }

    public DataflowReadChannel<T> createReadChannel() {
        return new DataflowStreamReadAdapter<T>(getHead());
    }
}
