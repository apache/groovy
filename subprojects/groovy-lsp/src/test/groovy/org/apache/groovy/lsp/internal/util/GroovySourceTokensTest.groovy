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
package org.apache.groovy.lsp.internal.util

import org.junit.jupiter.api.Test

final class GroovySourceTokensTest {

    @Test
    void coveringAndTokenizeGuardEmptyInput() {
        assert GroovySourceTokens.covering(null, 0) == null
        assert GroovySourceTokens.covering('', 0) == null
        assert GroovySourceTokens.covering('abc', -1) == null
        assert GroovySourceTokens.covering('abc', 3) == null
        assert GroovySourceTokens.tokenize(null).isEmpty()
        assert GroovySourceTokens.tokenize('').isEmpty()
        assert !GroovySourceTokens.isStructuralRbrace(null, 0)
        assert !GroovySourceTokens.isStructuralRbrace('}', -1)
        assert GroovySourceTokens.tokenize('class C {}')
    }

    @Test
    void structuralRbraceIsTrueForClosers() {
        assert GroovySourceTokens.isStructuralRbrace('class C { }', 'class C { }'.lastIndexOf('}'))
        assert GroovySourceTokens.isStructuralRbrace('def c = { }', 'def c = { }'.lastIndexOf('}'))
        assert GroovySourceTokens.isStructuralRbrace('"${x}"', '"${x}"'.indexOf('}'))
    }

    @Test
    void structuralRbraceIsFalseInsideLiteralsAndComments() {
        assert !GroovySourceTokens.isStructuralRbrace('def s = "foo}bar"', 'def s = "foo}bar"'.indexOf('}'))
        assert !GroovySourceTokens.isStructuralRbrace("def s = 'foo}bar'", "def s = 'foo}bar'".indexOf('}'))
        assert !GroovySourceTokens.isStructuralRbrace('def s = """foo}bar"""', 'def s = """foo}bar"""'.indexOf('}'))
        assert !GroovySourceTokens.isStructuralRbrace('def s = /foo}bar/', 'def s = /foo}bar/'.indexOf('}'))
        assert !GroovySourceTokens.isStructuralRbrace('// trailing }', '// trailing }'.indexOf('}'))
        assert !GroovySourceTokens.isStructuralRbrace('/* block } */', '/* block } */'.indexOf('}'))
        assert !GroovySourceTokens.isStructuralRbrace("def c = '}'", "def c = '}'".indexOf('}'))
    }
}
