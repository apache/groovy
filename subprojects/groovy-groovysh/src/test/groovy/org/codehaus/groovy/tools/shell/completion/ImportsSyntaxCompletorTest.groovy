/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.tools.shell.CompletorTestSupport
import org.codehaus.groovy.tools.shell.Groovysh

import static org.codehaus.groovy.tools.shell.completion.TokenUtilTest.tokenList

class ImportsSyntaxCompletorTest extends CompletorTestSupport {

    void testPackagePattern() {
        assertTrue('import static java.lang.Math' ==~ ImportsSyntaxCompletor.STATIC_IMPORT_PATTERN)
        assertTrue('import static java.lang.Math.max' ==~ ImportsSyntaxCompletor.STATIC_IMPORT_PATTERN)
        assertTrue('import static java.lang.Math.max2' ==~ ImportsSyntaxCompletor.STATIC_IMPORT_PATTERN)
        assertTrue('import static java.lang.Math.*' ==~ ImportsSyntaxCompletor.STATIC_IMPORT_PATTERN)
        assertTrue('import static org.w3c.Math.*' ==~ ImportsSyntaxCompletor.STATIC_IMPORT_PATTERN)
        assertFalse('import static java lang' ==~ ImportsSyntaxCompletor.STATIC_IMPORT_PATTERN)
    }

    void testPreImported() {
        groovyshMocker.demand.getPackageHelper(6) { [getContents: {['Foo']}] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            assertEquals(true, completor.findMatchingPreImportedClasses('Foo', candidates))
            // once for each standard package
            assertEquals(['prefill', 'Foo', 'Foo', 'Foo', 'Foo', 'Foo', 'Foo'], candidates)
        }
    }

    void testPreImportedBigs() {
        groovyshMocker.demand.getPackageHelper(6) { [getContents: {[]}] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            assertEquals(true, completor.findMatchingPreImportedClasses('Big', candidates))
            assertEquals(['prefill', 'BigInteger', 'BigDecimal'], candidates)
            // test again without invoking pakage Helper
            assertEquals(true, completor.findMatchingPreImportedClasses('Big', candidates))
            assertEquals(['prefill', 'BigInteger', 'BigDecimal', 'BigInteger', 'BigDecimal'], candidates)
        }
    }

    void testImportedNone() {
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('A', 'import foo.lang.Bbb', candidates)
            assertEquals(['prefill'], candidates)
        }
    }

    void testImported() {
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('A', 'import foo.lang.Abcdef', candidates)
            assertEquals(['prefill', 'Abcdef'], candidates)
        }
    }

    void testImportedAs() {
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.collectImportedSymbols('import foo.lang.Xxxxx as Abcdef', candidates)
            assertEquals(['prefill', 'Abcdef'], candidates)
        }
    }

    void testImportedStarMatchCached() {
        // mock<making sure cache is used
        groovyshMocker.demand.getPackageHelper(1) { [getContents: {['Bbbb', 'Abcdef']}] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('A', 'import foo.lang.*', candidates)
            assertEquals(['prefill', 'Abcdef'], candidates)
            // test again without invoking packageHelper
            completor.findMatchingImportedClassesCached('A', 'import foo.lang.*', candidates)
            assertEquals(['prefill', 'Abcdef', 'Abcdef'], candidates)
        }
    }

    void testImportedStaticCachedNone() {
        groovyshMocker.demand.getInterp(1) { [evaluate: {expr -> assert (expr == ["foo.lang.Math"]); Math}] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('ma', 'import static foo.lang.Math.max', candidates)
            assertEquals(['prefill', 'max('], candidates)
        }
    }

    void testImportedStaticCachedStar() {
        groovyshMocker.demand.getInterp(1) { [evaluate: {expr -> assert (expr == ["foo.lang.Math"]); Math}] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('ma', 'import static foo.lang.Math.*', candidates)
            assertEquals(['prefill', 'max('], candidates)
        }
    }

    void testImportedAsCachedOther() {
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('Y', 'import foo.lang.Abcdef as Xxxxxx', candidates)
            assertEquals(['prefill'], candidates)
        }
    }

    void testImportedStaticCached() {
        groovyshMocker.demand.getInterp(1) { [evaluate: {expr -> assert (expr == ["foo.lang.Math"]); Math}] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('ma', 'import static foo.lang.Math.max', candidates)
            assertEquals(['prefill', 'max('], candidates)
        }
    }

    void testImportedStarCachedOther() {
        groovyshMocker.demand.getPackageHelper(1) { [getContents: {['Bbbb']}] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('A', 'import foo.lang.*', candidates)
            assertEquals(['prefill'], candidates)
            completor.findMatchingImportedClassesCached('B', 'import foo.lang.*', candidates)
            assertEquals(['prefill', 'Bbbb'], candidates)
        }
    }

    void testImportedStarCachedMatch() {
        groovyshMocker.demand.getPackageHelper(1) { [getContents: {['Bbbb', 'Abcdef']}] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            completor.findMatchingImportedClassesCached('A','import foo.lang.*', candidates)
            assertEquals(['prefill', 'Abcdef'], candidates)
            // test again without invoking packageHelper
            completor.findMatchingImportedClassesCached('A', 'import foo.lang.*', candidates)
            assertEquals(['prefill', 'Abcdef', 'Abcdef'], candidates)
        }
    }

    // Integration tests over all methods
    void testNoImports() {
        groovyshMocker.demand.getPackageHelper(6) { [getContents: {[]}] }
        groovyshMocker.demand.getImports(1) { [] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            assertEquals(false, completor.complete(tokenList("xyz"), candidates))
            assertEquals(['prefill'], candidates)
        }
    }

    void testSimpleImport() {
        groovyshMocker.demand.getPackageHelper(6) { [getContents: {[]}] }
        groovyshMocker.demand.getImports(1) { ["import xyzabc.*", "import xxxx.*"] }
        groovyshMocker.demand.getPackageHelper(1) { [getContents: { ['Xyzabc']}] }
        groovyshMocker.demand.getPackageHelper(1) { [getContents: { ['Xyz123']}] }
        // second call
        groovyshMocker.demand.getImports(2) { ["import xyzabc.*", "import xxxx.*"] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            assertEquals(true, completor.complete(tokenList("Xyz"), candidates))
            assertEquals(['prefill', "Xyzabc", "Xyz123"], candidates)
            // try again to check cache is used
            assertEquals(true, completor.complete(tokenList("Xyz1"), candidates))
            assertEquals(['prefill', "Xyzabc", "Xyz123", "Xyz123"], candidates)
            assertEquals(true, completor.complete(tokenList("Xyz"), candidates))
            assertEquals(['prefill', "Xyzabc", "Xyz123", "Xyz123", "Xyzabc", "Xyz123"], candidates)
        }
    }

    void testUnknownImport() {
        groovyshMocker.demand.getPackageHelper(6) { [getContents: {[]}] }
        groovyshMocker.demand.getImports(1) { ["import xxxx"] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportsSyntaxCompletor completor = new ImportsSyntaxCompletor(groovyshMock)
            def candidates = ['prefill']
            assertEquals(false, completor.complete(tokenList("xyz"), candidates))
            assertEquals(['prefill'], candidates)
        }
    }
}
