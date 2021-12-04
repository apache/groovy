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

    void testSV() {
        assertScript """
        $BASE
        assert SV(num, list, range, string).toString() == 'num=42, list=[1, 2, 3], range=[0, 1, 2, 3, 4, 5], string=foo'
        """
    }

    void testSVInClosure() {
        assertScript """
        $BASE
        def cl = {
            SV(num, list, range, string).toString()
        }

        assert cl().toString() == 'num=42, list=[1, 2, 3], range=[0, 1, 2, 3, 4, 5], string=foo'
        """
    }

    void testList() {
        assertScript """
        $BASE
        assert [SV(num, list), SV(range, string)].toString() == '[num=42, list=[1, 2, 3], range=[0, 1, 2, 3, 4, 5], string=foo]'
        """
    }

    void testSVInclude() {
        assertScript """
        $BASE
        def numSV = SV(num)
        assert SV(numSV, list, range, string).toString() == 'numSV=num=42, list=[1, 2, 3], range=[0, 1, 2, 3, 4, 5], string=foo'
        """
    }

    void testNested() {
        assertScript """
        $BASE
        def result = SV(SV(num), string)
        def strip = 'org.codehaus.groovy.macro.runtime.MacroStub.INSTANCE.'
        assert result - strip == 'macroMethod()=num=42, string=foo'
        """
    }

    void testSVI() {
        assertScript """
        $BASE
        assert SVI(num, list, range, string).toString() == /num=42, list=[1, 2, 3], range=0..5, string='foo'/
        """
    }

    void testSVD() {
        assertScript """
        $BASE
        def result = SVD(num, list, range, string)
        def trimmed = result.replaceAll(/@[^>]+/, '@...')
        assert trimmed == /num=<java.lang.Integer@...>, list=<java.util.ArrayList@...>, range=<groovy.lang.IntRange@...>, string=<java.lang.String@...>/
        """
    }

    void testNV() {
        assertScript """
        $BASE
        assert NV(num).toString() == 'num=42'
        """
    }

    void testNVL() {
        assertScript """
        $BASE
        assert NVL(num, list, range, string).toString() == "[num=42, list=[1, 2, 3], range=0..5, string='foo']"
        """
    }

}
