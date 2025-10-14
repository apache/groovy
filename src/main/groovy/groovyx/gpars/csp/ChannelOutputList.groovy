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

import jcsp.lang.Channel
import jcsp.plugNplay.ProcessWrite

/**
 * ChannelOutputList is used to create a list of
 * <code>ChannelOUTPUTEnds</code>
 * <p>Company: Napier University</p>
 *
 * @author Jon Kerridge, Ken Barclay, John Savage
 * @version 1.0
 *
 * @version 1.1 included the empty  constructor to enable
 * easier <code>NetChannelOUTPUT</code> list creation (Jon Kerridge)
 * and changes to comply with Groovy-jsr03
 */
class ChannelOutputList {
    def cList = []

/**
 * ChannelOutputList uses the <code>ArrayList</code> class of java
 * This ChannelOutputList takes an array of <code>One2OneChannels</code> as its
 * constructor parameter and converts them to a list of <code>ChannelOUTPUTEnds</code>
 * */
    ChannelOutputList(channelArray) {
        cList = (Arrays.asList(Channel.getOutputArray(channelArray)))
    }

/**
 * ChannelOutputList uses the <code>ArrayList</code> class of java
 * This constructor creates an empty <code>ArrayList</code> to be populated with
 * <code>NetChannelOUTPUTs</code>
 * */
    ChannelOutputList() {
        // nothing required it is just an empty list
    }

    def append(value) {
        cList << value
    }

    def size() {
        return cList.size()
    }

    def contains(value) {
        return cList.contains(value)
    }

    def remove(value) {
        return cList.remove(value)
    }

    def minus(list) {
        return cList - list
    }

    def plus(list) {
        return cList + list
    }

    def putAt(index, value) {
        cList[index] = value
    }

    def getAt(index) {
        return cList[index]
    }

    def Object[] toArray() {
        return cList.toArray()
    }

    def write(value) {
        def channels = cList.size()
        def writerList = []
        (0..<channels).each {i ->
            writerList[i] = new ProcessWrite(cList[i])
            writerList[i].value = value
        }
        def parWrite = new PAR(writerList)
        parWrite.run()
    }

}
