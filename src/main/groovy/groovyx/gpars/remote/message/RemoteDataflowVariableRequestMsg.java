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

import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.serial.SerialMsg;

import java.util.UUID;

/**
 * Message that carry remote Variable request.
 *
 * @author Rafal Slawik
 */
public class RemoteDataflowVariableRequestMsg extends SerialMsg {

    private final String name;

    public RemoteDataflowVariableRequestMsg(UUID hostId, String name) {
        super(hostId);
        this.name = name;
    }

    @Override
    public void execute(RemoteConnection conn) {
        updateRemoteHost(conn);

        DataflowVariable<?> var = conn.getLocalHost().get(DataflowVariable.class, name);
        conn.write(new RemoteDataflowVariableReplyMsg(name, var));
    }

}
