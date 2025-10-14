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
import org.codehaus.groovy.runtime.NullObject;

/**
 * Used by the filter() method to wrap the supplied closure and use it for filtering data.
 * This wrapper closure returns the original data, if the supplied function returns true.
 * A NullObject.getNullObject() value is return to indicate the current piece of data should be filtered out.
 *
 * @author Vaclav Pech
 */

public final class FilterClosure<V> extends Closure {
    private final Closure<Boolean> wiseMan;

    private static final Class[] PARAMETER_TYPES = {Object.class};

    public FilterClosure(final Closure<Boolean> wiseMan) {
        super(null, null);
        if (wiseMan.getMaximumNumberOfParameters() != 1)
            throw new IllegalArgumentException("A filter operator can only use one-argument functions for decisions.");
        this.wiseMan = wiseMan;
    }

    @Override
    public int getMaximumNumberOfParameters() {
        return 1;
    }

    @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
    @Override
    public Class[] getParameterTypes() {
        return PARAMETER_TYPES;
    }

    @Override
    public Object call(final Object arguments) {
        final boolean decision = wiseMan.call(arguments);
        return decision ? arguments : NullObject.getNullObject();
    }

    @SuppressWarnings({"OverloadedVarargsMethod"})
    @Override
    public Object call(final Object... args) {
        final boolean decision = wiseMan.call(args);
        return decision ? args[0] : NullObject.getNullObject();
    }

    @Override
    public Object call() {
        final boolean decision = wiseMan.call();
        return decision ? null : NullObject.getNullObject();
    }
}
