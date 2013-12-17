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

    @NotYetImplemented
    void testInferenceForDGM_CollectUsingImplicitItAndLUB() {
        assertScript '''
            assert [1234, 3.14].collect { it.intValue() } == [1234,3.14]
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
}

