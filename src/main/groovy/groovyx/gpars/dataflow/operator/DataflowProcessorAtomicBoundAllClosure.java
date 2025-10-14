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

public class DataflowProcessorAtomicBoundAllClosure extends Closure {
    public DataflowProcessorAtomicBoundAllClosure() {
        super(null);
    }

    @Override
    public int getMaximumNumberOfParameters() {
        return 1;
    }

    @Override
    public Object call() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object call(final Object[] args) {
        ((DataflowProcessor) getDelegate()).bindAllOutputsAtomically(args[0]);
        return null;

    }

    @Override
    public Object call(final Object arguments) {
        ((DataflowProcessor) getDelegate()).bindAllOutputsAtomically(arguments);
        return null;
    }
}
