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
package org.codehaus.groovy.transform.stc

import groovy.lang.GroovyClassLoader
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.junit.jupiter.api.Test

import java.util.function.Function
import java.util.function.Predicate

import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE
import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsForClassNode

/**
 * The DGM lookup used by the static type checker must keep returning the same
 * named methods after the per-receiver name index is populated, including
 * after the loader's extension cache is dropped and rebuilt.
 */
final class DgmMethodLookupTest {

    private static ClassLoader testLoader() {
        DgmMethodLookupTest.classLoader
    }

    @Test
    void 'named DGM lookup is stable after the name index is built'() {
        def loader = testLoader()
        def first = findDGMMethodsForClassNode(loader, LIST_TYPE, 'each')
        def second = findDGMMethodsForClassNode(loader, LIST_TYPE, 'each')
        assert !first.isEmpty()
        assert first.size() == second.size()
        assert first.every { it.name == 'each' }

        def collect = findDGMMethodsForClassNode(loader, LIST_TYPE, 'collect')
        assert collect.every { it.name == 'collect' }
        assert collect.size() > 0
    }

    @Test
    void 'unknown DGM name is empty'() {
        def missing = findDGMMethodsForClassNode(testLoader(), STRING_TYPE, 'definitelyNotADgmMethod')
        assert missing.isEmpty()
    }

    @Test
    void 'unknown receiver still only returns the requested name'() {
        def unknown = ClassHelper.make('com.example.DoesNotExist')
        def each = findDGMMethodsForClassNode(testLoader(), unknown, 'each')
        assert each.every { it.name == 'each' }
        assert findDGMMethodsForClassNode(testLoader(), unknown, 'definitelyNotADgmMethod').isEmpty()
    }

    @Test
    void 'clearing the extension cache still allows a subsequent lookup'() {
        def loader = new GroovyClassLoader()
        def before = findDGMMethodsForClassNode(loader, OBJECT_TYPE, 'with')
        assert !before.isEmpty()
        StaticTypeCheckingSupport.clearExtensionMethodCache(loader)
        def after = findDGMMethodsForClassNode(loader, OBJECT_TYPE, 'with')
        assert after.size() == before.size()
        assert after.every { it.name == 'with' }
    }

    @Test
    void 'clearing all extension caches still allows a subsequent lookup'() {
        def loader = new GroovyClassLoader()
        def before = findDGMMethodsForClassNode(loader, OBJECT_TYPE, 'with')
        assert !before.isEmpty()
        StaticTypeCheckingSupport.clearExtensionMethodCache()
        def after = findDGMMethodsForClassNode(loader, OBJECT_TYPE, 'with')
        assert after.size() == before.size()
    }

    @Test
    void 'clearing one loader does not drop another loader cache'() {
        def one = new GroovyClassLoader()
        def two = new GroovyClassLoader()
        def fromOne = findDGMMethodsForClassNode(one, OBJECT_TYPE, 'with')
        def fromTwo = findDGMMethodsForClassNode(two, OBJECT_TYPE, 'with')
        assert fromOne.size() == fromTwo.size()

        Set<String> namesTwo = ExtensionMethodCache.INSTANCE.getPreemptiveNames(two)
        StaticTypeCheckingSupport.clearExtensionMethodCache(one)

        def fromTwoAfter = findDGMMethodsForClassNode(two, OBJECT_TYPE, 'with')
        assert fromTwoAfter.size() == fromTwo.size()
        assert ExtensionMethodCache.INSTANCE.getPreemptiveNames(two) == namesTwo

        def fromOneAfter = findDGMMethodsForClassNode(one, OBJECT_TYPE, 'with')
        assert fromOneAfter.size() == fromOne.size()
    }

    @Test
    void 'name index matches a linear scan of the cached list'() {
        def loader = testLoader()
        def objectMethods = ExtensionMethodCache.INSTANCE.get(loader).get(OBJECT_TYPE.name)
        assert objectMethods != null
        assert objectMethods.size() > 1

        def linear = objectMethods.findAll { it.name == 'with' }
        def indexed = ExtensionMethodCache.INSTANCE.get(loader, OBJECT_TYPE.name, 'with')
        assert indexed.size() == linear.size()
        assert indexed.containsAll(linear)
        assert indexed.every { it.name == 'with' }
    }

    @Test
    void 'cached lists and named views are unmodifiable'() {
        def loader = testLoader()
        def objectMethods = ExtensionMethodCache.INSTANCE.get(loader).get(OBJECT_TYPE.name)
        shouldFail(UnsupportedOperationException) { objectMethods.add(null) }
        shouldFail(UnsupportedOperationException) { objectMethods.clear() }
        shouldFail(UnsupportedOperationException) { objectMethods.remove(0) }
        shouldFail(UnsupportedOperationException) { objectMethods.set(0, objectMethods.get(0)) }
        shouldFail(IndexOutOfBoundsException) { objectMethods.get(-1) }
        shouldFail(IndexOutOfBoundsException) { objectMethods.get(objectMethods.size()) }

        def named = ExtensionMethodCache.INSTANCE.get(loader, OBJECT_TYPE.name, 'with')
        assert !named.isEmpty()
        shouldFail(UnsupportedOperationException) { named.add(null) }

        def missing = ExtensionMethodCache.INSTANCE.get(loader, OBJECT_TYPE.name, 'definitelyNotADgmMethod')
        assert missing.isEmpty()
        shouldFail(UnsupportedOperationException) { missing.add(null) }

        def unknownClass = ExtensionMethodCache.INSTANCE.get(loader, 'com.example.DoesNotExist', 'each')
        assert unknownClass.isEmpty()
    }

    @Test
    void 'overloads of the same name are all indexed'() {
        def loader = testLoader()
        def plus = findDGMMethodsForClassNode(loader, STRING_TYPE, 'plus')
        assert plus.size() > 1
        assert plus.every { it.name == 'plus' }

        def objectMethods = ExtensionMethodCache.INSTANCE.get(loader).get(OBJECT_TYPE.name)
        def singletonEntry = objectMethods.countBy { it.name }.find { it.value == 1 }
        assert singletonEntry != null
        def singleton = ExtensionMethodCache.INSTANCE.get(loader, OBJECT_TYPE.name, singletonEntry.key)
        assert singleton.size() == 1
        assert singleton[0].name == singletonEntry.key
        shouldFail(UnsupportedOperationException) { singleton.add(null) }
    }

    @Test
    void 'lookup walks interfaces and superclasses'() {
        def arrayList = ClassHelper.make(ArrayList)
        def each = findDGMMethodsForClassNode(testLoader(), arrayList, 'each')
        assert !each.isEmpty()
        assert each.every { it.name == 'each' }

        def mapEach = findDGMMethodsForClassNode(testLoader(), MAP_TYPE, 'each')
        assert !mapEach.isEmpty()
        assert mapEach.every { it.name == 'each' }
    }

    @Test
    void 'lookup on arrays walks component and Object array helpers'() {
        def loader = testLoader()
        def stringArrayGetAt = findDGMMethodsForClassNode(loader, STRING_TYPE.makeArray(), 'getAt')
        assert stringArrayGetAt.every { it.name == 'getAt' }
        assert !stringArrayGetAt.isEmpty()

        def listArrayGetAt = findDGMMethodsForClassNode(loader, LIST_TYPE.makeArray(), 'getAt')
        assert !listArrayGetAt.isEmpty()
        assert listArrayGetAt.every { it.name == 'getAt' }

        def intArrayGetAt = findDGMMethodsForClassNode(loader, int_TYPE.makeArray(), 'getAt')
        assert !intArrayGetAt.isEmpty()
        assert intArrayGetAt.every { it.name == 'getAt' }

        def objectArrayGetAt = findDGMMethodsForClassNode(loader, OBJECT_TYPE.makeArray(), 'getAt')
        assert !objectArrayGetAt.isEmpty()
    }

    @Test
    void 'lookup on a primitive type still finds Object extensions'() {
        def with = findDGMMethodsForClassNode(testLoader(), int_TYPE, 'with')
        assert !with.isEmpty()
        assert with.every { it.name == 'with' }
    }

    @Test
    void 'named lookup with argument types still selects DGM candidates'() {
        def each = findDGMMethodsByNameAndArguments(testLoader(), LIST_TYPE, 'each', [CLOSURE_TYPE] as ClassNode[])
        assert !each.isEmpty()
        assert each.every { MethodNode it -> it.name == 'each' }

        def missing = findDGMMethodsByNameAndArguments(testLoader(), LIST_TYPE, 'definitelyNotADgmMethod', [CLOSURE_TYPE] as ClassNode[])
        assert missing.isEmpty()
    }

    @Test
    void 'preemptive names rebuild after the loader cache is cleared'() {
        def loader = new GroovyClassLoader()
        def before = ExtensionMethodCache.INSTANCE.getPreemptiveNames(loader)
        assert before.contains('withDefault')
        StaticTypeCheckingSupport.clearExtensionMethodCache(loader)
        def after = ExtensionMethodCache.INSTANCE.getPreemptiveNames(loader)
        assert after == before
    }

    @Test
    void 'abstract extension method cache handles single-name buckets and diverse buckets with MethodsByName'() {
        def singleNameCache = new AbstractExtensionMethodCache() {
            @Override
            protected void addAdditionalClassesToScan(Set<Class> instanceExtClasses, Set<Class> staticExtClasses) {
                instanceExtClasses.add(DefaultGroovyMethods)
            }

            @Override
            protected String getDisablePropertyName() {
                'custom.extension.disable'
            }

            @Override
            protected Predicate<MethodNode> getMethodFilter() {
                { MethodNode m -> false }
            }

            @Override
            protected Function<MethodNode, String> getMethodMapper() {
                { MethodNode m -> m.name } // key is method name, so all methods under a key have the same name
            }
        }

        def loader = testLoader()
        def eachMethods = singleNameCache.get(loader).get('each')
        assert eachMethods != null
        assert !eachMethods.isEmpty()
        assert eachMethods.every { it.name == 'each' }

        // Named lookup via get(loader, key, name) uses MethodsByName fast-path
        def found = singleNameCache.get(loader, 'each', 'each')
        assert found.size() == eachMethods.size()
        assert found == eachMethods

        def missing = singleNameCache.get(loader, 'each', 'definitelyNotADgmMethod')
        assert missing.isEmpty()
    }
}
