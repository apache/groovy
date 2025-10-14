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
import groovyx.gpars.dataflow.remote.RemoteDataflowQueue;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.serial.SerialMsg;

/**
 * Message that pushes new value into remote queue.
 * @param <T> the type of the queue
 *
 * @author Rafal Slawik
 */
public class RemoteDataflowQueueEnqueueValueMsg<T> extends SerialMsg {
    private final DataflowChannel<T> queue;
    private final T value;

    public RemoteDataflowQueueEnqueueValueMsg(RemoteDataflowQueue<T> queue, T value) {
        this.queue = queue;
        this.value = value;
    }

    @Override
    public void execute(RemoteConnection conn) {
        queue.bind(value);
    }
}
