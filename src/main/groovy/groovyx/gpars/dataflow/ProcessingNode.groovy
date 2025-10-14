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
import groovyx.gpars.dataflow.operator.DataflowOperator
import groovyx.gpars.dataflow.operator.DataflowProcessor
import groovyx.gpars.group.PGroup

/**
 * A ProcessingNode specifies a {@link groovyx.gpars.dataflow.operator.DataflowOperator}:
 * its input and output channels, maxForks and its body of processing logic.
 * @author Dierk Koenig
 */
@CompileStatic(value = TypeCheckingMode.PASS)
class ProcessingNode {
    final List inputs = []
    final List outputs = []
    int maxForks = 1
    Closure body

    /** Convenience factory method.   **/
    static ProcessingNode node(Closure body) {
        new ProcessingNode(body: body)
    }

    /** Factory method for an immediately started operator for this node.  **/
    DataflowProcessor operator(PGroup group) {
        def params = [inputs: inputs, outputs: outputs, maxForks: maxForks]
        new DataflowOperator(group, params, body).start()
    }
}
