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

package groovyx.gpars.dataflow.impl;

/**
 * Represents a request for value from a select.
 * Each request holds a list of guards (boolean flags) to indicate, which input channels of the select should be queried,
 * and a routine to invoke once a value is available.
 *
 * @author Vaclav Pech
 *         Date: 30th Sep 2010
 */
public interface SelectRequest<T> {

    /**
     * Checks, whether the given index should be queried for value or not.
     *
     * @param index The index of the input channel to check for guard
     * @return True, it the select user is interested in values from the channel represented by the provided index
     */
    boolean matchesMask(int index);

    /**
     * Invoked, when a value has been found bound to any of the input channels monitored by the select.
     *
     * @param index The index of the input channel
     * @param value The value obtained from the channel
     */
    void valueFound(int index, T value);
}
