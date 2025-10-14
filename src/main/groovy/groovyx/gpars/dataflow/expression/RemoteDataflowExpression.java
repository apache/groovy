// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
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

package groovyx.gpars.dataflow.expression;

import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.remote.RemoteHost;
import groovyx.gpars.serial.RemoteSerialized;
import groovyx.gpars.serial.SerialContext;

/**
 * @author Alex Tkachman
 */
public class RemoteDataflowExpression<T> extends DataflowExpression<T> implements RemoteSerialized {
    private static final long serialVersionUID = -3166182949181062129L;
    private final RemoteHost remoteHost;

    public RemoteDataflowExpression() {
        remoteHost = (RemoteHost) SerialContext.get();
        getValAsync(new MessageStream() {
            private static final long serialVersionUID = -8868544599311892034L;

            @SuppressWarnings({"unchecked"})
            @Override
            public MessageStream send(final Object message) {
                remoteHost.write(new BindDataflow(RemoteDataflowExpression.this, message, remoteHost.getHostId()));
                return this;
            }
        });
    }

    @Override
    protected T evaluate() {
        return value;
    }

    @Override
    protected void subscribe(final DataflowExpression<T>.DataflowExpressionsCollector listener) {
        listener.subscribe(this);
    }
}
