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

package groovyx.gpars.agent.remote;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.remote.message.RemoteAgentGetValMsg;
import groovyx.gpars.remote.message.RemoteAgentSendClosureMessage;
import groovyx.gpars.remote.message.RemoteAgentSendMessage;
import groovyx.gpars.serial.SerialMsg;

/**
 * Policy of executing closures sent to remote Agent instance as messages.
 *
 * @author Rafal Slawik
 */
public enum AgentClosureExecutionPolicy {
    LOCAL {
        @Override
        public SerialMsg prepareMessage(RemoteAgent<?> agent, Object message) {
            if (message instanceof Closure) {
                Closure closure = (Closure) message;
                RemoteAgentMock mock = new RemoteAgentMock();
                closure.setDelegate(mock);
                DataflowVariable<?> oldValue = new DataflowVariable<>();
                DataflowVariable newValue = new DataflowVariable();
                oldValue.whenBound(new MessageStream() {
                    @Override
                    public MessageStream send(Object message) {
                        closure.call(message);
                        newValue.bindUnique(mock.getState());
                        return this;
                    }
                });
                return new RemoteAgentSendClosureMessage(agent, oldValue, newValue);
            }
            return new RemoteAgentSendMessage(agent, message);
        }
    },
    REMOTE {
        @Override
        public SerialMsg prepareMessage(RemoteAgent<?> agent, Object message) {
            return new RemoteAgentSendMessage(agent, message);
        }
    };

    public abstract SerialMsg prepareMessage(RemoteAgent<?> agent, Object message);

    public <T> SerialMsg prepareGetValMessage(RemoteAgent<T> agent, DataflowVariable<T> resultVariable) {
        return new RemoteAgentGetValMsg<>(agent, resultVariable);
    }
}
