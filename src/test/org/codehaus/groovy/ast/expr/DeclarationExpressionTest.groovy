/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.groovy.ast.expr

import org.junit.Test

final class DeclarationExpressionTest {

    @Test
    void getTextForBasicDeclaration() {
        def ast = macro {
            String greeting = 'hello'
        }

        assert ast.text == 'String greeting = hello'
    }

    @Test
    void getTextForDynamicDeclaration() {
        def ast = macro {
            def greeting = 'hello'
        }

        assert ast.text == 'def greeting = hello'
    }

    @Test
    void getTextForMultipleDeclaration() {
        def ast = macro {
            def (one, two) = ['1', '2']
        }

        assert ast.text == 'def (one, two) = [1, 2]'
    }

    @Test
    void getTextForMultipleMixedDeclaration() {
        def ast = macro {
            def (String one, two) = ['1', '2']
        }

        assert ast.text == 'def (String one, two) = [1, 2]'
    }

    @Test
    void getTextForMultipleTypedDeclaration() {
        def ast = macro {
            def (String one, CharSequence two) = ['1', '2']
        }

        assert ast.text == 'def (String one, CharSequence two) = [1, 2]'
    }
}
