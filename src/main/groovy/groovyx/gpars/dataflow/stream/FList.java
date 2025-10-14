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

package groovyx.gpars.dataflow.stream;

import groovy.lang.Closure;

/**
 * Represents a list implemented as a Functional Queue.
 *
 * @param <T> The type of values to store in the list
 */
public interface FList<T> extends Iterable<T> {

    T getFirst();

    FList<T> getRest();

    boolean isEmpty();

    FList<T> filter(Closure filterClosure);

    FList<Object> map(Closure mapClosure);

    Object reduce(Closure reduceClosure);

    Object reduce(T seed, Closure reduceClosure);

    String appendingString();
}

