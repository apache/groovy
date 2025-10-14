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
import groovyx.gpars.dataflow.DataflowChannelListener;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Groups the listener-related functionality shared by dataflow channels
 *
 * @author Vaclav Pech
 */
public final class DataflowChannelEventOrchestrator<T> implements DataflowChannelEventListenerManager<T>, BindErrorListenerManager<T>, DataflowChannelEventDistributor<T>, BindErrorDistributor<T> {
    private final Collection<DataflowChannelListener<T>> listeners = new CopyOnWriteArrayList<DataflowChannelListener<T>>();
    private final Collection<BindErrorListener<T>> BindErrorListener = new CopyOnWriteArrayList<BindErrorListener<T>>();

    @Override
    public void addDataflowChannelListener(final DataflowChannelListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void addAllDataflowChannelListeners(final Collection<DataflowChannelListener<T>> listeners) {
        this.listeners.addAll(listeners);
    }

    @Override
    public void removeDataflowChannelListener(final DataflowChannelListener<T> listener) {
        listeners.remove(listener);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    @Override
    public Collection<DataflowChannelListener<T>> getListeners() {
        return listeners;
    }

    @Override
    public void fireOnMessage(final T message) {
        for (final DataflowChannelListener<T> listener : listeners) {
            listener.onMessage(message);
        }
    }

    @Override
    public void addBindErrorListener(final BindErrorListener<T> listener) {
        BindErrorListener.add(listener);
    }

    @Override
    public void addAllBindErrorListeners(final Collection<BindErrorListener<T>> listeners) {
        this.BindErrorListener.addAll(listeners);
    }

    @Override
    public void removeBindErrorListener(final BindErrorListener<T> listener) {
        BindErrorListener.remove(listener);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    @Override
    public Collection<BindErrorListener<T>> getBindErrorListeners() {
        return BindErrorListener;
    }

    /**
     * Notifies all listeners about failed bind operations
     *
     * @param oldValue    The already bound value
     * @param failedValue The value attempted to be bound
     * @param uniqueBind  Flag indicating bindUnique() method call
     */
    @Override
    public void fireBindError(final T oldValue, final T failedValue, final boolean uniqueBind) {
        for (final BindErrorListener<T> listener : BindErrorListener) {
            listener.onBindError(oldValue, failedValue, uniqueBind);
        }
    }

    /**
     * Notifies all listeners about failed bindError operations
     *
     * @param oldValue    The already bound value
     * @param failedError The error attempted to be bound
     */
    @Override
    public void fireBindError(final T oldValue, final Throwable failedError) {
        for (final BindErrorListener<T> listener : BindErrorListener) {
            listener.onBindError(oldValue, failedError);
        }
    }

    /**
     * Notifies all listeners about failed bind operations
     *
     * @param oldError    The already bound Throwable
     * @param failedValue The value attempted to be bound
     * @param uniqueBind  Flag indicating bindUnique() method call
     */
    @Override
    public void fireBindError(final Throwable oldError, final T failedValue, final boolean uniqueBind) {
        for (final BindErrorListener<T> listener : BindErrorListener) {
            listener.onBindError(oldError, failedValue, uniqueBind);
        }
    }

    /**
     * Notifies all listeners about failed bindError operations
     *
     * @param oldError    The already bound Throwable
     * @param failedError The error attempted to be bound
     */
    @Override
    public void fireBindError(final Throwable oldError, final Throwable failedError) {
        for (final BindErrorListener<T> listener : BindErrorListener) {
            listener.onBindError(oldError, failedError);
        }
    }
}
