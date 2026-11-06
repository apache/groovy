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
package groovy.typecheckers

import groovy.transform.TypeChecked
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.util.regex.Matcher

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class RegexCheckerTest {

    private static GroovyShell shell

    @BeforeAll
    static void setUp() {
        shell = new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(TypeChecked)
            customizer.annotationParameters = [extensions: 'groovy.typecheckers.RegexChecker']
            addCompilationCustomizers(customizer, new ImportCustomizer().addStarImports('java.util.regex'))
        })
    }

    @Test
    void testBadRegexForExplicitPatternDeclaration() {
        def err = shouldFail shell, '''
            def shortNamed = ~/\\w{3/ // missing closing quantifier brace
        '''
        assert err.message =~ /Bad regex: Unclosed counted closure/

        err = shouldFail shell, '''
            def oNamed = ~/(.)o(.*/ // missing closing grouping bracket
        '''
        assert err.message.contains('Bad regex: Unclosed group')
    }

    @Test
    void testGoodRegexForExplicitPatternDeclaration() {
        assertScript shell, '''
            def pets = ['cat', 'dog', 'goldfish']
            def shortNamed = ~/\\w{3}/
            assert pets.grep(shortNamed) == ['cat', 'dog']
        '''

        assertScript shell, '''
            def pets = ['cat', 'dog', 'goldfish']
            def oNamed = ~/(.)o(.*)/
            assert pets.grep(oNamed) == ['dog', 'goldfish']
        '''
    }

    @Test
    void testBadRegexForExplicitJavaPatternDeclaration() {
        def err = shouldFail shell, '''
            Pattern.compile(/?/) // dangling metacharacter
        '''
        assert err.message.contains(/Bad regex: Dangling meta character '?'/)
    }

    @Test
    void testGoodRegexForExplicitJavaPatternDeclaration() {
        assertScript shell, '''
            def pets = ['bird', 'cat', 'goldfish']
            def threeOrFour = Pattern.compile(/....?/)
            assert pets.grep(threeOrFour) == ['bird', 'cat']
        '''
    }

    @Test
    void testBadRegexWithRegexMatchOperator() {
        // explicit string cases
        def err = shouldFail shell, '''
            def newYears = '2020-12-31'
            def matcher = newYears =~ /(\\d{4})-(\\d{1,2})-(\\d{1,2}/
        '''
        assert err.message.contains(/Bad regex: Unclosed group/)

        err = shouldFail shell, '''
            def newYears = '2020-12-31'
            def matcher = newYears =~ /(\\d{4})-(\\d{1,2})-(\\d{1,2)/
        '''
        assert err.message.contains(/Bad regex: Unclosed counted closure/)

        // field case(s)
        err = shouldFail shell, '''
            class Foo {
                private static final REGEX = /(\\d{4})-(\\d{1,2})-(\\d{1,2}/
                static void main(String[] args) {
                    def newYears = '2020-12-31'
                    def m = newYears =~ REGEX
                }
            }
        '''
        assert err.message ==~ /(?s).*Bad regex.*: Unclosed group.*/

        // local var case(s)
        err = shouldFail shell, $/
            class Foo {
                static void main(String[] args) {
                    def newYears = '2020-12-31'
                    def REGEX = /(\d{4})-(\d{1,2})-(\d{1,2}/
                    def m = newYears =~ REGEX
                }
            }
        /$
        assert err.message ==~ /(?s).*Bad regex.*: Unclosed group.*/
    }

    @Test
    void testGoodRegexWithRegexMatchOperator() {
        // explicit cases
        assertScript shell, '''
            def newYears = '2020-12-31'
            Matcher m = newYears =~ /(\\d{4})-(\\d{1,2})-(\\d{1,2})/
            List parts = (List) m[0]
            assert parts[1] == '2020'
            assert parts[2] == '12'
            assert parts[3] == '31'
        '''
        // as above without cast
        assertScript shell, '''
            def newYears = '2020-12-31'
            Matcher m = newYears =~ /(\\d{4})-(\\d{2})-(\\d{2})/
            List parts = m[0]
            assert parts[1] == '2020'
            assert parts[2] == '12'
            assert parts[3] == '31'
        '''

        // field
        assertScript shell, '''
            class Foo {
                private static final REGEX = /(\\d{4})-(\\d{1,2})-(\\d{1,2})/
                static void main(String[] args) {
                    def newYears = '2020-12-31'
                    Matcher m = newYears =~ REGEX
                    List parts = (List) m[0]
                    assert parts[1] == '2020'
                    assert parts[2] == '12'
                    assert parts[3] == '31'
                }
            }
        '''

        // local var cases
        assertScript shell, '''
            def newYears = '2020-12-31'
            def REGEX = /(\\d{4})-(\\d{1,2})-(\\d{1,2})/
            Matcher m = newYears =~ REGEX
            List parts = (List) m[0]
            assert parts[1] == '2020'
            assert parts[2] == '12'
            assert parts[3] == '31'
        '''
        // as above without groups or casting
        assertScript shell, '''
            def newYears = '2020-12-31'
            Matcher m = newYears =~ /\\d{2}/
            assert m[0].toUpperCase() == '20'
            assert m[1].getBytes() == [50, 48]
            assert m[2] == '12'
            assert m[3] == '31'
        '''
    }

    @Test
    void testBadRegexWithRegexFindOperator() {
        // explicit string cases
        def err = shouldFail shell, $/
            def newYears = '2020-12-31'
            assert newYears ==~ /\d{4}-\d{1,2}-\d{1,2/
        /$
        assert err.message.contains(/Bad regex: Unclosed counted closure/)

        // field case(s)
        err = shouldFail shell, '''
            class Foo {
                private static final REGEX = /2020-12-[0123][0123456789/
                static void main(String[] args) {
                    def newYears = '2020-12-31'
                    assert newYears ==~ REGEX
                }
            }
        '''
        assert err.message ==~ /(?s).*Bad regex.*: Unclosed character class.*/

        // local var case(s)
        err = shouldFail shell, '''
            def REGEX = /?/
            def newYears = '2020-12-31'
            assert newYears ==~ REGEX
        '''
        assert err.message ==~ /(?s).*Bad regex.*: Dangling meta character '?'.*/
    }

    @Test
    void testGoodRegexWithRegexFindOperator() {
        // field case(s)
        assertScript shell, '''
        class Foo {
            private static final REGEX = /\\d{4}-\\d{1,2}-\\d{1,2}/
            static void main(String[] args) {
                def newYears = '2020-12-31'
                assert newYears ==~ REGEX
            }
        }
        '''
    }

    @Test
    void testBadRegexJavaMatches() {
        // local var
        def err = shouldFail shell, '''
            def REGEX = /?/
            Pattern.matches(REGEX, 'foo')
        '''
        assert err.message ==~ /(?s).*Bad regex.*: Dangling meta character '?'.*/
    }

    @Test
    void testGoodRegexJavaMatches() {
        assertScript shell, "assert Pattern.matches(/\\d{4}-\\d{1,2}-\\d{1,2}/, '2020-12-31')"
    }

    @Test
    void testBadRegexJavaStringMatches() {
        // GROOVY-12081: the regex argument of String#matches is now checked
        def err = shouldFail shell, '''
            assert 'JOHN'.matches('[A-Z+') // unclosed character class
        '''
        assert err.message ==~ /(?s).*Bad regex.*: Unclosed character class.*/

        // String-typed receiver via parameter
        err = shouldFail shell, '''
            boolean isUpper(String s) { s.matches('[A-Z+') }
        '''
        assert err.message ==~ /(?s).*Bad regex.*: Unclosed character class.*/

        // regex supplied via a local constant variable
        err = shouldFail shell, '''
            def re = '[A-Z+'
            assert 'JOHN'.matches(re)
        '''
        assert err.message ==~ /(?s).*Bad regex.*: Unclosed character class.*/
    }

    @Test
    void testGoodRegexJavaStringMatches() {
        assertScript shell, '''
            boolean isUpper(String s) { s.matches('[A-Z]+') }
            assert isUpper('JOHN')
            assert !isUpper('John')
        '''
        // a matches(String) on a non-String receiver must not be treated as a regex
        assertScript shell, '''
            class Foo { boolean matches(String s) { true } }
            assert new Foo().matches('[A-Z+')
        '''
    }

    @Test
    void testBadRegexGroupCount() {
        def shouldFailWithGroupCountError = { String script ->
            def err = shouldFail(shell, script)
            assert err.message =~ /Invalid group count 3 for regex with 2 groups/
        }

        shouldFailWithGroupCountError '''
            def m = 'foobaz' =~ /(...)(...)/
            assert m.find()
            assert m.group(3)
        '''
        shouldFailWithGroupCountError '''
            def p = ~'(...)(...)'
            Matcher m = p.matcher('barfoo')
            assert m.find()
            assert m.group(3)
        '''
        shouldFailWithGroupCountError '''
            Pattern p = Pattern.compile('(...)(...)')
            Matcher m = p.matcher('foobar')
            assert m.find()
            assert m.group(3)
        '''
        shouldFailWithGroupCountError '''
            def m = 'barbaz' =~ /(...)(...)/
            assert m[0][3]
        '''
    }

    @Test
    void testInlinePatternAsMatcherReceiver() {
        // GROOVY-12080: pattern operator result used directly as a matcher() receiver
        // used to crash the compiler ("No signature of method: findTargetVariable")
        assertScript shell, '''
            Matcher m = (~/(...)(...)/).matcher('foobar')
            assert m.find()
            assert m.group(1) == 'foo'
            assert m.group(2) == 'bar'
        '''
    }

    @Test
    void testBadRegexGroupCountForInlinePattern() {
        // GROOVY-12080: group count is now inferred for inline patterns too
        def err = shouldFail shell, '''
            Matcher m = (~/(...)(...)/).matcher('foobar')
            assert m.find()
            assert m.group(3)
        '''
        assert err.message =~ /Invalid group count 3 for regex with 2 groups/
    }

    @Test
    void testGoodRegexGroupCount() {
        assertScript shell, '''
            def m = 'foobaz' =~ /(...)(...)/
            assert m.find()
            assert m.group(1) == 'foo'
            assert m.group(2) == 'baz'
        '''
        assertScript shell, '''
            def p = ~'(...)(...)'
            Matcher m = p.matcher('barfoo')
            assert m.find()
            assert m.group(1) == 'bar'
            assert m.group(2) == 'foo'
        '''
        assertScript shell, '''
            Pattern p = Pattern.compile('(...)(...)')
            Matcher m = p.matcher('foobar')
            assert m.find()
            assert m.group(1) == 'foo'
            assert m.group(2) == 'bar'
        '''
        assertScript shell, '''
            def m = 'barbaz' =~ /(...)(...)/
            assert m[0][1] == 'bar'
            assert m[0][2] == 'baz'
        '''
    }

    @Test
    void testBadRegexWithRegexGuard() {
        def err = shouldFail shell, '''
            groovy.util.regex.RegexGuard.matches(/(foo/, 'foobar', 100)
        '''
        assert err.message.contains('Bad regex: Unclosed group')

        err = shouldFail shell, '''
            def m = groovy.util.regex.RegexGuard.matcher(/f[o]{2/, 'foobar', 100)
        '''
        assert err.message =~ /Bad regex: Unclosed counted closure/

        // the operator-shaped entry points take the input first and the pattern second
        err = shouldFail shell, '''
            groovy.util.regex.RegexGuard.matchRegex('foobar', /(foo/, 100)
        '''
        assert err.message.contains('Bad regex: Unclosed group')

        err = shouldFail shell, '''
            def m = groovy.util.regex.RegexGuard.findRegex('foobar', /f[o]{2/, 100)
        '''
        assert err.message =~ /Bad regex: Unclosed counted closure/
    }

    @Test
    void testBadRegexWithinSafeRegexScope() {
        def err = shouldFail shell, '''
            @groovy.transform.SafeRegex(millis = 100)
            def check() { 'foobar' ==~ /(foo/ }
        '''
        assert err.message.contains('Bad regex: Unclosed group')

        err = shouldFail shell, '''
            @groovy.transform.SafeRegex(millis = 100)
            def check() { 'foobar' =~ /f[o]{2/ }
        '''
        assert err.message =~ /Bad regex: Unclosed counted closure/
    }

    @Test
    void testInvalidGroupCountWithRegexGuardMatcher() {
        def err = shouldFail shell, '''
            def m = groovy.util.regex.RegexGuard.matcher(/(...)(...)/, 'foobar', 100)
            assert m.find()
            m.group(3)
        '''
        assert err.message.contains('Invalid group count 3 for regex with 2 groups')

        err = shouldFail shell, '''
            def m = groovy.util.regex.RegexGuard.findRegex('foobar', /(...)(...)/, 100)
            assert m.find()
            m.group(3)
        '''
        assert err.message.contains('Invalid group count 3 for regex with 2 groups')
    }

    @Test
    void testInvalidGroupCountWithinSafeRegexScope() {
        def err = shouldFail shell, '''
            @groovy.transform.SafeRegex(millis = 100)
            def check() {
                def m = 'foobar' =~ /(...)(...)/
                def x = m[0][3]
            }
        '''
        assert err.message.contains('Invalid group count 3 for regex with 2 groups')
    }

    @Test
    void testBadRegexWithCharSequenceExtensionMethods() {
        for (name in ['find', 'findAll', 'findGroups', 'findAllGroups', 'split']) {
            def err = shouldFail shell, "'foobar'." + name + "(/(foo/)"
            assert err.message.contains('Bad regex: Unclosed group')
        }
        def err = shouldFail shell, '''
            'foobar'.replaceAll(/f[o]{2/, 'x')
        '''
        assert err.message =~ /Bad regex: Unclosed counted closure/
    }

    @Test
    void testGoodRegexWithCharSequenceExtensionMethods() {
        assertScript shell, '''
            assert 'foobar'.findGroups(/(...)(...)/) == ['foobar', 'foo', 'bar']
            assert 'foobar'.findAll(/o+/) == ['oo']
            assert 'a,b'.split(/,/) == ['a', 'b']
        '''
    }

    @Test
    void testTooManyVariablesForFindGroups() {
        def err = shouldFail shell, '''
            def (all, first, second, third) = 'foobar'.findGroups(/(...)(...)/)
        '''
        assert err.message.contains('Too many variables 4 for findGroups')
        assert err.message.contains('yields at most 3 elements')

        err = shouldFail shell, '''
            def (all, first, second, third) = 'foobar'.findGroups(~/(...)(...)/)
        '''
        assert err.message.contains('Too many variables 4 for findGroups')
    }

    @Test
    void testValidDestructuringForFindGroups() {
        assertScript shell, '''
            def (all, first, second) = 'foobar'.findGroups(/(...)(...)/)
            assert [all, first, second] == ['foobar', 'foo', 'bar']
        '''
        // fewer variables than available elements is legitimate
        assertScript shell, '''
            def (all, first) = 'foobar'.findGroups(/(...)(...)/)
            assert [all, first] == ['foobar', 'foo']
        '''
    }

    @Test // matcher group inference must survive the @SafeRegex rewrite
    void testMatcherGroupInferenceWithinSafeRegexScope() {
        assertScript shell, '''
            @groovy.transform.SafeRegex(millis = 500)
            def check() {
                def m = 'foobar' =~ /(...)(...)/
                assert m[0][1] == 'foo'
                assert m[0][2] == 'bar'
            }
            check()
        '''
    }

    // GROOVY-12688
    @Test
    void testMethodsWithoutBody() {
        assertScript shell, '''
            interface Named { String name() }
            enum Op {
                PLUS { int apply(int a, int b) { a + b } }
                abstract int apply(int a, int b)
            }
            assert Op.PLUS.apply(1, 2) == 3
        '''
        def err = shouldFail shell, '''
            enum Op {
                SHORT { Pattern pattern() { ~/\\w{3/ } }
                abstract Pattern pattern()
            }
        '''
        assert err.message =~ /Bad regex: Unclosed counted closure/
        assert !err.message.contains('General error')
    }

    // GROOVY-12697: group(String) used to fail inside the compiler ("Cannot get property 'value' on null object")
    @Test
    void testGoodNamedGroupLookup() {
        assertScript shell, '''
            String level(String line) {
                Matcher m = line =~ /(?<level>INFO|WARN|ERROR)/
                m.find() ? m.group('level') : 'none'
            }
            assert level('2026-11-04 WARN disk low') == 'WARN'
            assert level('2026-11-04 TRACE verbose') == 'none'
        '''
        assertScript shell, '''
            Pattern p = ~/(?<first>...)(?<second>...)/
            Matcher m = p.matcher('foobar')
            assert m.find()
            assert m.group('first') == 'foo'
            assert m.group('second') == 'bar'
        '''
        assertScript shell, '''
            Matcher m = Pattern.compile(/(?<first>...)(?<second>...)/).matcher('foobar')
            assert m.find()
            assert m.group('second') == 'bar'
        '''
        // a name tracked through a local constant
        assertScript shell, '''
            def m = 'foobar' =~ /(?<first>...)(?<second>...)/
            assert m.find()
            String name = 'first'
            assert m.group(name) == 'foo'
        '''
    }

    // GROOVY-12697
    @Test
    void testBadNamedGroupLookup() {
        def err = shouldFail shell, '''
            def m = 'foobar' =~ /(?<first>...)(?<second>...)/
            assert m.find()
            assert m.group('third')
        '''
        assert err.message.contains("Invalid group name 'third' for regex with named groups first, second")
        assert !err.message.contains('General error')

        err = shouldFail shell, '''
            Pattern p = ~/(?<first>...)(...)/
            Matcher m = p.matcher('foobar')
            assert m.find()
            assert m.group('second')
        '''
        assert err.message.contains("Invalid group name 'second' for regex with named group first")

        err = shouldFail shell, '''
            Matcher m = Pattern.compile(/(...)(...)/).matcher('foobar')
            assert m.find()
            assert m.group('first')
        '''
        assert err.message.contains("Invalid group name 'first' for regex with no named groups")
    }

    // GROOVY-12697: a group index or name that is not a constant is left to the runtime
    @Test
    void testNonConstantGroupLookup() {
        assertScript shell, '''
            String pick(int index, String name) {
                def m = 'foobar' =~ /(?<first>...)(?<second>...)/
                assert m.find()
                m.group(index) + m.group(name) + m[0][index]
            }
            assert pick(2, 'first') == 'barfoobar'
        '''
        assertScript shell, '''
            Pattern p = ~/(?<first>...)(?<second>...)/
            Matcher m = p.matcher('foobar')
            assert m.find()
            int index = 'foobar'.size() - 4
            String name = 'FIRST'.toLowerCase()
            assert m.group(index) == 'bar'
            assert m.group(name) == 'foo'
        '''
    }

    // GROOVY-12697: the singular form of the group-count diagnostic, for both lookup shapes
    @Test
    void testBadRegexGroupCountForSingleGroup() {
        def err = shouldFail shell, '''
            def m = 'foobar' =~ /(...).../
            assert m.find()
            assert m.group(2)
        '''
        assert err.message.contains('Invalid group count 2 for regex with 1 group')
        assert !err.message.contains('1 groups')

        err = shouldFail shell, '''
            def m = 'foobar' =~ /(...).../
            assert m[0][2]
        '''
        assert err.message.contains('Invalid group count 2 for regex with 1 group')
        assert !err.message.contains('1 groups')
    }

    // GROOVY-12697: a non-constant outer index does not stop the constant inner index being checked
    @Test
    void testGroupCountWithNonConstantOuterIndex() {
        assertScript shell, '''
            def pick(int which) {
                def m = 'foobar' =~ /(...)(...)/
                assert m[which][1] == 'foo'
                m[which][2]
            }
            assert pick(0) == 'bar'
        '''
        def err = shouldFail shell, '''
            def m = 'foobar' =~ /(...)(...)/
            int which = 'foobar'.size() - 6
            assert m[which][3]
        '''
        assert err.message.contains('Invalid group count 3 for regex with 2 groups')
    }

    // GROOVY-12697: a matcher without a tracked regex (parameter or field) is left to the runtime
    @Test
    void testGroupLookupOnUntrackedMatcher() {
        assertScript shell, '''
            String pick(Matcher m, String name) {
                assert m.find()
                m.group('first') + m.group(name) + m.group(2)
            }
            assert pick('foobar' =~ /(?<first>...)(?<second>...)/, 'second') == 'foobarbar'
        '''
        assertScript shell, '''
            class Holder {
                Matcher m = 'foobar' =~ /(?<first>...)(?<second>...)/
                String first() { m.find() ? m.group('first') : null }
                String named(String name) { m.group(name) }
            }
            def h = new Holder()
            assert h.first() == 'foo'
            assert h.named('second') == 'bar'
        '''
    }

    // GROOVY-12697: the findGroups path records a group count without names; a (mistyped) matcher
    // declared from it must not crash the extension or earn a spurious group-name diagnostic
    @Test
    void testGroupNameLookupOnReceiverWithoutTrackedNames() {
        def err = shouldFail shell, '''
            Pattern p = ~/(...)(...)/
            Matcher m = 'foobar'.findGroups(p)
            m.group('first')
        '''
        assert err.message.contains('Cannot assign value of type java.util.List')
        assert !err.message.contains('Invalid group name')
        assert !err.message.contains('General error')
    }

    // GROOVY-12697: in COMMENTS mode the opener may carry whitespace around and inside the name
    @Test
    void testNamedGroupLookupInCommentsMode() {
        assertScript shell, '''
            Matcher m = 'foobar' =~ /(?x) (?< fir st > ...) (?<second>...)/
            assert m.find()
            assert m.group('first') == 'foo'
            assert m.group('second') == 'bar'
        '''
        def err = shouldFail shell, '''
            Matcher m = 'foobar' =~ /(?x) (?< fir st > ...) (?<second>...)/
            assert m.find()
            assert m.group('fir st')
        '''
        assert err.message.contains("Invalid group name 'fir st' for regex with named groups first, second")
    }

    // GROOVY-12697: the group names travel with the group count on every propagation path,
    // and a constant name is also resolved through a local constant
    @Test
    void testBadNamedGroupLookupVariants() {
        def err = shouldFail shell, '''
            Matcher m = (~/(?<first>...)(...)/).matcher('foobar')
            assert m.find()
            assert m.group('second')
        '''
        assert err.message.contains("Invalid group name 'second' for regex with named group first")

        err = shouldFail shell, '''
            def m = groovy.util.regex.RegexGuard.matcher(/(?<first>...)(?<second>...)/, 'foobar', 100)
            assert m.find()
            m.group('third')
        '''
        assert err.message.contains("Invalid group name 'third' for regex with named groups first, second")

        err = shouldFail shell, '''
            def m = groovy.util.regex.RegexGuard.findRegex('foobar', /(...)(...)/, 100)
            assert m.find()
            m.group('first')
        '''
        assert err.message.contains("Invalid group name 'first' for regex with no named groups")

        err = shouldFail shell, '''
            @groovy.transform.SafeRegex(millis = 100)
            def check() {
                def m = 'foobar' =~ /(?<first>...)(?<second>...)/
                m.group('third')
            }
        '''
        assert err.message.contains("Invalid group name 'third' for regex with named groups first, second")

        err = shouldFail shell, '''
            def m = 'foobar' =~ /(?<first>...)(?<second>...)/
            assert m.find()
            String name = 'third'
            assert m.group(name)
        '''
        assert err.message.contains("Invalid group name 'third' for regex with named groups first, second")
    }

    // GROOVY-12706: a script binding variable is reported as undeclared, not as a general error
    @Test
    void testBindingVariableIsUndeclared() {
        def err = shouldFail shell, '''
            finder = 'groovy' =~ /(oo)/
            finder[0]
        '''
        assert err.message.contains('The variable [finder] is undeclared')
        assert !err.message.contains('General error')

        err = shouldFail shell, '''
            p = ~/(oo)/
            'groovy'.findGroups(p)
        '''
        assert err.message.contains('The variable [p] is undeclared')
        assert !err.message.contains('General error')
    }

    // GROOVY-12706: binding variables given a type by another extension (as a typed binding
    // would) reach the Pattern and Matcher paths; they are left unchecked, not a general error
    @Test
    void testTypedBindingVariables() {
        def typedShell = new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(TypeChecked)
            customizer.annotationParameters = [extensions: ['groovy.typecheckers.RegexChecker', TypedBindingStub.name]]
            addCompilationCustomizers(customizer, new ImportCustomizer().addStarImports('java.util.regex'))
        })
        assertScript typedShell, '''
            m = 'groovy' =~ /(oo)/
            assert m[0] == ['oo', 'oo']
        '''
        assertScript typedShell, '''
            p = ~/(oo)/
            assert 'groovy'.findGroups(p) == ['oo', 'oo']
        '''
        assertScript typedShell, '''
            p = ~/(oo)/
            def matcher = p.matcher('groovy')
            assert matcher.find()
        '''
        assertScript typedShell, '''
            m = 'groovy' =~ /(oo)/
            assert m.find()
            assert m.group(1) == 'oo'
        '''
        // a regex declared in the same script is still checked
        def err = shouldFail typedShell, '''
            p = ~/(oo)/
            def local = 'groovy' =~ /(oo)/
            local[0][2]
        '''
        assert err.message.contains('Invalid group count 2 for regex with 1 group')
        assert !err.message.contains('General error')
    }

    /**
     * Resolves the script variables {@code p} and {@code m} as {@code Pattern} and {@code Matcher},
     * standing in for a host that records the types of its binding variables.
     */
    static class TypedBindingStub extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {
        @Override
        Object run() {
            unresolvedVariable { VariableExpression var ->
                def type = [p: ClassHelper.PATTERN_TYPE, m: ClassHelper.make(Matcher)][var.name]
                if (type) {
                    storeType(var, type)
                    handled = true
                }
            }
        }
    }

    // GROOVY-12707: in COMMENTS mode whitespace may follow the opening parenthesis, and a comment
    // may sit inside the name, so the group is found however the regex text spells it
    @Test
    void testNamedGroupSpelledAcrossCommentsModeWhitespace() {
        assertScript shell, '''
            Matcher m = 'foobar' =~ /(?x)( ?<first>...)/
            assert m.find()
            assert m.group('first') == 'foo'
        '''
        assertScript shell, '''
            Matcher m = 'foobar' =~ "(?x)(?<fi#c\\nrst>...)"
            assert m.find()
            assert m.group('first') == 'foo'
        '''
    }

    // GROOVY-12707: text that only looks like a group opener does not define a group
    @Test
    void testGroupOpenerLookalikesDefineNoGroup() {
        def err = shouldFail shell, '''
            Matcher m = 'x(?<a>)foo' =~ /\\Q(?<a>)\\E(?<b>...)/
            assert m.find()
            m.group('a')
        '''
        assert err.message.contains("Invalid group name 'a' for regex with named group b")

        err = shouldFail shell, '''
            Matcher m = 'afoo' =~ /[(?<a>)](?<b>...)/
            assert m.find()
            m.group('a')
        '''
        assert err.message.contains("Invalid group name 'a' for regex with named group b")

        // a name that could end the backreference early is not mistaken for a defined one
        err = shouldFail shell, '''
            Matcher m = 'foo' =~ /(?<a>...)/
            assert m.find()
            m.group('a>|x')
        '''
        assert err.message.contains("Invalid group name 'a>|x' for regex with named group a")

        // the names listed are only those the regex defines
        err = shouldFail shell, '''
            Matcher m = 'x(?<a>)foobar' =~ /\\Q(?<a>)\\E(?<b>...)(?<c>...)/
            assert m.find()
            m.group('d')
        '''
        assert err.message.contains("Invalid group name 'd' for regex with named groups b, c")
    }

    // GROOVY-12707: start and end take a group name or index just as group does
    @Test
    void testGroupStartAndEnd() {
        assertScript shell, '''
            Matcher m = 'foobar' =~ /(?<first>...)(?<second>...)/
            assert m.find()
            assert m.start('second') == 3 && m.end('second') == 6
            assert m.start(1) == 0 && m.end(2) == 6
        '''
        def err = shouldFail shell, '''
            Matcher m = 'foobar' =~ /(?<first>...)(?<second>...)/
            assert m.find()
            m.start('third')
        '''
        assert err.message.contains("Invalid group name 'third' for regex with named groups first, second")

        err = shouldFail shell, '''
            Matcher m = 'foobar' =~ /(?<first>...)(?<second>...)/
            assert m.find()
            m.end(3)
        '''
        assert err.message.contains('Invalid group count 3 for regex with 2 groups')
    }

    // GROOVY-12707: where the check cannot tell, the lookup is left to the runtime
    @Test
    void testUndecidableGroupNameLeftToRuntime() {
        // an unterminated quote runs to the end of the regex
        def err = shouldFail shell, '''
            Matcher m = 'foo(?<a>' =~ /(?<b>...)\\Q(?<a>/
            assert m.find()
            m.group('a')
        '''
        assert err instanceof IllegalArgumentException
        assert err.message.contains('No group with name <a>')

        // a LITERAL pattern has no groups at all
        err = shouldFail shell, '''
            Matcher m = Pattern.compile('(?<a>x)', Pattern.LITERAL).matcher('(?<a>x)')
            assert m.find()
            m.group('a')
        '''
        assert err instanceof IllegalArgumentException
    }
}
