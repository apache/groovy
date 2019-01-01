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

/**
 * Check that Object.identity(Closure) method works as expected
 */
class IdentityClosureTest extends GroovyTestCase {
    
    def foo = [[1,2,3],[4,5,6],[7,8,9]]
    def bar = " bar "
    def mooky = 1

    /** most useful perceived usecase, almost like with(expr) */
    void testIdentity0() {
        assert " bar " == bar

        bar.toUpperCase().trim().identity{
            assert "BAR" == it
            assert 3 == it.size()
            assert it.indexOf("A") > 0
        }
    }  

    /** check the basics */
    void testIdentity1() {
        mooky.identity{ spooky->
            assert spooky == mooky
        }
    }

    /** test temp shortcut to an element of an array */
    void testIdentity2() {
        assert 6 == foo[1][2]
        
        foo[1].identity{ myArray->
            myArray[2] = 12
        }
        
        assert 12 == foo[1][2]
    }

    /** check nested identity usage */
    void testIdentity3() {
        mooky.toString().identity{ m->
            assert "1" == m
            m += "234567890"
            m.identity{ n->
                assert "1234567890" == n
            }
        }
    }

    /** Test the closure delegate */
    void testClosureDelegate1() {
        bar.toUpperCase().trim().identity{
            assert "BAR" == it
            assert 3 == size()
            assert indexOf("A") > 0
        }
    }

    /** Test the closure delegate with Expandos */
    void testClosureDelegate2() {
        def a = new Expando()
        a.foobar = "foobar"
        a.barfoo = 555
        a.identity{
            assert foobar == "foobar"
            assert barfoo == 555
        }
    }
}
