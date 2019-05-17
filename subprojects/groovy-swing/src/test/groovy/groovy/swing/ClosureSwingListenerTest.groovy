/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.swing

import javax.swing.JButton

class ClosureSwingListenerTest extends GroovySwingTestCase {

    void testAddingAndRemovingAClosureListener() {
        testInEDT {
            def b = new JButton("foo")
            b.actionPerformed = { println("Found ${it}") }
            def size = b.actionListeners.size()
            assert size == 1
            def l = b.actionListeners[0]
            def code = l.hashCode()
            println("listener: ${l} with hashCode code ${code}")
            assert l.toString() != "null"
            assert !l.equals(b)
            assert l.equals(l)
            assert l.hashCode() != 0
            b.removeActionListener(l)
            println(b.actionListeners)
            size = b.actionListeners.size()
            assert size == 0
        }
    }

    void testGettingAListenerProperty() {
        testInEDT {
            def b = new JButton("foo")
            def foo = b.actionPerformed
            assert foo == null
        }
    }

    void testNonStandardListener() {
        testInEDT {
            def myWhat = null
            def myWhere = null
            def strangeBean = new StrangeBean()
            strangeBean.somethingStrangeHappened = { what, where -> myWhat = what; myWhere = where}
            strangeBean.somethingStrangeHappened('?', '!')
            assert myWhat == '?'
            assert myWhere == '!'
        }
    }
}