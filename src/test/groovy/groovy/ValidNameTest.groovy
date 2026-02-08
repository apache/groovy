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

class ValidNameTest extends GroovyTestCase {

    void testFieldNamesWithDollar() {
        def $foo$foo = '3foo'
        def foo$foo = '2foo'
        def $foo = '1foo'
        def foo = '0foo'
        assert $foo$foo == '3foo'
        assert foo$foo == '2foo'
        assert $foo == '1foo'
        assert foo == '0foo'
        assert "$foo$foo${foo$foo}${$foo$foo}${$foo}$foo" == '0foo0foo2foo3foo1foo0foo'
    }

    void testClassAndMethodNamesWithDollar() {
        assert new $Temp().$method() == 'bar'
    }
}

class $Temp {
    def $method() { 'bar' }
}
