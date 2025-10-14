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

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * A dataflow expression, which invokes a method after the receiver as well as all the arguments become available
 *
 * @author Alex Tkachman
 */
public class DataflowInvocationExpression extends DataflowComplexExpression<Object> {
    private static final long serialVersionUID = -678669663648650627L;
    private Object receiver;
    private final String methodName;

    public DataflowInvocationExpression(final Object receiver, final String methodName, final Object[] args) {
        super(args);
        this.receiver = receiver;
        this.methodName = methodName;
        subscribe();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object evaluate() {
        if (receiver instanceof DataflowExpression) {
            receiver = ((DataflowExpression<Object>) receiver).value;
        }
        super.evaluate();
        return InvokerHelper.invokeMethod(receiver, methodName, args);
    }

    @Override
    protected void subscribe(final DataflowExpressionsCollector listener) {
        if (receiver instanceof DataflowExpression) {
            receiver = listener.subscribe(receiver);
        }
        super.subscribe(listener);
    }
}
