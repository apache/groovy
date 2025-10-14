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

package groovyx.gpars.dataflow.operator;

import groovy.lang.Closure;

/**
 * Used by the chainWith() method to wrap the supplied closure inside the operator's body.
 * This wrapper closure is responsible for writing the calculated result into the operator's output through its bind() methods.
 *
 * @author Vaclav Pech
 */

public final class BinaryChoiceClosure extends Closure<Void> {
    private final Closure<Boolean> code;

    public BinaryChoiceClosure(final Closure<Boolean> code) {
        super(null, null);
        this.code = code;
    }

    @Override
    public int getMaximumNumberOfParameters() {
        return code.getMaximumNumberOfParameters();
    }

    @Override
    public Class[] getParameterTypes() {
        return code.getParameterTypes();
    }

    @Override
    public void setDelegate(final Object delegate) {
        super.setDelegate(delegate);
        code.setDelegate(delegate);
    }

    @Override
    public void setResolveStrategy(final int resolveStrategy) {
        super.setResolveStrategy(resolveStrategy);
        code.setResolveStrategy(resolveStrategy);
    }

    @Override
    public Void call(final Object arguments) {
        final boolean result = code.call(arguments);
        if (result) ((DataflowProcessor) getDelegate()).bindOutput(0, arguments);
        else ((DataflowProcessor) getDelegate()).bindOutput(1, arguments);
        return null;
    }

    @Override
    public Void call(final Object... args) {
        final boolean result = code.call(args);
        if (result) ((DataflowProcessor) getDelegate()).bindOutput(0, args[0]);
        else ((DataflowProcessor) getDelegate()).bindOutput(1, args[0]);
        return null;
    }

    @Override
    public Void call() {
        final boolean result = code.call();
        if (result) ((DataflowProcessor) getDelegate()).bindOutput(0, null);
        else ((DataflowProcessor) getDelegate()).bindOutput(1, null);
        return null;
    }
}
