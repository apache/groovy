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

class IfElseTest extends GroovyTestCase {

    void testIf_NoElse() {

        def x = false

        if ( true ) {
            x = true
        }

        assert x == true
    }

    void testIf_WithElse_MatchIf() {

        def x = false
        def y = false

        if ( true ) {
            x = true
        } else {
            y = true
        }

        assert x == true
        assert y == false

    }

    void testIf_WithElse_MatchElse() {

        def x = false
        def y = false

        if ( false ) {
            x = true
        } else {
            y = true
        }

        assertEquals( false, x )
        assertEquals( true, y )
    }

    void testIf_WithElseIf_MatchIf() {

        def x = false
        def y = false

        if ( true ) {
            x = true
        } else if ( false ) {
            y = true
        }

        assert x == true
        assert y == false
    }

    void testIf_WithElseIf_MatchElseIf() {

        def x = false
        def y = false

        if ( false ) {
            x = true
        } else if ( true ) {
            y = true
        }

        assertEquals( false, x )
        assertEquals( true, y )
    }

    void testIf_WithElseIf_WithElse_MatchIf() {

        def x = false
        def y = false
        def z = false

        if ( true ) {
            x = true
        } else if ( false ) {
            y = true
        } else {
            z = true
        }

        assert x == true
        assert y == false
        assertEquals( false, z )
    }

    void testIf_WithElseIf_WithElse_MatchElseIf() {

        def x = false
        def y = false
        def z = false

        if ( false ) {
            x = true
        } else if ( true ) {
            y = true
        } else {
            z = true
        }

        assertEquals( false, x )
        assertEquals( true, y )
        assertEquals( false, z )
    }

    void testIf_WithElseIf_WithElse_MatchElse() {

        def x = false
        def y = false
        def z = false

        if ( false ) {
            x = true
        } else if ( false ) {
            y = true
        } else {
            z = true
        }

        assertEquals( false, x )
        assert y == false
        assertEquals( true, z )
    }
}
