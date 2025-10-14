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

/**
 * A KanbanTray is a container for passing a product around a {@KanbanLink}.
 * A tray always stays within the same {@link KanbanLink}, which allows methods like
 * {@link KanbanTray#take}, {@link KanbanTray#bind} and {@link KanbanTray#release} to work directly on the
 * respective streams without those being exposed to the operator.
 * While full access to the inner workings remains available, it is advised to use the methods
 * {@link KanbanTray#take} and {@link KanbanTray#bind}.
 * @see KanbanLink
 * @author Dierk Koenig
 */
@CompileStatic(value = TypeCheckingMode.PASS)
class KanbanTray {

    KanbanLink link
    Object product

    /** Put the product in the tray and send it downstream.   **/
    void bind(product) {
        this.product = product
        link.downstream.bind this
    }

    /** Alias for {@link KanbanTray#bind} in order to write <code> tray &lt;&lt; product </code>   **/
    void leftShift(product) { bind product }

    /** Alias for {@link KanbanTray#bind} in order to write <code> tray product </code>   **/
    void call(product) { bind product }

    /** Send the empty tray back upstream.   **/
    void release() {
        product = null
        link.upstream.bind this
    }

    /** Alias for {@link KanbanTray#release} in order to write <code> &tilde; tray </code> ("shake loose")  **/
    def bitwiseNegate() { release() }

    /** Take the product from the tray and release the tray. You can only do this once!
     * @return the product
     */
    Object take() {
        def result = product
        release()
        return result
    }

    String toString() {
        "Tray ${hashCode()} link ${link.id} product $product"
    }
}
