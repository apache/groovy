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
import jcsp.plugNplay.ProcessRead

/**
 * ChannelInputList is used to create a list of
 * <code>ChannelInputEnds</code>
 * */

class ChannelInputList {
    def cList = []

/**
 * ChannelInputList uses the <code>ArrayList</code> class of java
 * This ChannelInputList takes an array of <code>One2OneChannels</code> as its
 * constructor parameter and converts them to a list of <code>ChannelInputEnds</code>
 *
 * <p>Company: Napier University</p>
 * @author Jon Kerridge, Ken Barclay, John Savage
 * @version 1.0
 *
 * @version 1.1 included the empty  constructor to enable
 * easier <code>NetChannelOUTPUT</code> list creation (Jon Kerridge)
 * and changes to comply with Groovy-jsr03
 */
    ChannelInputList(channelArray) {
        cList = (Arrays.asList(Channel.getInputArray(channelArray)))
    }

/**
 * ChannelInputList uses the <code>ArrayList</code> class of java
 * This constructor creates an empty <code>ArrayList</code> to be populated with
 * <code>NetChannelInputs</code>
 * */
    ChannelInputList() {
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

    def List read() {
        def values = []
        def channels = cList.size()
        def readerList = []
        (0..<channels).each {i -> readerList[i] = new ProcessRead(cList[i])
        }
        def parRead = new PAR(readerList)
        parRead.run()
        (0..<channels).each {i -> values[i] = readerList[i].value }
        return values
    }

}
