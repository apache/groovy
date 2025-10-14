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
import groovyx.gpars.dataflow.remote.RemoteDataflowVariable;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.serial.SerialMsg;

/**
 * Message that carry Variable.
 *
 * @author Rafal Slawik
 */
public class RemoteDataflowVariableReplyMsg extends SerialMsg {

    private final String name;
    private final DataflowVariable variable;
    private final boolean bound;
    private Object value;

    public RemoteDataflowVariableReplyMsg(String name, DataflowVariable variable) {
        this.name = name;
        this.variable = variable;
        this.bound = variable.isBound();
        if (bound) {
            try {
                value = variable.getVal();
            } catch (InterruptedException e) {
                // fail silently
            }
        }
    }

    @Override
    public void execute(RemoteConnection conn) {
        conn.getLocalHost().registerProxy(RemoteDataflowVariable.class, name, ((RemoteDataflowVariable) variable));
        if (bound) {
            variable.bind(value);
        }
    }

}
