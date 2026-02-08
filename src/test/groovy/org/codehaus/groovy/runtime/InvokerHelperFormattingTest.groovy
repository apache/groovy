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
        assert 'null' == FormatHelper.toString(null)
        assert '0.5' == FormatHelper.toString(0.5)
        assert '2' == FormatHelper.toString(2)
        assert '2' == FormatHelper.toString(2L)
        assert 'a' == FormatHelper.toString('a')
        assert 'a\'b' == FormatHelper.toString('a\'b')

    }

    void testToStringThrows() {
        shouldFail(UnsupportedOperationException) {
            FormatHelper.toString(new ExceptionOnToString())
        }
    }

    void testFormat() {
        assert 'null' == FormatHelper.format(null, false)
        assert '0.5' == FormatHelper.format(0.5, false)
        assert '2' == FormatHelper.format(2, false)
        assert '2' == FormatHelper.format(2L, false)
        assert 'a' == FormatHelper.format('a', false)
        assert 'a\'b' == FormatHelper.format('a\'b', false)
        assert 'a\\b' + '' == FormatHelper.format('a\\b', false)
        assert '\'a\\\'b\'' == FormatHelper.format('a\'b', true)

        assert 'null' == FormatHelper.format(null, true)
        assert '0.5' == FormatHelper.format(0.5, true)
        assert '2' == FormatHelper.format(2, true)
        assert '2' == FormatHelper.format(2L, true)
        assert '\'a\'' == FormatHelper.format('a', true)
        assert '\'a\\\'b\'' + '' == FormatHelper.format('a\'b', true)
        assert '\'a\\\\b\'' + '' == FormatHelper.format('a\\b', true)

        Object eObject = new ExceptionOnToString()
        shouldFail(UnsupportedOperationException) {
            FormatHelper.format(eObject, false)
        }
        assert FormatHelper.format(new ExceptionOnToString(), false, -1, true) =~ (ExceptionOnToString.MATCHER)
    }

    void testFormatRanges() {
        assert '1..4' == FormatHelper.format(1..4, false)
        assert "a'b..a'd" == FormatHelper.format('a\'b'..'a\'d', false)
        assert "[1..4]" == FormatHelper.format([1..4], false)
        assert "[a'b..a'd]" == FormatHelper.format(['a\'b'..'a\'d'], false)

        // verbose
        assert '1..4' == FormatHelper.format(1..4, true)
        assert "'a\\'b'..'a\\'d'" == FormatHelper.format('a\'b'..'a\'d', true)
        assert "[1..4]" == FormatHelper.format([1..4], true)
        assert "['a\\'b'..'a\\'d']" == FormatHelper.format(['a\'b'..'a\'d'], true)

        Object eObject = new ExceptionOnToString()
        Object eObject2 = new ExceptionOnToString()
        shouldFail(UnsupportedOperationException) {
            FormatHelper.format(eObject..eObject2)
        }

        assert FormatHelper.format(eObject..eObject, false, -1, true) == '<ObjectRange@????>'
    }

    void testToStringLists() {
        assert '[]' == FormatHelper.toString([])
        assert '[1, true, a, \'b\']' == FormatHelper.toString([1, true, 'a, \'b\''])
    }

    void testToListString() {
        assert '[]' == FormatHelper.toString([])
        assert '[1, true, a, \'b\']' == FormatHelper.toListString([1, true, 'a, \'b\''])
        assert '[1, ...]' == FormatHelper.toListString([1, true, 'a, \'b\''], 2)
        Object eObject = new ExceptionOnToString()
        assert FormatHelper.toListString([eObject], -1, true) =~ "\\[${ExceptionOnToString.MATCHER}\\]"
        List list = [[z: eObject]]
        assert FormatHelper.toListString(list, -1, true) =~ "\\[\\[z:${ExceptionOnToString.MATCHER}\\]\\]"
        // even when throwing object is deeply nested, exception handling only happens in Collection
        list = [[x: [y: [z: eObject, a: 2, b: 4]]]]
        assert FormatHelper.toListString(list, -1, true) =~ "\\[\\[x:\\[y:\\[z:${ExceptionOnToString.MATCHER}, a:2, b:4\\]\\]\\]\\]"

        list = [[eObject, 1, 2]]
        // safe argument is not passed on recursively
        assert FormatHelper.toListString(list, -1, true) =~ "\\[\\[${ExceptionOnToString.MATCHER}, 1, 2\\]\\]"
        list = [[[[[eObject, 1, 2]]]]]
        assert FormatHelper.toListString(list, -1, true) =~ "\\[\\[\\[\\[\\[${ExceptionOnToString.MATCHER}, 1, 2\\]\\]\\]\\]\\]"

        shouldFail(UnsupportedOperationException) {
            FormatHelper.toListString([eObject], -1, false)
        }
        shouldFail(UnsupportedOperationException) {
            FormatHelper.toListString([eObject], -1)
        }
        shouldFail(UnsupportedOperationException) {
            FormatHelper.toListString([eObject])
        }
    }

    void testToStringRanges() {
        assert '1..4' == FormatHelper.toString(1..4)
        assert "a'b..a'd" == FormatHelper.toString('a\'b'..'a\'d')
        assert "[1..4]" == FormatHelper.toString([1..4])
        assert "[a'b..a'd]" == FormatHelper.toString(['a\'b'..'a\'d'])
    }

    void testToStringArrays() {
        assert "[a, a'b]" == FormatHelper.toString(['a', 'a\'b'] as String[])
        assert "[a, a'b]" == FormatHelper.toString(['a', 'a\'b'] as Object[])
    }

    void testFormatArrays() {
        assert "[a, a'b]" == FormatHelper.format(['a', 'a\'b'] as String[], false)
        assert "[a, a'b]" == FormatHelper.format(['a', 'a\'b'] as Object[], false)
        assert "['a', 'a\\'b']" == FormatHelper.format(['a', 'a\'b'] as String[], true)
        assert "['a', 'a\\'b']" == FormatHelper.format(['a', 'a\'b'] as Object[], true)
        Object eObject = new ExceptionOnToString()
        shouldFail(UnsupportedOperationException) {
            FormatHelper.format([eObject] as ExceptionOnToString[], false)
        }

        assert FormatHelper.format([new ExceptionOnToString()] as Object[], false, -1, true) =~ "\\[${ExceptionOnToString.MATCHER}\\]"
    }

    void testToStringMaps() {
        assert '[:]' == FormatHelper.toString([:])
        assert "[a'b:1, 2:b'c]" == FormatHelper.toString(['a\'b':1, 2:'b\'c'])
    }

    void testFormatMaps() {
        assert '[:]' == FormatHelper.format([:], false)
        assert "[a'b:1, 2:b'c]" == FormatHelper.format(['a\'b':1, 2:'b\'c'], false)
        assert "['a\\'b':1, 2:'b\\'c']" == FormatHelper.format(['a\'b':1, 2:'b\'c'], true, -1, true)

        Object eObject = new ExceptionOnToString()
        shouldFail(UnsupportedOperationException) {
            FormatHelper.format([foo: eObject], false)
        }

        assert FormatHelper.format([foo: eObject], false, -1, true) =~ "\\[foo:${ExceptionOnToString.MATCHER}\\]"
        assert FormatHelper.format([foo: eObject], true, -1, true) =~ "\\['foo':${ExceptionOnToString.MATCHER}\\]"
        Map m = [:]
        m.put(eObject, eObject)
        assert FormatHelper.format(m, false, -1, true) =~ "\\[${ExceptionOnToString.MATCHER}:${ExceptionOnToString.MATCHER}\\]"
    }

    void testToMapString() {
        assert '[:]' == FormatHelper.toMapString([:])
        assert "[a'b:1, 2:b'c]" == FormatHelper.toMapString(['a\'b':1, 2:'b\'c'])
        assert "[a'b:1, ...]" == FormatHelper.toMapString(['a\'b':1, 2:'b\'c'], 2)
        Object eObject = new ExceptionOnToString()
        // no safe / verbose toMapString method provided
        shouldFail(UnsupportedOperationException) {
            FormatHelper.toMapString([x: eObject])
        }
        shouldFail(UnsupportedOperationException) {
            FormatHelper.toMapString([x: eObject], 2)
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
        assert "[key:[[a'b:c'd], [e, f, g], 5..9, fog..fop, [h, i], [10, 11]]]" == FormatHelper.toString([key: list])

        assert "['key':[['a\\'b':'c\\'d'], ['e', 'f', 'g'], 5..9, 'fog'..'fop', ['h', 'i'], [10, 11]]]" == FormatHelper.format([key:list], true, -1, false)
    }

    void testToStringSelfContained() {
        List l = [];
        l.add(l)
        assert '[(this Collection)]' == FormatHelper.toString(l)

        Map m = [:]
        m.put(m, m)
        assert '[(this Map):(this Map)]' == FormatHelper.toString(m)
    }

}
