// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2012  The original author or authors
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

import groovyx.gpars.dataflow.BindErrorListener;

import java.util.Collection;

/**
 * @author Vaclav Pech
 */
public interface BindErrorListenerManager<T> {
    void addBindErrorListener(BindErrorListener<T> listener);

    void addAllBindErrorListeners(Collection<BindErrorListener<T>> listeners);

    void removeBindErrorListener(BindErrorListener<T> listener);

    Collection<BindErrorListener<T>> getBindErrorListeners();
}
