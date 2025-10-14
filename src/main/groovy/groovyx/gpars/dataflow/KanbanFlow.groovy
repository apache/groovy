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

package groovyx.gpars.dataflow

import groovyx.gpars.GParsConfig
import groovyx.gpars.group.DefaultPGroup
import groovyx.gpars.group.PGroup

/**
 * A KanbanFlow is a network of dataflow connections made up from {@link KanbanLink}s.
 * Links can be arranged as to form a chain (1:1), a fork (1:n), a funnel (n:1),
 * a diamond (1:n:1) and any other acyclic directed graph by composition.
 * Cycles are disallowed by
 * default but can be made possible by setting the <tt>cycleAllowed</tt> property to <tt>true</tt>.
 * All {@link KanbanLink}s in a KanbanFlow share the same PGroup. Per default this group uses a
 * resizeable pool of daemon threads, starting with the GPars default pool size. This can be overridden
 * by setting the <tt>pooledGroup</tt> property to a custom PGroup.
 * @see KanbanLink
 * @author Dierk Koenig
 */
class KanbanFlow {

    protected List<KanbanLink> links = []

    /** If adapted, this must be set before calling link()   */
    boolean cycleAllowed = false

    /** If adapted, this must be set before calling start()   */
    PGroup pooledGroup = new DefaultPGroup(GParsConfig.retrieveDefaultPool()) // default pool size

    /**
     * First part of the sequence <code>link producer to consumer</code>.
     */
    KanbanLink link(ProcessingNode producer) {
        assert producer != null
        new KanbanLink(producerSpec: producer, flow: this)
    }

    /** Start all {@link KanbanLink}s of this flow without any trays.   **/
    void startEmpty() {
        assert links, "There is not much point in starting a flow that has no links."
        links*.start()
    }

    /** Start all {@link KanbanLink}s of this flow with so many trays.   **/
    void start(int numberOfTrays) {
        startEmpty()
        numberOfTrays.times { links*.addTray() }
    }

    /** Start all {@link KanbanLink}s of this flow with as many trays per link as what is considered optimal.   **/
    void start() {
        startEmpty()
        links*.addOptimalNumberOfTrays()
    }

    /** Stop all {@link KanbanLink}s of this flow. **/
    void stop() { links*.stop() } // note dk: needs investigation, maybe do it in reverse

    /** Helper method that inverses the sequence of Closure parameters.  **/
    static Closure inverse(Closure body) {
        { a, b -> body b, a }
    }

    /** Composes two flows by adding a trailing one to the current one, returning a new one.
     * {@link KanbanLink}s maintain their state beside that the last link of the current flow
     * is linked to the first link of the trailing flow.
     * */
    KanbanFlow plus(KanbanFlow trailingFlow) {
        def flow = new KanbanFlow()
        flow.links.addAll links

        def spec = trailingFlow.links.first().producerSpec
        spec.body = inverse(spec.body) // since trailingFlow has been wired already, sequence would be switched
        flow.link(links.last().consumerSpec).to(spec)

        flow.links.addAll trailingFlow.links

        return flow
    }
}


