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
package org.apache.groovy.plugin

import groovy.test.GroovyTestCase

import static java.util.Collections.emptyMap

class GroovyRunnerRegistryTest extends GroovyTestCase {

    Map<String, GroovyRunner> knownRunners = [
            Junit3TestRunner: DefaultRunners.junit3TestRunner(),
            Junit3SuiteRunner: DefaultRunners.junit3SuiteRunner(),
            Junit4TestRunner: DefaultRunners.junit4TestRunner()
    ]

    GroovyRunnerRegistry registry = new GroovyRunnerRegistry(knownRunners)

    void testRegistryContainsDefaultRunners() {
        for (runner in GroovyRunnerRegistry.getInstance()) {
            knownRunners.remove(runner.getClass().getSimpleName())
        }
        assert knownRunners.isEmpty()
    }

    void testDefaultRunnersAreLoaded() {
        def reg = GroovyRunnerRegistry.getInstance()
        reg.clear()
        assert !reg.isEmpty()
        testRegistryContainsDefaultRunners()
    }

    void testCustomRunner() {
        DummyRunner customRunner = new DummyRunner()
        GroovyRunnerRegistry realRegistry = GroovyRunnerRegistry.getInstance()
        realRegistry.put('DummyRunner', customRunner)
        try {
            def result = new GroovyShell().run('class DummyClass {}', 'DummyClass.groovy', [])
            assert result == 'DummyClass was run'
        } finally {
            realRegistry.remove('DummyRunner')
        }
    }

    void testLegacyCustomRunner() {
        LegacyDummyRunner customRunner = new LegacyDummyRunner()
        GroovyRunnerRegistry realRegistry = GroovyRunnerRegistry.getInstance()
        realRegistry.put('LegacyDummyRunner', customRunner)
        try {
            def result = new GroovyShell().run('class LegacyDummyClass {}', 'LegacyDummyClass.groovy', [])
            assert result == 'LegacyDummyClass was run'
        } finally {
            realRegistry.remove('LegacyDummyRunner')
        }
    }

    void testSize() {
        assert registry.size() == knownRunners.size()
    }

    void testIsEmpty() {
        assert !registry.isEmpty()
        assert new GroovyRunnerRegistry(emptyMap()).isEmpty()
    }

    void testContainsKey() {
        assert registry.containsKey('Junit4TestRunner')
        assert !registry.containsKey(null)
    }

    void testContainsValue() {
        assert registry.containsValue(knownRunners.get('Junit4TestRunner'))
        assert !registry.containsValue(null)
    }

    void testGet() {
        assert registry.get('Junit4TestRunner') == knownRunners.get('Junit4TestRunner')
        assert !registry.get(null)
    }

    void testPut() {
        DummyRunner runner = new DummyRunner()
        registry.put('DummyRunner', runner)

        assert registry.get('DummyRunner').is(runner)
        assert registry.put('DummyRunner', new DummyRunner()).is(runner)

        assert !registry.put(null, runner)
        assert !registry.put('DummyRunner', null)
        assert !registry.put(null, null)
    }

    void testRemove() {
        DummyRunner runner = new DummyRunner()
        registry.put('DummyRunner', runner)

        assert registry.remove('DummyRunner').is(runner)
        assert !registry.remove('NotExistsRunner')
        assert !registry.remove(null)
    }

    void testClear() {
        registry.put('DummyRunner', new DummyRunner())
        assert registry.size() == knownRunners.size() + 1
        registry.clear()
        assert registry.size() == knownRunners.size()
    }

    void testPutAll() {
        shouldFail(NullPointerException) {
            registry.putAll(null as Map<String, GroovyRunner>)
        }

        Map<String, GroovyRunner> map = ['Dummy': new DummyRunner(), 'Dummy2': null]
        map[null] = new DummyRunner()

        assert registry.size() == knownRunners.size()
        registry.putAll(map)
        assert registry.size() == knownRunners.size() + 1
    }

    void testAsIterable() {
        Iterator itr = registry.iterator()
        assert itr.hasNext()
        assert itr.next() == knownRunners['Junit3TestRunner']
        shouldFail(UnsupportedOperationException) {
            itr.remove()
        }
    }

    void testValues() {
        Collection<GroovyRunner> values = registry.values()
        assert values.size() == 3
        assert values.contains(knownRunners['Junit3TestRunner'])
        shouldFail(UnsupportedOperationException) {
            values.remove(knownRunners['Junit3TestRunner'])
        }
    }

    void testKeySet() {
        Set<String> keySet = registry.keySet()
        assert keySet.size() == 3
        assert keySet.contains('Junit3TestRunner')
        shouldFail(UnsupportedOperationException) {
            keySet.remove('Junit3TestRunner')
        }
    }

    void testEntrySet() {
        Set<Map.Entry<String, GroovyRunner>> entries = registry.entrySet()
        assert entries.size() == 3
        assert entries.find { it.key == 'Junit3TestRunner' }
        shouldFail(UnsupportedOperationException) {
            entries.remove(entries.find { it.key == 'Junit3TestRunner' })
        }
    }

    static class DummyRunner implements GroovyRunner {
        @Override
        boolean canRun(Class<?> scriptClass, GroovyClassLoader loader) {
            return scriptClass.getSimpleName() == 'DummyClass'
        }

        @Override
        Object run(Class<?> scriptClass, GroovyClassLoader loader) {
            return 'DummyClass was run'
        }
    }

    static class LegacyDummyRunner implements org.codehaus.groovy.plugin.GroovyRunner {
        @Override
        boolean canRun(Class<?> scriptClass, GroovyClassLoader loader) {
            return scriptClass.getSimpleName() == 'LegacyDummyClass'
        }

        @Override
        Object run(Class<?> scriptClass, GroovyClassLoader loader) {
            return 'LegacyDummyClass was run'
        }
    }

}
