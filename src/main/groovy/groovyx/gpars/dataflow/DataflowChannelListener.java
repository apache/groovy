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
 * Gets notified about state changes inside DataflowReadChannels
 *
 * @author Vaclav Pech
 */
public interface DataflowChannelListener<T> {
    /**
     * Notifies about messages passed through the channel
     *
     * @param message The value just added to the channel
     */
    void onMessage(T message);
}
