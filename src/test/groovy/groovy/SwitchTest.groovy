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
package groovy

import groovy.test.GroovyTestCase

class SwitchTest extends GroovyTestCase {

    void testSwitch() {
        callSwitch("foo", "foo")
        callSwitch("bar", "barfoo")
        callSwitch("barbar", "barfoo")
        callSwitch("dummy", "d*")
        callSwitch("xyz", "xyzDefault")
        callSwitch("zzz", "Default")
        callSwitch(4, "List")
        callSwitch(5, "List")
        callSwitch(6, "List")
        callSwitch("inList", "List")
        callSwitch(1, "Integer")
        callSwitch(1.2, "Number")
        callSwitch(null, "null")
        callSwitch([1, 2, 3], "ListInterface")
    }

    def callSwitch(x, expected) {
        def result = ""
        switch (x) {
            case null:
                result = "null"
                break

            case ~/d.*/:
                result = "d*"
                break

            case "barbar":
            case "bar":
                result = result + "bar"

            case "foo":
                result = result + "foo"
                break

            case [4, 5, 6, 'inList']:
                result = "List"
                break

            case Integer:
                result = "Integer"
                break

            case Number:
                result = "Number"
                break

            case List:
                result = "ListInterface"
                break

            case "xyz":
                result = result + "xyz"

            default:
                result = result + "Default"
                // unnecessary just testing compiler
                break
        }
        assert result == expected, "Expected $expected but found $result when calling switch with $x"
    }

    // test the continue in switch, which should jump to the while start
    void testSwitchScope() {
        def i = 0
        def j = 0
        while (true) {
            ++i;
            switch (i) {
                case 4:
                    continue
                case 5:
                    break;
                default:
                    j += i;
                    break;
            }
            if (i == 5) break;
        }
        assert j == 6
    }

    void testSwitchWithClosure() {
        switch (0) {
            case {true}: break
            default: assert false
        }
        switch (0) {
            case {false}: assert false
        }
        switch (0) {
            case {it == 0}: break
            default: assert false
        }
        switch (0) {
            case {candidate -> candidate == 0}: break
            default: assert false
        }
    }

    /** older versions of groovy produced a ListExpression for a
        fall through. the result was that it worked in some cases
        and in other cases not. For example not for patterns */
    void testFallthroughToOtherCaseWithNoCode() {
        def a = ['FileName.java', 'AnotherFileName.groovy', 'foo']
        def i = 0
        a.each {
            switch (it) {
                case ~/.*java$/:
                case ~/.*groovy$/:
                    i++
                    break
                default:
                    i += 10
            }
        }
        assertEquals 12, i
    }

    void testFallthroughToOtherCaseWithCode() {
        def a = ['FileName.java', 'AnotherFileName.groovy', 'foo']
        def i = 0
        a.each {
            switch (it) {
                case ~/.*java$/:
                    i += 5
                case ~/.*groovy$/:
                    i++
                    break
                default:
                    i += 10
            }
        }
        assertEquals 17, i
    }

    void testFallthroughToDefaultWithNoCode() {
        def a = ['FileName.java', 'AnotherFileName.groovy', 'foo']
        def i = 0
        a.each {
            switch (it) {
                case ~/.*java$/:
                    i++
                    break
                case ~/.*groovy$/:
                default:
                    i += 10
            }
        }
        assertEquals 21, i
    }

    void testFallthroughToDefaultWithCode() {
        def a = ['FileName.java', 'AnotherFileName.groovy', 'foo']
        def i = 0
        a.each {
            switch (it) {
                case ~/.*java$/:
                    i++
                    break
                case ~/.*groovy$/:
                    i += 5
                default:
                    i += 10
            }
        }
        assertEquals 26, i
    }

    void testSwitchNoStatementsAtEnd() {
        def a = ['FileName.java', 'AnotherFileName.groovy', 'foo']
        def i = 0
        a.each {
            switch (it) {
                case ~/.*java$/:
                    i++
                    break
                case ~/.*groovy$/: break
            }
        }
        assertEquals 1, i
        i = 0
        a.each {
            switch (it) {
                case ~/.*java$/:
                    i++
                    break
                default: break
            }
        }
        assertEquals 1, i
    }

    void testSwitchReturn1() {
        assertScript '''
            def test(x) {
               switch (x) {
                 case 'a': 'letter A'; break
                 case 'b': 'letter B'; break
                 default : 'Unknown letter'
               }
            }
            assert test('a') == 'letter A'
            assert test('b') == 'letter B'
            assert test('z') == 'Unknown letter'
        '''
    }

    // GROOVY-3789
    void testSwitchReturn2() {
        assertScript '''
            def test = { ->
                if ( 0 ) { 10 }
                else { 20 }
            }
            assert test() == 20
        '''
        assertScript '''
            def test = { ->
                switch ( 0 ) {
                  case 0 : 10 ; break
                  default : 20 ; break
                }
            }
            assert test() == 10
        '''
    }

    // GROOVY-4727
    void testSwitchReturn3() {
        assertScript '''
            def test(x,y) {
                switch (x) {
                  case 'x1':
                    switch (y) {
                      case 'y1':
                        'r1'
                        break
                      case 'y2':
                        'r2'
                        break
                    }
                    // no break
                }
            }
            assert test('x1','y1') == 'r1'
        '''
    }

    // GROOVY-9880
    void testSwitchReturn4() {
        assertScript '''
            def test(sb) {
                switch ('value') {
                  case 'value':
                    sb.append('foo')
                    if (false) sb.append('X')
                    // implicit "else ;"
                    break
                  default:
                    sb.append('bar')
                }
            }
            def sb = new StringBuilder()
            test(sb); assert sb.toString() == 'foo'
        '''
    }

    // GROOVY-9896
    void testSwitchReturn5() {
        assertScript '''
            def test(x) {
              switch(x) {
               case 1:
                'a'
                break
               case 2:
                'b'
                break
               case 3:
                'c'
              }
            }
            assert test(1) == 'a'
            assert test(2) == 'b'
            assert test(3) == 'c'
            assert test(4) == null
        '''
    }
}
