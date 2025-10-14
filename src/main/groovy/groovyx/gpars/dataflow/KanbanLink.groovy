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

package groovyx.gpars.dataflow

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovyx.gpars.dataflow.operator.DataflowProcessor

/**
 * A KanbanLink is the simplest possible producer-consumer pair in a kanban-like
 * scenario as described in <a href="http://people.canoo.com/mittie/kanbanflow.html> the
 * kanban flow pattern</a>.
 * In such a link, one or many {@link KanbanTray}s are passed back and forth.
 * Each link must have a {@KanbanFlow}. Many links can share the same {@KanbanFlow}.
 * @see KanbanFlow
 * @see KanbanTray
 * @author Dierk Koenig
 */
@CompileStatic(value = TypeCheckingMode.PASS)
class KanbanLink {
    KanbanFlow flow
    ProcessingNode producerSpec, consumerSpec
    protected DataflowQueue upstream
    protected DataflowQueue downstream
    protected DataflowProcessor producer, consumer

    /**
     * Second part of the sequence <code>link producer to consumer</code>.
     * @throws IllegalArgumentException in case of improper wiring attempts such as cycles or trying to create a chain bottom-up instead of top-down.
     */
    KanbanLink to(ProcessingNode consumerSpec) {
        assert consumerSpec != null
        if (!flow.cycleAllowed) {
            if (consumerSpec == producerSpec || flow.links.any { KanbanLink link -> link.producerSpec.is consumerSpec }) {
                throw new IllegalArgumentException("""You try to link to a consumer that is already a producer. This is not allowed as it may result in cyclic messaging.""")
            }
        }
        this.consumerSpec = consumerSpec
        upstream = new DataflowQueue()
        downstream = new DataflowQueue()
        consumerSpec.inputs << downstream // note that sequence is important if producer == consumer
        consumerSpec.outputs << upstream
        producerSpec.inputs << upstream
        producerSpec.outputs << downstream
        flow.links << this
        return this
    }

    /** Construct and start the operators if not already started.
     *  No trays are put automatically into the upstream.
     * */
    void start() {
        // the producer may have already been started as a consumer of a previous link
        // (in case of chains) or as a producer (in case of splitters)
        // in which case we do not want to start it a second time
        if (!producerAlreadyStarted()) {
            producer = producerSpec.operator(flow.pooledGroup)
        }
        if (!consumerAlreadyStarted()) {
            consumer = consumerSpec.operator(flow.pooledGroup)
        }
    }

    protected boolean producerAlreadyStarted() {
        if (producer != null) return true
        flow.links.any { KanbanLink link ->
            if (link.consumerSpec.is(producerSpec) && link.consumer != null) return true
            if (link.producerSpec.is(producerSpec) && link.producer != null) return true
            false
        }
    }

    protected boolean consumerAlreadyStarted() {
        if (consumer != null) return true
        flow.links.any { KanbanLink link ->
            if (link.consumerSpec.is(consumerSpec) && link.consumer != null) return true
            if (link.producerSpec.is(consumerSpec) && link.producer != null) return true
            false
        }
    }

    /** Putting another tray into system by adding an empty one to the upstream.   **/
    void addTray() {
        upstream << new KanbanTray(link: this)
    }

    /** Removing a tray from the system by fetching the next one from the upstream.   **/
    void removeTray() {
        upstream.val
    }

    /** Put as many trays into the system as there are processing units. This is usually the optimal count.   **/
    def addOptimalNumberOfTrays() {
        (producerSpec.maxForks + consumerSpec.maxForks).times {
            addTray()
        }
    }

    /** Immediately terminating all producers and consumers.   **/
    void stop() {
        producer?.terminate()
        consumer?.terminate()
        consumer = null
        producer = null
    }

    /** The index of this link in its flow.   **/
    int getId() {
        flow.links.indexOf this
    }
}
