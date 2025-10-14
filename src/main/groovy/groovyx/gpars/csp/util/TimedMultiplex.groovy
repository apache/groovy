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

package groovyx.gpars.csp.util

import groovyx.gpars.csp.ALT
import groovyx.gpars.csp.ChannelInputList

import jcsp.lang.CSProcess
import jcsp.lang.CSTimer
import jcsp.lang.ChannelOutput

class TimedMultiplex implements CSProcess {

    ChannelInputList inChannels
    ChannelOutput outChannel
    long timeout      // period during which process will run

    def void run() {
        def timer = new CSTimer()
        def timerIndex = inChannels.size()
        def alt = new ALT((List)(inChannels + timer))
        timer.alarm = timer.read() + timeout
        def running = true
        while (running) {
            def index = alt.select()
            if (index == timerIndex) {
                running = false
                println "TimedMultiplex has stopped"
            } else {
                outChannel.write(inChannels[index].read())
            }
        }
    }
}
