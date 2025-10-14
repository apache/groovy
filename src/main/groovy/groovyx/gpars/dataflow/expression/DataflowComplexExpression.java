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

/**
 * Provides the infrastructure for dataflow expressions to evaluate arguments.
 * The arguments may be dataflow expressions of themselves and so we cannot evaluate them before they all have real values bound to them.
 *
 * @author Alex Tkachman
 */
public abstract class DataflowComplexExpression<T> extends DataflowExpression<T> {
    private static final long serialVersionUID = 1527021112173826064L;
    protected final Object[] args;

    protected DataflowComplexExpression(final Object... elements) {
        this.args = elements.clone();
    }

    /**
     * Subscribes the listener to all the arguments
     */
    @Override
    protected void subscribe(final DataflowExpressionsCollector listener) {
        for (int i = 0; i != args.length; ++i) {
            args[i] = listener.subscribe(args[i]);
        }
    }

    /**
     * Evaluates all the arguments
     */
    @Override
    protected T evaluate() {
        for (int i = 0; i != args.length; ++i) {
            if (args[i] instanceof DataflowExpression) {
                args[i] = ((DataflowExpression<?>) args[i]).value;
            }
        }
        return null;
    }
}
