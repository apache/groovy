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

package groovyx.gpars.dataflow;

/**
 * An empty implementation of BindErrorListener
 *
 * @param <T> The type of the values bound to the DataflowVariable that is being listened to
 */
public class BindErrorAdapter<T> implements BindErrorListener<T> {
    /**
     * Notifies about failed bind operations
     *
     * @param oldValue    The already bound value
     * @param failedValue The value attempted to be bound
     * @param uniqueBind  Flag indicating bindUnique() method call
     */
    @Override
    public void onBindError(final T oldValue, final T failedValue, final boolean uniqueBind) {
    }

    /**
     * Notifies about failed bindError operations
     *
     * @param oldValue    The already bound value
     * @param failedError The error attempted to be bound
     */
    @Override
    public void onBindError(final T oldValue, final Throwable failedError) {
    }

    /**
     * Notifies about failed bind operations
     *
     * @param oldError    The already bound Throwable
     * @param failedValue The value attempted to be bound
     * @param uniqueBind  Flag indicating bindUnique() method call
     */
    @Override
    public void onBindError(final Throwable oldError, final T failedValue, final boolean uniqueBind) {
    }

    /**
     * Notifies about failed bindError operations
     *
     * @param oldError    The already bound Throwable
     * @param failedError The error attempted to be bound
     */
    @Override
    public void onBindError(final Throwable oldError, final Throwable failedError) {
    }
}
