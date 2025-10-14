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
import groovyx.gpars.dataflow.DataflowVariable;

/**
 * Wrapper for closure message that is execute locally.
 *
 * @author Rafal Slawik
 */
public class AgentClosureExecutionClosure extends Closure {
    private final DataflowVariable oldValueVariable;
    private final DataflowVariable newValueVariable;

    public AgentClosureExecutionClosure(Object owner, DataflowVariable oldValueVariable, DataflowVariable newValueVariable) {
        super(owner);
        this.oldValueVariable = oldValueVariable;
        this.newValueVariable = newValueVariable;
    }

    @Override
    public Object call(Object... args) {
        oldValueVariable.bindUnique(args[0]);
        try {
            invokeMethod("updateValue", newValueVariable.getVal());
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }
}
