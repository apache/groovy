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
package groovy.bugs

import gls.CompilableTestSupport

class Groovy6792Bug extends CompilableTestSupport {
    void testMethodWithSpecialCharsInName() {
        assertScript """
            class Foo {
                static ",{}()|!?foo@#\\\$%^&*-=]\\\\bar'\\""(){ Foo.name }
            }
            assert Foo.",{}()|!?foo@#\\\$%^&*-=]\\\\bar'\\""() == 'Foo'
        """
    }

    void testMethodWithInvalidName() {
        // currently groovy.compiler.strictNames is experimental
        System.setProperty('groovy.compiler.strictNames', 'true')
        def message = shouldNotCompile """
            class Foo {
                def "bar.baz"(){}
            }
        """
        assert message.contains("You are not allowed to have '.' in a method name")
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown()
        System.setProperty('groovy.compiler.strictNames', 'false')
    }
}
