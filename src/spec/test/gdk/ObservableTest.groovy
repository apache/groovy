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
package gdk

import groovy.test.GroovyTestCase

import java.beans.PropertyChangeListener

class ObservableTest extends GroovyTestCase {

    void testObservableList() {
        // tag::observable_list[]

        def event                                       // <1>
        def listener = {
            if (it instanceof ObservableList.ElementEvent)  {  // <2>
                event = it
            }
        } as PropertyChangeListener


        def observable = [1, 2, 3] as ObservableList    // <3>
        observable.addPropertyChangeListener(listener)  // <4>

        observable.add 42                               // <5>

        assert event instanceof ObservableList.ElementAddedEvent

        def elementAddedEvent = event as ObservableList.ElementAddedEvent
        assert elementAddedEvent.changeType == ObservableList.ChangeType.ADDED
        assert elementAddedEvent.index == 3
        assert elementAddedEvent.oldValue == null
        assert elementAddedEvent.newValue == 42
        // end::observable_list[]
    }

    void testObservableListAndClear() {
        // tag::observable_list_clear[]
        def event
        def listener = {
            if (it instanceof ObservableList.ElementEvent)  {
                event = it
            }
        } as PropertyChangeListener


        def observable = [1, 2, 3] as ObservableList
        observable.addPropertyChangeListener(listener)

        observable.clear()

        assert event instanceof ObservableList.ElementClearedEvent

        def elementClearedEvent = event as ObservableList.ElementClearedEvent
        assert elementClearedEvent.values == [1, 2, 3]
        assert observable.size() == 0
        // end::observable_list_clear[]
    }
}
