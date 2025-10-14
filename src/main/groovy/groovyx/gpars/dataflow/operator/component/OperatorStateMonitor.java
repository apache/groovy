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

package groovyx.gpars.dataflow.operator.component;

/**
 * Used by GracefulShutdownListeners. Not intended for public use.
 *
 * @author Vaclav Pech
 */
public interface OperatorStateMonitor {
    /**
     * Invoked whenever a processor changes state
     */
    void stateChanged();

    /**
     * Invoked by GracefulShutdownListeners, which listen on operators/selectors for lifecycle events, to get registered with the monitor.
     * The monitor will query these registered listeners about their respective operator/selector state when performing graceful shutdown.
     * @param listener The listener to register
     */
    void registerProcessorListener(final GracefulShutdownListener listener);
}
