// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
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

package groovyx.gpars;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Holds a thread-local stack of pools to allow for nested calls to ForkJoinPool.withPool() or GParsExecutorsPool.withPool()
 *
 * @author Vaclav Pech
 *         Date: Dec 15, 2009
 */
final class ThreadLocalPools extends ThreadLocal<LinkedList<Object>> {

    @Override
    protected LinkedList<Object> initialValue() {
        return new LinkedList<Object>();
    }

    /**
     * Adds a new element
     *
     * @param pool The pool to store
     */
    @SuppressWarnings({"UnusedDeclaration"})
    void leftShift(final Object pool) {
        get().add(pool);
    }

    /**
     * Removes the top (last) element
     */
    void pop() {
        assert !get().isEmpty();
        get().removeLast();
    }

    /**
     * Gives the current element
     *
     * @return The retrieved pool
     */
    Object getCurrent() {
        final Deque<Object> stack = get();
        return stack.isEmpty() ? null : stack.getLast();
    }

    /**
     * Indicates whether the stack is empty
     *
     * @return True if the stack is empty
     */
    Object isEmpty() {
        return get().isEmpty();
    }
}
