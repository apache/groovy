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

import groovy.lang.Closure;
import groovyx.gpars.agent.AgentCore;
import groovyx.gpars.agent.remote.RemoteAgent;
import groovyx.gpars.remote.RemoteConnection;
import groovyx.gpars.serial.SerialMsg;

/**
 * Message that carry message to Agent.
 *
 * @author Rafal Slawik
 */
public class RemoteAgentSendMessage extends SerialMsg {
    private final AgentCore agent;
    private final Object message;

    public RemoteAgentSendMessage(RemoteAgent agent, Object message) {
        this.message = prepareMessage(message);
        this.agent = agent;
    }

    @Override
    public void execute(RemoteConnection conn) {
        agent.send(message);
    }

    private Object prepareMessage(Object message) {
        if (message instanceof Closure) {
            return ((Closure) message).dehydrate();
        }
        return message;
    }
}
