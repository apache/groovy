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

import groovyx.gpars.agent.AgentCore;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.remote.RemoteHost;
import groovyx.gpars.serial.RemoteSerialized;

/**
 * Proxy object for remote Agent instance.
 * @param <T> the type of state hold by the agent
 *
 * @author Rafal Slawik
 */
public class RemoteAgent<T> extends AgentCore implements RemoteSerialized{
    private final RemoteHost remoteHost;

    private AgentClosureExecutionPolicy executionPolicy;

    public RemoteAgent(RemoteHost remoteHost) {
        this.remoteHost = remoteHost;
        executionPolicy = AgentClosureExecutionPolicy.REMOTE;
    }

    public void setExecutionPolicy(AgentClosureExecutionPolicy executionPolicy) {
        this.executionPolicy = executionPolicy;
    }

    @Override
    public void handleMessage(Object message) {
        remoteHost.write(executionPolicy.prepareMessage(this, message));
    }

    public T getVal() throws InterruptedException {
        DataflowVariable<T> resultVariable = new DataflowVariable<>();
        remoteHost.write(executionPolicy.prepareGetValMessage(this, resultVariable));
        return resultVariable.getVal();
    }

}
