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

package groovyx.gpars.dataflow;

/**
 * A common interface for all writable dataflow variables, streams or queues
 *
 * @author Vaclav Pech
 *         Date: 21st Sep 2010
 */
public interface DataflowWriteChannel<T> {

    /**
     * Assigns a value to the variable. Can only be invoked once on each instance of DataflowVariable
     *
     * @param value The value to assign
     * @return The current channel instance
     */
    DataflowWriteChannel<T> leftShift(final T value);

    /**
     * Assigns a value to the variable. Can only be invoked once on each instance of DataflowVariable
     *
     * @param value The value to assign
     */
    void bind(final T value);

    /**
     * Assigns a value from one DataflowVariable instance to this variable.
     * Can only be invoked once on each instance of DataflowVariable
     *
     * @param ref The DataflowVariable instance the value of which to bind
     * @return The current channel instance
     */
    DataflowWriteChannel<T> leftShift(final DataflowReadChannel<T> ref);
}
