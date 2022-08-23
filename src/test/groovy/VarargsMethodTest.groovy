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

import org.junit.Test

/**
 * Tests the feature that the length of arguments can be variable when invoking
 * methods with or without parameters.
 */
final class VarargsMethodTest {

    Object nullProperty = null

    int varargsOnlyMethod(Object[] args) {
        // GROOVY-1023 (Java 5 feature)
        //     If this method having varargs is invoked with no parameter,
        //     then args is not null, but an array of length 0.
        // GROOVY-1026 (Java 5 feature)
        //     If this method having varargs is invoked with one parameter
        //     null, then args is null, and so -1 is returned here.
        args == null ? -1 : args.length
    }

    int varargsLastMethod(arg, Object[] args) {
        // GROOVY-1026 (Java 5 feature)
        //     If this method having varargs is invoked with two parameters
        //     1 and null, then args is null, and so -1 is returned here.
        args == null ? -1 : args.length
    }

    def varargsOverloads1(arg) {
        '1'
    }
    def varargsOverloads1(arg, Object[] args) {
        "1+${args?.length}".toString()
    }

    def varargsOverloads2(String key, Object[] args) {
        "key=$key, args=$args".toString()
    }
    def varargsOverloads2(String key, Object[] args, Object[] parts) {
        "key=$key, args=$args, parts=$parts".toString()
    }
    def varargsOverloads2(String key, Object[] args, String[] names) {
        "key=$key, args=$args, names=$names".toString()
    }

    //--------------------------------------------------------------------------

    @Test
    void testVarargsOnly() {
        assert varargsOnlyMethod('') == 1
        assert varargsOnlyMethod(11) == 1
        assert varargsOnlyMethod('','') == 2
        assert varargsOnlyMethod(['','']) == 1
        assert varargsOnlyMethod(*['','']) == 2
        assert varargsOnlyMethod(['',''] as Object[]) == 2

        // GROOVY-1023
        assert varargsOnlyMethod() == 0

        // GROOVY-1026
        assert varargsOnlyMethod(null) == -1
        assert varargsOnlyMethod(null, null) == 2

        // GROOVY-6146
        assert varargsOnlyMethod((Object[])null) == -1
        assert varargsOnlyMethod(null as Object[]) == -1
        assert varargsOnlyMethod((Object)null) == -1 // TODO: 1
        assert varargsOnlyMethod(null as Object) == -1 // TODO: 1

        // GROOVY-10099
        Object[] array = null
        assert varargsOnlyMethod(array) == -1
        Object value = null
        assert varargsOnlyMethod(value) == -1 // TODO: 1
        // non-array POGO property == null
        assert varargsOnlyMethod(nullProperty) == -1 // TODO: 1
        // non-array POJO property (ie: via getter) returns null
        assert varargsOnlyMethod(URI.create('http://example.com').query) == -1 // TODO: 1
        // non-array returning method returns null
        assert varargsOnlyMethod(Objects.toString(null, null)) == -1 // TODO: 1
    }

    @Test
    void testVarargsLast() {
        assert varargsLastMethod('') == 0
        assert varargsLastMethod(1) == 0
        assert varargsLastMethod('','') == 1
        assert varargsLastMethod('','','') == 2
        assert varargsLastMethod('',['',''] ) == 1
        assert varargsLastMethod('',['',''] as Object[]) == 2
        assert varargsLastMethod('',*['',''] ) == 2

        // GROOVY-1026
        assert varargsLastMethod('',null) == -1
        assert varargsLastMethod('',null, null) == 2
    }

    @Test
    void testVarargsSelection1() {
        assert varargsOverloads1('') == '1'
        assert varargsOverloads1('','') == '1+1'
        assert varargsOverloads1('',null) == '1+null'
        assert varargsOverloads1('','',null) == '1+2'
        assert varargsOverloads1('',new Object[0]) == '1+0'
    }

    @Test // GROOVY-8737
    void testVarargsSelection2() {
        assert varargsOverloads2('hello', new Object[]{'world'}) == 'key=hello, args=[world]'

        assert varargsOverloads2('hello', new String[]{'world'}) == 'key=hello, args=[world]'

        assert varargsOverloads2("${'hello'}", 'world') == 'key=hello, args=[world]'

        assert varargsOverloads2('hello', 'world') == 'key=hello, args=[world]'

        assert varargsOverloads2('hello', new String[]{'there'}, 'Steve') == 'key=hello, args=[there], names=[Steve]'
    }
}
