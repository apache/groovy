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

class ObservableMapTest extends GroovyTestCase {
    void testFireEvent_withoutTest() {
        def map = new ObservableMap()
        def propertyListener = new SamplePropertyChangeListener()
        map.addPropertyChangeListener(propertyListener)
        def sizeListener = new SamplePropertyChangeListener(false)
        map.addPropertyChangeListener(ObservableMap.SIZE_PROPERTY, sizeListener)

        def key = 'key'
        def value1 = 'value1'
        def value2 = 'value2'
        map[key] = null
        assertNotNull(propertyListener.event)
        assertTrue(propertyListener.event instanceof ObservableMap.PropertyAddedEvent)
        assertEquals(key, propertyListener.event.propertyName)
        assertNull(propertyListener.event.oldValue)
        assertNull(propertyListener.event.newValue)
        assert sizeListener.event
        assert 0 == sizeListener.event.oldValue
        assert 1 == sizeListener.event.newValue

        sizeListener.event = null
        map[key] = value1
        assertNotNull(propertyListener.event)
        assertEquals(map, propertyListener.event.source)
        assertEquals(key, propertyListener.event.propertyName)
        assertNull(propertyListener.event.oldValue)
        assertEquals(value1, propertyListener.event.newValue)
        assert !sizeListener.event

        map[key] = value2
        assertNotNull(propertyListener.event)
        assertEquals(map, propertyListener.event.source)
        assertEquals(key, propertyListener.event.propertyName)
        assertEquals(value1, propertyListener.event.oldValue)
        assertEquals(value2, propertyListener.event.newValue)
        assert !sizeListener.event

        propertyListener.event = null
        map[key] = value2
        assertNull(propertyListener.event)

    }

    void testFireEvent_removeKey() {
        def map = new ObservableMap()
        def propertyListener = new SamplePropertyChangeListener()
        map.addPropertyChangeListener(propertyListener)
        def sizeListener = new SamplePropertyChangeListener(false)
        map.addPropertyChangeListener(ObservableMap.SIZE_PROPERTY, sizeListener)

        def key = 'key'
        def value1 = 'value1'
        def value2 = 'value2'
        map[key] = null
        assertNotNull(propertyListener.event)
        assertTrue(propertyListener.event instanceof ObservableMap.PropertyAddedEvent)
        assertEquals(key, propertyListener.event.propertyName)
        assertNull(propertyListener.event.newValue)
        assert sizeListener.event
        assert 0 == sizeListener.event.oldValue
        assert 1 == sizeListener.event.newValue

        sizeListener.event = null
        map[key] = value1
        assertNotNull(propertyListener.event)
        assertEquals(map, propertyListener.event.source)
        assertEquals(key, propertyListener.event.propertyName)
        assertNull(propertyListener.event.oldValue)
        assertEquals(value1, propertyListener.event.newValue)
        assert !sizeListener.event

        map.remove(key)
        assertNotNull(propertyListener.event)
        assertTrue(propertyListener.event instanceof ObservableMap.PropertyRemovedEvent)
        assertEquals(map, propertyListener.event.source)
        assertEquals(key, propertyListener.event.propertyName)
        assertEquals(value1, propertyListener.event.oldValue)
        assert sizeListener.event
        assert 1 == sizeListener.event.oldValue
        assert 0 == sizeListener.event.newValue
    }

    void testFireEvent_clearMap() {
        def map = new ObservableMap()
        def propertyListener = new SamplePropertyChangeListener()
        map.addPropertyChangeListener(propertyListener)
        def sizeListener = new SamplePropertyChangeListener(false)
        map.addPropertyChangeListener(ObservableMap.SIZE_PROPERTY, sizeListener)

        def key1 = 'key1'
        def key2 = 'key2'
        def value1 = 'value1'
        def value2 = 'value2'
        map[key1] = value1
        map[key2] = value2

        assert sizeListener.event
        assert 1 == sizeListener.event.oldValue
        assert 2 == sizeListener.event.newValue

        map.clear()

        assertNotNull(propertyListener.event)
        assert propertyListener.event instanceof ObservableMap.PropertyClearedEvent
        assertEquals(map, propertyListener.event.source)
        def values = propertyListener.event.values
        assertNotNull(values)
        assertEquals(2, values.size())
        assertEquals(value1, values[key1])
        assertEquals(value2, values[key2])
        assert sizeListener.event
        assert 2 == sizeListener.event.oldValue
        assert 0 == sizeListener.event.newValue
    }

    void testFireEvent_putAll() {
        def map = new ObservableMap()
        def propertyListener = new SamplePropertyChangeListener()
        map.addPropertyChangeListener(propertyListener)
        def sizeListener = new SamplePropertyChangeListener(false)
        map.addPropertyChangeListener(ObservableMap.SIZE_PROPERTY, sizeListener)

        def key1 = 'key1'
        def key2 = 'key2'
        def value1 = 'value1'
        def value2 = 'value2'
        map[key1] = null

        assert sizeListener.event
        assert 0 == sizeListener.event.oldValue
        assert 1 == sizeListener.event.newValue

        map.putAll([key1: value1, key2: value2])

        assertNotNull(propertyListener.event)
        assert propertyListener.event instanceof ObservableMap.MultiPropertyEvent
        assertEquals(map, propertyListener.event.source)
        assertEquals(2, propertyListener.event.events.size())
        assertTrue(propertyListener.event.events[0] instanceof ObservableMap.PropertyUpdatedEvent)
        assertEquals(key1, propertyListener.event.events[0].propertyName)
        assertEquals(value1, propertyListener.event.events[0].newValue)
        assertTrue(propertyListener.event.events[1] instanceof ObservableMap.PropertyAddedEvent)
        assertEquals(key2, propertyListener.event.events[1].propertyName)
        assertEquals(value2, propertyListener.event.events[1].newValue)

        assert sizeListener.event
        assert 1 == sizeListener.event.oldValue
        assert 2 == sizeListener.event.newValue
    }

    void testFireEvent_withTest() {
        def map = new ObservableMap({ it != 'value2' })
        def propertyListener = new SamplePropertyChangeListener()
        map.addPropertyChangeListener(propertyListener)
        def sizeListener = new SamplePropertyChangeListener(false)
        map.addPropertyChangeListener(ObservableMap.SIZE_PROPERTY, sizeListener)

        def key = 'key'
        def value1 = 'value1'
        def value2 = 'value2'
        map[key] = value1
        assertNotNull(propertyListener.event)
        assertEquals(map, propertyListener.event.source)
        assertEquals(key, propertyListener.event.propertyName)
        assertEquals(value1, propertyListener.event.newValue)

        propertyListener.event = null
        map[key] = value2
        assertNull(propertyListener.event)
    }

    void testFireEvent_withTestOnKey() {
        def map = new ObservableMap({ name, value -> name != 'key' })
        def propertyListener = new SamplePropertyChangeListener()
        map.addPropertyChangeListener(propertyListener)
        def sizeListener = new SamplePropertyChangeListener(false)
        map.addPropertyChangeListener(ObservableMap.SIZE_PROPERTY, sizeListener)

        def key = 'key'
        def value1 = 'value1'
        def value2 = 'value2'
        map[key] = value1
        assertNull(propertyListener.event)
        map[key] = value2
        assertNull(propertyListener.event)

        map['key2'] = value1
        assertNotNull(propertyListener.event)
        assertEquals(map, propertyListener.event.source)
        assertEquals('key2', propertyListener.event.propertyName)
        assertEquals(value1, propertyListener.event.newValue)
    }
}

class SamplePropertyChangeListener implements PropertyChangeListener {
    PropertyChangeEvent event
    private final boolean skip

    SamplePropertyChangeListener() {
        this(true)
    }

    SamplePropertyChangeListener(boolean skip) {
        this.skip = skip;
    }

    void propertyChange(PropertyChangeEvent evt) {
        if (skip && evt.propertyName in [ObservableMap.SIZE_PROPERTY]) return
        event = evt
    }
}
