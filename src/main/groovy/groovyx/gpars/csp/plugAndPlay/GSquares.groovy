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
import jcsp.lang.Channel
import jcsp.lang.ChannelOutput
import jcsp.lang.One2OneChannel

class GSquares implements CSProcess {

    ChannelOutput outChannel

    void run() {

        One2OneChannel N2I = Channel.createOne2One()
        One2OneChannel I2P = Channel.createOne2One()

        def testList = [new GNumbers(outChannel: N2I.out()),
                new GIntegrate(inChannel: N2I.in(),
                        outChannel: I2P.out()),
                new GPairs(inChannel: I2P.in(),
                        outChannel: outChannel),
        ]
        new PAR(testList).run()
    }
}
