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
package groovy.lang

import groovy.test.GroovyTestCase

/**
 * GROOVY-2875: MetaClassRegistryImpl constantMetaClasses map is leaking resources
 * GROOVY-4481: the listener and iterator mechanism over the MetaClassRegistry wasn't working.
 */
class MetaClassRegistryTest extends GroovyTestCase {

    def registry = GroovySystem.metaClassRegistry
    static initSize
    static {
        try {
            Class.forName("org.codehaus.groovy.vmplugin.v7.IndyInterface", true, MetaClassRegistryTest.classLoader)
        } catch (e) {
        }
        initSize = GroovySystem.metaClassRegistry.metaClassRegistryChangeEventListeners.size()
    }

    void testListenerAdditionAndRemoval() {
        def called = null
        def listener = { event -> called = event } as MetaClassRegistryChangeEventListener
        registry.addMetaClassRegistryChangeEventListener listener

        Integer.metaClass.foo = { -> }
        assert 1.foo() == null
        assert called != null

        def listeners = registry.metaClassRegistryChangeEventListeners
        assert listeners.size() == initSize + 1
        registry.removeMetaClassRegistryChangeEventListener(listener)
        assert registry.metaClassRegistryChangeEventListeners.size() == initSize

        def oldCalled = called;
        Integer.metaClass = null

        Integer.metaClass.bar = {}
        assert 1.bar() == null
        shouldFail(MissingMethodException) {
            1.foo()
        }
        assert called == oldCalled

        Integer.metaClass = null
        shouldFail(MissingMethodException) {
            1.bar()
        }
    }

    void testDefaultListenerRemoval() {
        assert registry.metaClassRegistryChangeEventListeners.size() == initSize
        registry.removeMetaClassRegistryChangeEventListener(registry.metaClassRegistryChangeEventListeners[0])
        assert registry.metaClassRegistryChangeEventListeners.size() == initSize
    }

    void testIteratorIteration() {
        // at the start the iteration might show elements, even if
        // they are no longer in use. After they are added to the list,
        // they can not be collected for now.
        def metaClasses = []
        registry.each { metaClasses << it }

        // we add one more constant meta class and then count them to
        // see if the number fits
        Integer.metaClass.foo = {}

        def count = 0;
        registry.each { count++ }
        assert count == 1 + metaClasses.size()

        // we remove the class again, but it might still show up
        // in the list.. so we don't test that
        Integer.metaClass = null
    }

    void testIteratorRemove() {
        Integer.metaClass.foo { -> 1 }
        assert 1.foo() == 1
        for (def it = registry.iterator(); it.hasNext();) {
            it.remove()
        }
        shouldFail(MissingMethodException) {
            1.foo()
        }
    }

    void testAddingAnEventListenerAndChangingAMetaClassWithAnEMC() {
        def events = []
        def listener = { MetaClassRegistryChangeEvent event ->
            events << event
        } as MetaClassRegistryChangeEventListener

        GroovySystem.metaClassRegistry.addMetaClassRegistryChangeEventListener listener
        String.metaClass.foo = { -> "foo" }

        assert "bar".foo() == "foo"
        assert events.size() == 1
        assert events[0].classToUpdate == String
        assert events[0].oldMetaClass != events[0].newMetaClass
        assert events[0].instance == null

        MetaClass reference = events[0].newMetaClass
        String.metaClass = null

        assert events.size() == 2
        assert events[1].classToUpdate == String
        assert events[1].oldMetaClass == reference
        assert events[1].instance == null

        // now test per instance MC change
        def str = 'foo'
        str.metaClass.foo = { 'bar' }
        assert str.foo() == 'bar'
        assert events.size() == 3
        assert events[2].classToUpdate == String
        assert events[2].instance == str

        GroovySystem.metaClassRegistry.removeMetaClassRegistryChangeEventListener listener
        String.metaClass = null
    }

    void testAddingAnEventListenerAndChangingAMetaClassWithANormalMetaClass() {
        def events = []
        def listener = { MetaClassRegistryChangeEvent event ->
            events << event
        } as MetaClassRegistryChangeEventListener

        GroovySystem.metaClassRegistry.addMetaClassRegistryChangeEventListener listener
        def mc = new MetaClassImpl(Double)
        mc.initialize()
        Double.metaClass = mc

        assert events.size() == 1
        assert events[0].classToUpdate == Double

        GroovySystem.metaClassRegistry.removeMetaClassRegistryChangeEventListener listener
        Double.metaClass = null
    }
}
