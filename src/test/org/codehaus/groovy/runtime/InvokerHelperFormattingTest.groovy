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

    public void testFormatLiterals() {
        assert 'null' == InvokerHelper.format(null, false)
        assert '0.5' == InvokerHelper.format(0.5, false)
        assert '2' == InvokerHelper.format(2, false)
        assert '2' == InvokerHelper.format(2L, false)
        assert 'a' == InvokerHelper.format('a', false)
        assert 'a\'b' == InvokerHelper.format('a\'b', false)
        assert 'a\\b' + '' == InvokerHelper.format('a\\b', false)

        assert 'null' == InvokerHelper.format(null, true)
        assert '0.5' == InvokerHelper.format(0.5, true)
        assert '2' == InvokerHelper.format(2, true)
        assert '2' == InvokerHelper.format(2L, true)
        assert '\'a\'' == InvokerHelper.format('a', true)
        assert '\'a\\\'b\'' + '' == InvokerHelper.format('a\'b', true)
        assert '\'a\\\\b\'' + '' == InvokerHelper.format('a\\b', true)
    }

    public void testPrintSelfContained() {
        List l = [];
        l.add(l)
        assert '[(this Collection)]' == InvokerHelper.toListString(l)

        Map m = [:]
        m.put('x', m)
        assert '[x:(this Map)]' == InvokerHelper.toMapString(m)
    }

}
