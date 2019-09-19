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
package groovy.util

import groovy.test.GroovyTestCase

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ObservableListTest extends GroovyTestCase {
    void testFireEvent_add_withoutTest() {
        def list = new ObservableList()
        def contentListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'

        list << value1
        assertNotNull(contentListener.event)
        assertEquals(list, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value1, contentListener.event.newValue)
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(0i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)

        list << value2
        assertNotNull(contentListener.event)
        assertEquals(list, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value2, contentListener.event.newValue)

        contentListener.event = null
        list[1] = value1
        assertNotNull(contentListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(value2, contentListener.event.oldValue)
        assertEquals(value1, contentListener.event.newValue)
        assertEquals(1, contentListener.event.index)

        contentListener.event = null
        list[0] = value1
        assertNull(contentListener.event)
    }

    void testFireEvent_remove() {
        def list = new ObservableList()
        def contentListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'

        list << value1
        assertNotNull(contentListener.event)
        assertEquals(list, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value1, contentListener.event.newValue)
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(0i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)

        list << value2
        assertNotNull(contentListener.event)
        assertEquals(list, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value2, contentListener.event.newValue)
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(2i, sizeListener.event.newValue)

        list.remove(value2)
        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableList.ElementRemovedEvent)
        assertEquals(list, contentListener.event.source)
        assertEquals(value2, contentListener.event.oldValue)
        assertEquals(1, contentListener.event.index)
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(2i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)

        list.remove(value1)
        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableList.ElementRemovedEvent)
        assertEquals(list, contentListener.event.source)
        assertEquals(value1, contentListener.event.oldValue)
        assertEquals(0, contentListener.event.index)
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(0i, sizeListener.event.newValue)
    }

    void testFireEvent_clear() {
        def list = new ObservableList()
        def contentListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'
        list << value1
        list << value2

        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(2i, sizeListener.event.newValue)

        list.clear()

        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableList.ElementClearedEvent)
        assertEquals(list, contentListener.event.source)
        def values = contentListener.event.values
        assertNotNull(values)
        assertEquals(2, values.size())
        assertEquals(value1, values[0])
        assertEquals(value2, values[1])
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(2i, sizeListener.event.oldValue)
        assertEquals(0i, sizeListener.event.newValue)
    }

    void testFireEvent_addAll() {
        def list = new ObservableList()
        def contentListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        list << value1

        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(0i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)
        assertEquals(0i, contentListener.event.index)

        def value2 = 'value2'
        list.addAll([value2, value1])

        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableList.MultiElementAddedEvent)
        assertEquals(list, contentListener.event.source)
        def values = contentListener.event.values
        assertNotNull(values)
        assertEquals(2, values.size())
        assertEquals(value2, values[0])
        assertEquals(value1, values[1])
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(3i, sizeListener.event.newValue)
        assertEquals(1i, contentListener.event.index)
    }

    void testFireEvent_removeAll() {
        def list = new ObservableList()
        def contentListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'
        list << value1
        list << value2

        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(2i, sizeListener.event.newValue)

        list.removeAll([value2])

        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableList.MultiElementRemovedEvent)
        assertEquals(list, contentListener.event.source)
        def values = contentListener.event.values
        assertNotNull(values)
        assertEquals(1, values.size())
        assertEquals(value2, values[0])
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(2i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)
    }

    void testFireEvent_retainAll() {
        def list = new ObservableList()
        def contentListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'
        def value3 = 'value3'
        list << value1
        list << value2
        list << value3

        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(2i, sizeListener.event.oldValue)
        assertEquals(3i, sizeListener.event.newValue)

        list.retainAll([value2])

        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableList.MultiElementRemovedEvent)
        assertEquals(list, contentListener.event.source)
        def values = contentListener.event.values
        assertNotNull(values)
        assertEquals(2, values.size())
        assertEquals(value1, values[0])
        assertEquals(value3, values[1])
        assertNotNull(sizeListener.event)
        assertEquals(list, contentListener.event.source)
        assertEquals(3i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)
    }

    void testFireEvent_withTest() {
        def list = new ObservableList({ !(it instanceof String) })
        def contentListener = new SampleListPropertyChangeListener()
        list.addPropertyChangeListener(ObservableList.CONTENT_PROPERTY, contentListener)

        def value1 = 1
        def value2 = 'value2'
        list << value1
        assertNotNull(contentListener.event)
        assertEquals(list, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value1, contentListener.event.newValue)

        contentListener.event = null
        list << value2
        assertNull(contentListener.event)
    }

    void testSort_Groovy4937() {
        def list = [3, 2, 1] as ObservableList
        list = list.sort()
        assert list == [1, 2, 3]
    }

    void testListIterator() {
        def list = [1, 2, 3, 4, 5] as ObservableList
        assert list.listIterator(2).collect { it } == [3, 4, 5]
        assert list.listIterator().collect { it } == [1, 2, 3, 4, 5]
    }

        void testRetainAllBugGroovy4699() {
            def list = new ObservableList(['test', 'test2'])
            def contentListener = new SampleListPropertyChangeListener()
            list.addPropertyChangeListener(ObservableList.CONTENT_PROPERTY, contentListener)
            def sizeListener = new SampleListPropertyChangeListener()
            list.addPropertyChangeListener(ObservableList.SIZE_PROPERTY, sizeListener)
            list.retainAll { false }
            assert list.isEmpty()

            // we are storing one event, so should see second element removed
            assertNotNull(contentListener.event)
            contentListener.event.with {
                assert it instanceof ObservableList.ElementRemovedEvent
                assert source == list
                assert oldValue == 'test2'
                assert index == 0
            }
            // and size property changed from 1 to 0
            assertNotNull(sizeListener.event)
            sizeListener.event.with {
                assert it instanceof java.beans.PropertyChangeEvent
                assert source == list
                assert propertyName == 'size'
                assert oldValue == 1
                assert newValue == 0
            }
        }
}

class SampleListPropertyChangeListener implements PropertyChangeListener {
    PropertyChangeEvent event

    public void propertyChange(PropertyChangeEvent evt) {
        event = evt
    }
}
