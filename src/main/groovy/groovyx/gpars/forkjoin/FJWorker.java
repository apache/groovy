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

package groovyx.gpars.forkjoin;

import groovy.lang.Closure;

import java.util.Arrays;

/**
 * Represents a recursive task for the builder-style fork/join algorithm.
 *
 * @author Vaclav Pech
 */
@SuppressWarnings({"unchecked"})
final class FJWorker<T> extends AbstractForkJoinWorker<T> {
    private final Closure code;
    private final T[] args;
    public static final Object[] EMPTY_OBJECTS = {};

    FJWorker(final Object... args) {
        final int size = args.length;
        assert size > 0;
        this.args = size > 1 ? (T[]) Arrays.copyOfRange(args, 0, size - 1) : (T[]) EMPTY_OBJECTS;
        this.code = (Closure) ((Closure) args[size - 1]).clone();
        this.code.setDelegate(this);
        this.code.setResolveStrategy(Closure.DELEGATE_FIRST);
    }

    @SuppressWarnings({"MethodOverloadsMethodOfSuperclass", "OverloadedVarargsMethod"})
    void forkOffChild(final Object... childArgs) {
        if (childArgs.length != args.length)
            throw new IllegalArgumentException("The forkOffChild() method requires " + args.length + " arguments, but received " + childArgs.length + " parameters.");
        final Object[] params = new Object[childArgs.length + 1];
        System.arraycopy(childArgs, 0, params, 0, childArgs.length);
        params[params.length - 1] = code;
        forkOffChild(new FJWorker<T>(params));
    }

    @SuppressWarnings({"MethodOverloadsMethodOfSuperclass", "OverloadedVarargsMethod"})
    T runChildDirectly(final Object... childArgs) {
        if (childArgs.length != args.length)
            throw new IllegalArgumentException("The runChildDirectly() method requires " + args.length + " arguments, but received " + childArgs.length + " parameters.");
        final Object[] params = new Object[childArgs.length + 1];
        System.arraycopy(childArgs, 0, params, 0, childArgs.length);
        params[params.length - 1] = code;
        return new FJWorker<T>(params).compute();
    }

    @Override
    protected T computeTask() {
        return (T) code.call(args);
    }
}
