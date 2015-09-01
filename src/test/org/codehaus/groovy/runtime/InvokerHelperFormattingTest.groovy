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

class InvokerHelperFormattingTest extends GroovyTestCase {

    public void testPrintLiterals() {
        assert 'null' == InvokerHelper.toString(null)
        assert '0.5' == InvokerHelper.toString(0.5)
        assert '2' == InvokerHelper.toString(2)
        assert '2' == InvokerHelper.toString(2L)
        assert 'a' == InvokerHelper.toString('a')
        assert 'a\'b' == InvokerHelper.toString('a\'b')
    }

    public void testPrintLists() {
        assert '[]' == InvokerHelper.toString([])
        assert '[1, true, a, \'b\']' == InvokerHelper.toString([1, true, 'a, \'b\''])
    }

    public void testPrintRanges() {
        assert '[1, 2, 3, 4]' == InvokerHelper.toString(1..4)
        assert "[a'b, a'c, a'd]" == InvokerHelper.toString('a\'b'..'a\'d')
    }

    public void testPrintArrays() {
        assert "[a, a'b]" == InvokerHelper.toString(['a', 'a\'b'] as String[])
        assert "[a, a'b]" == InvokerHelper.toString(['a', 'a\'b'] as Object[])
    }

    public void testPrintMaps() {
        assert '[:]' == InvokerHelper.toString([:])
        assert "[a'b:1, 2:b'c]" == InvokerHelper.toString(['a\'b':1, 2:'b\'c'])
    }

    public void testEmbedded() {
        List list = []
        list.add(['a\'b': 'c\'d'])
        list.add(['e', 'f', 'g'])
        list.add(5..9)
        list.add('fog'..'fop')
        list.add(['h', 'i'] as String[])
        list.add([10, 11] as int[])
        assert "[key:[[a'b:c'd], [e, f, g], 5..9, fog..fop, [h, i], [10, 11]]]" == InvokerHelper.toString([key: list])
    }

    public void testPrintSelfContained() {
        List l = [];
        l.add(l)
        assert '[(this Collection)]' == InvokerHelper.toString(l)

        Map m = [:]
        m.put('x', m)
        assert '[x:(this Map)]' == InvokerHelper.toString(m)
    }

}
