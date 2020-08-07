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
package org.apache.groovy.macrolib

import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic

@CompileStatic
class MacroLibTest extends GroovyTestCase {

    def BASE = '''
    def num = 42
    def list = [1 ,2, 3]
    def range = 0..5
    def string = 'foo'
    '''

    void testNV() {
        assertScript """
        $BASE
        assert NV(num, list, range, string).toString() == 'num=42, list=[1, 2, 3], range=[0, 1, 2, 3, 4, 5], string=foo'
        """
    }

    void testNVInClosure() {
        assertScript """
        $BASE
        def cl = {
            NV(num, list, range, string).toString()
        }

        assert cl().toString() == 'num=42, list=[1, 2, 3], range=[0, 1, 2, 3, 4, 5], string=foo'
        """
    }

    void testList() {
        assertScript """
        $BASE
        assert [NV(num, list), NV(range, string)].toString() == '[num=42, list=[1, 2, 3], range=[0, 1, 2, 3, 4, 5], string=foo]'
        """
    }

    void testNVInclude() {
        assertScript """
        $BASE
        def numNV = NV(num)
        assert NV(numNV, list, range, string).toString() == 'numNV=num=42, list=[1, 2, 3], range=[0, 1, 2, 3, 4, 5], string=foo'
        """
    }

    void testNested() {
        assertScript """
        $BASE
        def result = NV(NV(num), string)
        def strip = 'org.codehaus.groovy.macro.runtime.MacroStub.INSTANCE.'
        assert result - strip == 'macroMethod()=num=42, string=foo'
        """
    }

    void testNVI() {
        assertScript """
        $BASE
        assert NVI(num, list, range, string).toString() == /num=42, list=[1, 2, 3], range=0..5, string='foo'/
        """
    }

    void testNVD() {
        assertScript """
        $BASE
        def result = NVD(num, list, range, string)
        def trimmed = result.replaceAll(/@[^>]+/, '@...')
        assert trimmed == /num=<java.lang.Integer@...>, list=<java.util.ArrayList@...>, range=<groovy.lang.IntRange@...>, string=<java.lang.String@...>/
        """
    }
}
