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
package org.codehaus.groovy.runtime

import groovy.test.GroovyTestCase

class InvokerHelperFormattingTest extends GroovyTestCase {

    private static class ExceptionOnToString implements Comparable {
        public static final String MATCHER = '<org.codehaus.groovy.runtime.InvokerHelperFormattingTest\\$ExceptionOnToString@[0-9a-z]+>'

        @Override
        String toString() {
            throw new UnsupportedOperationException("toString() throws Exception for Testing")
        }

        @Override
        int compareTo(Object o) {
            throw new UnsupportedOperationException("compareTo() throws Exception for Testing")
        }
    }

    void testToStringLiterals() {
        assert 'null' == InvokerHelper.toString(null)
        assert '0.5' == InvokerHelper.toString(0.5)
        assert '2' == InvokerHelper.toString(2)
        assert '2' == InvokerHelper.toString(2L)
        assert 'a' == InvokerHelper.toString('a')
        assert 'a\'b' == InvokerHelper.toString('a\'b')

    }

    void testToStringThrows() {
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.toString(new ExceptionOnToString())
        }
    }

    void testFormat() {
        assert 'null' == InvokerHelper.format(null, false)
        assert '0.5' == InvokerHelper.format(0.5, false)
        assert '2' == InvokerHelper.format(2, false)
        assert '2' == InvokerHelper.format(2L, false)
        assert 'a' == InvokerHelper.format('a', false)
        assert 'a\'b' == InvokerHelper.format('a\'b', false)
        assert 'a\\b' + '' == InvokerHelper.format('a\\b', false)
        assert '\'a\\\'b\'' == InvokerHelper.format('a\'b', true)
        
        assert 'null' == InvokerHelper.format(null, true)
        assert '0.5' == InvokerHelper.format(0.5, true)
        assert '2' == InvokerHelper.format(2, true)
        assert '2' == InvokerHelper.format(2L, true)
        assert '\'a\'' == InvokerHelper.format('a', true)
        assert '\'a\\\'b\'' + '' == InvokerHelper.format('a\'b', true)
        assert '\'a\\\\b\'' + '' == InvokerHelper.format('a\\b', true)
        
        Object eObject = new ExceptionOnToString()
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.format(eObject, false)
        }
        assert InvokerHelper.format(new ExceptionOnToString(), false, -1, true) =~ (ExceptionOnToString.MATCHER)
    }

    void testFormatRanges() {
        assert '1..4' == InvokerHelper.format(1..4, false)
        assert "a'b..a'd" == InvokerHelper.format('a\'b'..'a\'d', false)
        assert "[1..4]" == InvokerHelper.format([1..4], false)
        assert "[a'b..a'd]" == InvokerHelper.format(['a\'b'..'a\'d'], false)

        // verbose
        assert '1..4' == InvokerHelper.format(1..4, true)
        assert "'a\\'b'..'a\\'d'" == InvokerHelper.format('a\'b'..'a\'d', true)
        assert "[1..4]" == InvokerHelper.format([1..4], true)
        assert "['a\\'b'..'a\\'d']" == InvokerHelper.format(['a\'b'..'a\'d'], true)

        Object eObject = new ExceptionOnToString()
        Object eObject2 = new ExceptionOnToString()
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.format(eObject..eObject2)
        }

        assert InvokerHelper.format(eObject..eObject, false, -1, true) == '<ObjectRange@????>'
    }

    void testToStringLists() {
        assert '[]' == InvokerHelper.toString([])
        assert '[1, true, a, \'b\']' == InvokerHelper.toString([1, true, 'a, \'b\''])
    }

    void testToListString() {
        assert '[]' == InvokerHelper.toString([])
        assert '[1, true, a, \'b\']' == InvokerHelper.toListString([1, true, 'a, \'b\''])
        assert '[1, ...]' == InvokerHelper.toListString([1, true, 'a, \'b\''], 2)
        Object eObject = new ExceptionOnToString()
        assert InvokerHelper.toListString([eObject], -1, true) =~ "\\[${ExceptionOnToString.MATCHER}\\]"
        List list = [[z: eObject]]
        assert InvokerHelper.toListString(list, -1, true) =~ "\\[\\[z:${ExceptionOnToString.MATCHER}\\]\\]"
        // even when throwing object is deeply nested, exception handling only happens in Collection
        list = [[x: [y: [z: eObject, a: 2, b: 4]]]]
        assert InvokerHelper.toListString(list, -1, true) =~ "\\[\\[x:\\[y:\\[z:${ExceptionOnToString.MATCHER}, a:2, b:4\\]\\]\\]\\]"

        list = [[eObject, 1, 2]]
        // safe argument is not passed on recursively
        assert InvokerHelper.toListString(list, -1, true) =~ "\\[\\[${ExceptionOnToString.MATCHER}, 1, 2\\]\\]"
        list = [[[[[eObject, 1, 2]]]]]
        assert InvokerHelper.toListString(list, -1, true) =~ "\\[\\[\\[\\[\\[${ExceptionOnToString.MATCHER}, 1, 2\\]\\]\\]\\]\\]"

        shouldFail(UnsupportedOperationException) {
            InvokerHelper.toListString([eObject], -1, false)
        }
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.toListString([eObject], -1)
        }
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.toListString([eObject])
        }
    }

    void testToStringRanges() {
        assert '1..4' == InvokerHelper.toString(1..4)
        assert "a'b..a'd" == InvokerHelper.toString('a\'b'..'a\'d')
        assert "[1..4]" == InvokerHelper.toString([1..4])
        assert "[a'b..a'd]" == InvokerHelper.toString(['a\'b'..'a\'d'])
    }

    void testToStringArrays() {
        assert "[a, a'b]" == InvokerHelper.toString(['a', 'a\'b'] as String[])
        assert "[a, a'b]" == InvokerHelper.toString(['a', 'a\'b'] as Object[])
    }

    void testFormatArrays() {
        assert "[a, a'b]" == InvokerHelper.format(['a', 'a\'b'] as String[], false)
        assert "[a, a'b]" == InvokerHelper.format(['a', 'a\'b'] as Object[], false)
        assert "['a', 'a\\'b']" == InvokerHelper.format(['a', 'a\'b'] as String[], true)
        assert "['a', 'a\\'b']" == InvokerHelper.format(['a', 'a\'b'] as Object[], true)
        Object eObject = new ExceptionOnToString()
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.format([eObject] as ExceptionOnToString[], false)
        }

        assert InvokerHelper.format([new ExceptionOnToString()] as Object[], false, -1, true) =~ "\\[${ExceptionOnToString.MATCHER}\\]"
    }

    void testToStringMaps() {
        assert '[:]' == InvokerHelper.toString([:])
        assert "[a'b:1, 2:b'c]" == InvokerHelper.toString(['a\'b':1, 2:'b\'c'])
    }

    void testFormatMaps() {
        assert '[:]' == InvokerHelper.format([:], false)
        assert "[a'b:1, 2:b'c]" == InvokerHelper.format(['a\'b':1, 2:'b\'c'], false)
        assert "['a\\'b':1, 2:'b\\'c']" == InvokerHelper.format(['a\'b':1, 2:'b\'c'], true, -1, true)

        Object eObject = new ExceptionOnToString()
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.format([foo: eObject], false)
        }

        assert InvokerHelper.format([foo: eObject], false, -1, true) =~ "\\[foo:${ExceptionOnToString.MATCHER}\\]"
        assert InvokerHelper.format([foo: eObject], true, -1, true) =~ "\\['foo':${ExceptionOnToString.MATCHER}\\]"
        Map m = [:]
        m.put(eObject, eObject)
        assert InvokerHelper.format(m, false, -1, true) =~ "\\[${ExceptionOnToString.MATCHER}:${ExceptionOnToString.MATCHER}\\]"
    }

    void testToMapString() {
        assert '[:]' == InvokerHelper.toMapString([:])
        assert "[a'b:1, 2:b'c]" == InvokerHelper.toMapString(['a\'b':1, 2:'b\'c'])
        assert "[a'b:1, ...]" == InvokerHelper.toMapString(['a\'b':1, 2:'b\'c'], 2)
        Object eObject = new ExceptionOnToString()
        // no safe / verbose toMapString method provided
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.toMapString([x: eObject])
        }
        shouldFail(UnsupportedOperationException) {
            InvokerHelper.toMapString([x: eObject], 2)
        }
    }

    void testEmbedded() {
        List list = []
        list.add(['a\'b': 'c\'d'])
        list.add(['e', 'f', 'g'])
        list.add(5..9)
        list.add('fog'..'fop')
        list.add(['h', 'i'] as String[])
        list.add([10, 11] as int[])
        assert "[key:[[a'b:c'd], [e, f, g], 5..9, fog..fop, [h, i], [10, 11]]]" == InvokerHelper.toString([key: list])

        assert "['key':[['a\\'b':'c\\'d'], ['e', 'f', 'g'], 5..9, 'fog'..'fop', ['h', 'i'], [10, 11]]]" == InvokerHelper.format([key:list], true, -1, false)
    }

    void testToStringSelfContained() {
        List l = [];
        l.add(l)
        assert '[(this Collection)]' == InvokerHelper.toString(l)

        Map m = [:]
        m.put(m, m)
        assert '[(this Map):(this Map)]' == InvokerHelper.toString(m)
    }

}
