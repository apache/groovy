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
package org.codehaus.groovy.runtime.memoize

class MemoizeTest extends AbstractMemoizeTestCase {

    Closure buildMemoizeClosure(Closure cl) {
        cl.memoize()
    }

    void testMemoizeWithInject() {
        int maxExecutionCount = 0
        Closure max = { int a, int b ->
            maxExecutionCount++
            Math.max(a, b)
        }.memoize()
        int minExecutionCount = 0
        Closure min = { int a, int b ->
            minExecutionCount++
            Math.min(a, b)
        }.memoize()
        100.times {
            max.call(max.call(1, 2), 3)
        }
        100.times {
            [1, 2, 3].inject(min)
        }
        assert maxExecutionCount == 2
        assert minExecutionCount == 2
    }

    // GROOVY-6584
    void testMemoizeFunctionClosure() {
        int timesMethodBodyExecuted = 0
        def lst = []
        lst.metaClass.getTotalCount = {
            ++timesMethodBodyExecuted
            12
        }.memoize()

        assert lst.getTotalCount() == 12
        assert lst.getTotalCount() == 12
        assert timesMethodBodyExecuted == 1

        timesMethodBodyExecuted = 0
        def code = { String dept, int id ->
            ++timesMethodBodyExecuted
            [dept, "${id}"]
        }

        lst.metaClass.getUsersByDeptAndMgrId = code.memoize()

        assert lst.getUsersByDeptAndMgrId('123', 555) == ['123', '555']
        assert lst.getUsersByDeptAndMgrId('456', 999) == ['456', '999']

        assert timesMethodBodyExecuted == 2

        assert lst.getUsersByDeptAndMgrId('123', 555) == ['123', '555']
        assert lst.getUsersByDeptAndMgrId('456', 999) == ['456', '999']

        assert lst.getUsersByDeptAndMgrId('123', 555) == ['123', '555']
        assert lst.getUsersByDeptAndMgrId('456', 999) == ['456', '999']

        assert timesMethodBodyExecuted == 2

        // test SoftReferenceMemoizeFunction
        lst.metaClass.getUsersByDeptAndMgrId = code.memoizeAtLeast(4)

        assert lst.getUsersByDeptAndMgrId('123', 555) == ['123', '555']
        assert lst.getUsersByDeptAndMgrId('456', 999) == ['456', '999']

        assert lst.getUsersByDeptAndMgrId('123', 555) == ['123', '555']
        assert lst.getUsersByDeptAndMgrId('456', 999) == ['456', '999']

        assert timesMethodBodyExecuted == 4
    }

    void testMemoizeClosureParameters() {
        def clo = { String a, Date b, c -> 42 }.memoize()
        assert clo.maximumNumberOfParameters == 3
        assert clo.parameterTypes[0] == String
        assert clo.parameterTypes[1] == Date
        assert clo.parameterTypes[2] == Object

        // test SoftReferenceMemoizeFunction
        clo = { String a, Date b, c -> 42 }.memoizeAtLeast(2)
        assert clo.maximumNumberOfParameters == 3
        assert clo.parameterTypes[0] == String
        assert clo.parameterTypes[1] == Date
        assert clo.parameterTypes[2] == Object
    }

    // GROOVY-6175
    void testMemoizeClosureAsProperty() {
        def c = new ClassWithMemoizeClosureProperty();

        assert c.mc() == 1
        assert c.mc() == 1

        assert c.mcSoftRef() == 1
        assert c.mcSoftRef() == 1
    }

    // GROOVY-8486
    void testMemoizeConcurrently() {
        assertScript '''
        // http://groovy.329449.n5.nabble.com/ConcurrentModificationException-with-use-of-memoize-tp5736788.html
        
        class Utils { 
            public static int cnt = 0
            
            public static synchronized void increment() {
                cnt++
            }
            
            public static synchronized int getCnt() {
                return cnt
            }
        
            public static final Closure powerSet =  { Collection things -> 
                increment()
        
                def Set objSets = things.collect { [it] as Set } 
                def Set resultSet = [[] as Set] 
                def finalResult = objSets.inject(resultSet) { rez, objSet -> 
                        def newMemberSets = rez.collect { it -> (it + objSet) as Set } 
                        rez.addAll(newMemberSets) 
                        rez 
                } 
            }.memoize() 
    
            public static Collection combinations(Collection objs, int n) { (n < 1 || n > objs.size()) ? null : powerSet(objs)?.findAll { it.size() == n } } 
        } 
        
        def threadList = (0..<10).collect {
            Thread.start { 
                Collection things = [
                        [1, 2, 3],
                        [1, 2],
                        [1, 2],
                        [2, 3],
                        [2, 3],
                        [3, 4],
                        [3, 4],
                        [5, 6, 7, 8]
                ]
                Utils.combinations(things, 2) 
            } 
        }
        threadList.addAll((0..<5).collect {
            Thread.start {
                def things = [
                    [1, 2, 3],
                    [1, 2, 3],
                    [1, 2],
                    [2, 3, 4],
                    [2, 3, 4],
                    [3, 4],
                    [3, 4, 5],
                    [5, 6, 7, 8]
                ]
                Utils.combinations(things, 2) 
            }
        })
        
        threadList*.join()
        
        assert 2 == Utils.getCnt()
        '''
    }

    private static class ClassWithMemoizeClosureProperty {
        int timesCalled, timesCalledSoftRef
        def mc = {
            ++timesCalled
        }.memoize()

        def mcSoftRef = {
            ++timesCalledSoftRef
        }.memoizeAtLeast(4)
    }
}
