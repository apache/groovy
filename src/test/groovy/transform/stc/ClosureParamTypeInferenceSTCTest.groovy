/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

import groovy.transform.NotYetImplemented

/**
 * Unit tests for static type checking : closure parameter type inference.
 *
 * @author Cedric Champeau
 */
class ClosureParamTypeInferenceSTCTest extends StaticTypeCheckingTestCase {
    void testInferenceForDGM_CollectUsingExplicitIt() {
        assertScript '''
            ['a','b'].collect { it -> it.toUpperCase() }
        '''
    }

    void testInferenceForDGM_CollectUsingExplicitItAndIncorrectType() {
        shouldFailWithMessages '''
            ['a','b'].collect { Date it -> it.toUpperCase() }
        ''', 'Expected parameter of type java.lang.String but got java.util.Date'
    }

    void testInferenceForDGM_CollectUsingImplicitIt() {
        assertScript '''
            ['a','b'].collect { it.toUpperCase() }
        '''
    }

    void testInferenceForDGM_eachUsingExplicitIt() {
        assertScript '''
            ['a','b'].each { it -> it.toUpperCase() }
        '''
    }

    void testInferenceForDGM_eachUsingImplicitIt() {
        assertScript '''
            ['a','b'].each { it.toUpperCase() }
        '''
    }

    void testInferenceForDGM_CollectUsingImplicitItAndLUB() {
        assertScript '''
            assert [1234, 3.14].collect { it.intValue() } == [1234,3]
        '''
    }

    void testInferenceForDGM_countUsingFirstSignature() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { k,v -> v>1 } == 2
        '''
    }

    void testInferenceForDGM_countUsingSecondSignature() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { e -> e.value>1 } == 2
        '''
    }

    void testInferenceForDGM_countUsingSecondSignatureAndImplicitIt() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { it.value>1 } == 2
        '''
    }

    void testInferenceForDGM_collectManyUsingFirstSignature() {
        assertScript '''
def map = [bread:3, milk:5, butter:2]
def result = map.collectMany{ k, v -> k.startsWith('b') ? k.toList() : [] }
assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
'''
    }

    void testInferenceForDGM_collectManyUsingSecondSignature() {
        assertScript '''
def map = [bread:3, milk:5, butter:2]
def result = map.collectMany{ e -> e.key.startsWith('b') ? e.key.toList() : [] }
assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
'''
    }

    void testInferenceForDGM_collectManyUsingSecondSignatureAndImplicitIt() {
        assertScript '''
def map = [bread:3, milk:5, butter:2]
def result = map.collectMany{ it.key.startsWith('b') ? it.key.toList() : [] }
assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
'''
    }

    void testInferenceForDGM_Collect2() {
        assertScript '''
def items = []
['a','b','c'].collect(items) { it.toUpperCase() }
'''
    }

    void testInferenceForDGM_CollectMap() {
        assertScript '''
        assert [a: 'foo',b:'bar'].collect { k,v -> k+v } == ['afoo','bbar']
        assert [a: 'foo',b:'bar'].collect { e -> e.key+e.value } == ['afoo','bbar']
        assert [a: 'foo',b:'bar'].collect { it.key+it.value } == ['afoo','bbar']
'''
    }

    void testInferenceForDGM_CollectMapWithCollection() {
        assertScript '''
        assert [a: 'foo',b:'bar'].collect([]) { k,v -> k+v } == ['afoo','bbar']
        assert [a: 'foo',b:'bar'].collect([]) { e -> e.key+e.value } == ['afoo','bbar']
        assert [a: 'foo',b:'bar'].collect([]) { it.key+it.value } == ['afoo','bbar']
'''
    }

    void testInferenceForDGM_collectEntries() {
        assertScript '''
            assert ['a','b','c'].collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesWithCollector() {
        assertScript '''
            assert ['a','b','c'].collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesIterator() {
        assertScript '''
            assert ['a','b','c'].iterator().collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesIteratorWithCollector() {
        assertScript '''
            assert ['a','b','c'].iterator().collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesOnMap() {
        assertScript '''
            assert [a:'a',b:'b',c:'c'].collectEntries { k,v -> [k+k, v.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries { e -> [e.key+e.key, e.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries { [it.key+it.key, it.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
'''
    }

    void testInferenceForDGM_collectEntriesOnMapWithCollector() {
        assertScript '''
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { k,v -> [k+k, v.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { e -> [e.key+e.key, e.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { [it.key+it.key, it.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
'''
    }

    void testInferenceForDGM_collectEntriesOnArray() {
        assertScript '''
            String[] array = ['a','b','c']
            assert array.collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesOnArrayWithCollector() {
        assertScript '''
            String[] array = ['a','b','c']
            assert array.collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectManyOnIterable() {
        assertScript '''
            assert (0..5).collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

    void testInferenceForDGM_collectManyOnIterator() {
        assertScript '''
            assert (0..5).iterator().collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

    void testInferenceForDGM_collectManyOnIterableWithCollector() {
        assertScript '''
            assert (0..5).collectMany([]) { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

    void testInferenceForDGM_collectManyOnMap() {
        assertScript '''
            assert [a:0,b:1,c:2].collectMany { k,v -> [v, 2*v ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany { e -> [e.value, 2*e.value ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany { [it.value, 2*it.value ]} == [0,0,1,2,2,4]
'''
    }

    void testInferenceForDGM_collectManyOnMapWithCollector() {
        assertScript '''
            assert [a:0,b:1,c:2].collectMany([]) { k,v -> [v, 2*v ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany([]) { e -> [e.value, 2*e.value ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany([]) { [it.value, 2*it.value ]} == [0,0,1,2,2,4]
'''
    }

    void testInferenceForDGM_collectManyOnArray() {
        assertScript '''
            Integer[] arr = (0..5)
            assert arr.collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

}

