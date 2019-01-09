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

import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

class ObservableSetTests extends GroovyTestCase {
    void testFireEvent_add_withoutTest() {
        def set = new ObservableSet()
        def contentListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'

        set << value1
        assertNotNull(contentListener.event)
        assertEquals(set, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value1, contentListener.event.newValue)
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(0i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)

        set << value2
        assertNotNull(contentListener.event)
        assertEquals(set, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value2, contentListener.event.newValue)
    }

    void testFireEvent_remove() {
        def set = new ObservableSet()
        def contentListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'

        set << value1
        assertNotNull(contentListener.event)
        assertEquals(set, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value1, contentListener.event.newValue)
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(0i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)

        set << value2
        assertNotNull(contentListener.event)
        assertEquals(set, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value2, contentListener.event.newValue)
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(2i, sizeListener.event.newValue)

        set.remove(value2)
        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableSet.ElementRemovedEvent)
        assertEquals(set, contentListener.event.source)
        assertEquals(value2, contentListener.event.oldValue)
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(2i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)

        set.remove(value1)
        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableSet.ElementRemovedEvent)
        assertEquals(set, contentListener.event.source)
        assertEquals(value1, contentListener.event.oldValue)
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(0i, sizeListener.event.newValue)
    }

    void testFireEvent_clear() {
        def set = new ObservableSet()
        def contentListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'
        set << value1
        set << value2

        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(2i, sizeListener.event.newValue)

        set.clear()

        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableSet.ElementClearedEvent)
        assertEquals(set, contentListener.event.source)
        def values = contentListener.event.values
        assertNotNull(values)
        assertEquals(2, values.size())
        assertTrue(values.contains(value1))
        assertTrue(values.contains(value2))
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(2i, sizeListener.event.oldValue)
        assertEquals(0i, sizeListener.event.newValue)
    }

    void testFireEvent_addAll() {
        def set = new ObservableSet()
        def contentListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'
        set << value1

        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(0i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)

        set.addAll([value1, value2])

        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableSet.MultiElementAddedEvent)
        assertEquals(set, contentListener.event.source)
        def values = contentListener.event.values
        assertNotNull(values)
        assertEquals(1, values.size())
        assertFalse(values.contains(value1))
        assertTrue(values.contains(value2))
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assert 1i == sizeListener.event.oldValue
        assert 2i == sizeListener.event.newValue
    }

    void testFireEvent_removeAll() {
        def set = new ObservableSet()
        def contentListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'
        set << value1
        set << value2

        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(1i, sizeListener.event.oldValue)
        assertEquals(2i, sizeListener.event.newValue)

        set.removeAll([value2])

        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableSet.MultiElementRemovedEvent)
        assertEquals(set, contentListener.event.source)
        def values = contentListener.event.values
        assertNotNull(values)
        assertEquals(1, values.size())
        assertEquals(value2, values[0])
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(2i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)
    }

    void testFireEvent_retainAll() {
        def set = new ObservableSet()
        def contentListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.SIZE_PROPERTY, sizeListener)

        def value1 = 'value1'
        def value2 = 'value2'
        def value3 = 'value3'
        set << value1
        set << value2
        set << value3

        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(2i, sizeListener.event.oldValue)
        assertEquals(3i, sizeListener.event.newValue)

        set.retainAll([value2])

        assertNotNull(contentListener.event)
        assertTrue(contentListener.event instanceof ObservableSet.MultiElementRemovedEvent)
        assertEquals(set, contentListener.event.source)
        def values = contentListener.event.values
        assertNotNull(values)
        assertEquals(2, values.size())
        assertTrue(values.contains(value1))
        assertTrue(values.contains(value3))
        assertNotNull(sizeListener.event)
        assertEquals(set, contentListener.event.source)
        assertEquals(3i, sizeListener.event.oldValue)
        assertEquals(1i, sizeListener.event.newValue)
    }

    void testFireEvent_withTest() {
        def set = new ObservableSet({ !(it instanceof String) })
        def contentListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.CONTENT_PROPERTY, contentListener)

        def value1 = 1
        def value2 = 'value2'
        set << value1
        assertNotNull(contentListener.event)
        assertEquals(set, contentListener.event.source)
        assertNull(contentListener.event.oldValue)
        assertEquals(value1, contentListener.event.newValue)

        contentListener.event = null
        set << value2
        assertNull(contentListener.event)
    }

    void testSort_Groovy4937() {
        def set = [3, 2, 1] as ObservableSet
        set = set.sort()
        assert set == [1, 2, 3]
    }

    void testRetainAllBugGroovy4699() {
        def set = new ObservableSet(['test', 'test2'] as Set)
        def contentListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.CONTENT_PROPERTY, contentListener)
        def sizeListener = new SampleSetPropertyChangeListener()
        set.addPropertyChangeListener(ObservableSet.SIZE_PROPERTY, sizeListener)
        set.retainAll { false }
        assert set.isEmpty()

        // we are storing one event, so should see second element removed
        assertNotNull(contentListener.event)
        contentListener.event.with {
            assert it instanceof ObservableSet.ElementRemovedEvent
            assert source == set
            assert oldValue == 'test2'
        }
        // and size property changed from 1 to 0
        assertNotNull(sizeListener.event)
        sizeListener.event.with {
            assert it instanceof java.beans.PropertyChangeEvent
            assert source == set
            assert propertyName == 'size'
            assert oldValue == 1
            assert newValue == 0
        }
    }
}

class SampleSetPropertyChangeListener implements PropertyChangeListener {
    PropertyChangeEvent event

    void propertyChange(PropertyChangeEvent evt) {
        event = evt
    }
}
