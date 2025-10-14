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

import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.expression.DataflowExpression;
import groovyx.gpars.remote.RemoteHost;
import groovyx.gpars.serial.RemoteSerialized;

/**
 * Proxy object for remote Variable instance.
 * @param <T> the type of the variable
 *
 * @author Rafal Slawik
 */
public final class RemoteDataflowVariable<T> extends DataflowVariable<T> implements RemoteSerialized {
    private static final long serialVersionUID = -420013188758006693L;
    private final RemoteHost remoteHost;

    public RemoteDataflowVariable(final RemoteHost host) {
        remoteHost = host;
        getValAsync(new MessageStream() {
            private static final long serialVersionUID = 7968302123667353660L;

            @SuppressWarnings({"unchecked"})
            @Override
            public MessageStream send(final Object message) {
                remoteHost.write(new DataflowExpression.BindDataflow(RemoteDataflowVariable.this, message, remoteHost.getHostId()));
                return this;
            }
        });
    }
}
