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

package groovyx.gpars.csp.plugAndPlay

import groovyx.gpars.csp.PAR

import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.plugNplay.ProcessWrite

class GPCopy implements CSProcess {

    ChannelInput inChannel
    ChannelOutput outChannel0
    ChannelOutput outChannel1

    void run() {
        def write0 = new ProcessWrite(outChannel0)
        def write1 = new ProcessWrite(outChannel1)
        def parWrite2 = new PAR([write0, write1])
        while (true) {
            def i = inChannel.read()
            write0.value = i
            write1.value = i
            parWrite2.run()
        }
    }
}
