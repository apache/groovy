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

import groovyx.gpars.agent.Agent;
import groovyx.gpars.agent.AgentCore;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.serial.SerialMsg;

/**
 * Message that carry current state of Agent
 * @param <T> the type of state
 *
 * @author Rafal Slawik
 */
public class RemoteAgentGetValMsg<T> extends SerialMsg {
    private final AgentCore agent;
    private final DataflowVariable<T> valueVariable;

    public RemoteAgentGetValMsg(AgentCore agent, DataflowVariable<T> valueVariable) {
        this.agent = agent;
        this.valueVariable = valueVariable;
    }

    @Override
    public void execute(RemoteConnection conn) {
        try {
            T value = ((Agent<T>) agent).getVal();
            valueVariable.bindUnique(value);
        } catch (InterruptedException e) {
            valueVariable.bindError(e);
        }
    }
}
