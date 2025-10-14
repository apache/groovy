// GPars — Groovy Parallel Systems
//
// Copyright © 2008–2010, 2018  The original author or authors
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

package groovyx.gpars.csp

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

import jcsp.lang.CSProcess
import jcsp.lang.Parallel

/**
 * PAR is used to create a <code>Parallel</code> object
 *
 * <p>Company: Napier University</p>
 *
 * @author Jon Kerridge, Ken Barclay, John Savage
 * @version 1.0
 * @version 1.1 modified to compile under groovy jsr03
 */

@CompileStatic(value = TypeCheckingMode.PASS)
class PAR extends Parallel {

/**
 * PAR extends the <code>Parallel</code> class of JCSP
 * PAR takes a list of processes as its constructor parameter
 * and converts them to an array of <code>CSProcess</code>
 * as required by <code>Parallel</code>
 * */
    PAR(processList) {
        super()
        processList.each { CSProcess p ->
            this.addProcess(p)
        }
    }

    PAR() {
        super()
    }

}
