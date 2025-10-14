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

import jcsp.lang.Alternative
import jcsp.lang.Guard

/**
 * ALT is used to create an <code>Alternative</code> object
 *
 * <p>Company: Napier University</p>
 * @author Jon Kerridge, Ken Barclay, John Savage
 * @version 1.0
 * @version 1.1    takes account of jsr03 requirements
 */
@CompileStatic(value = TypeCheckingMode.PASS)
class ALT extends Alternative {

/**
 * ALT extends the <code>Alternative</code> class of JCSP
 * ALT takes a list of <code>Guards</code> as its constructor parameter
 * and converts them to an array of <code>Guards</code>
 * as required by <code>Alternative</code>
 */
    ALT(ChannelInputList channelList) {
        super((Guard[]) channelList.toArray())
    }

    ALT(List guardList) {
        super((Guard[]) guardList.toArray())
    }
}
