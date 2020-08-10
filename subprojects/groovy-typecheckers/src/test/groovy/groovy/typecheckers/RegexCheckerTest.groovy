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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.BeforeClass
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

class RegexCheckerTest {
    private static GroovyShell shell

    @BeforeClass
    static void setup() {
        def cc = new CompilerConfiguration()
        def customizer = new ASTTransformationCustomizer(TypeChecked)
        customizer.annotationParameters = [extensions: 'groovy.typecheckers.RegexChecker']
        cc.addCompilationCustomizers(customizer)
        cc.addCompilationCustomizers(new ImportCustomizer().addStarImports("java.util.regex"))
        shell = new GroovyShell(cc)
    }

    @Test
    void testBadRegexForExplicitPatternDeclaration() {
        def err = shouldFail(shell, '''
        def shortNamed = ~/\\w{3/ // missing closing quantifier brace
        ''')
        assert err.message.contains(/Bad regex: Unclosed counted closure/)

        err = shouldFail(shell, '''
        def oNamed = ~/(.)o(.*/ // missing closing grouping bracket
        ''')
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
        def oNamed = ~/(.)o(.*)/ // missing closing bracket
        assert pets.grep(oNamed) == ['dog', 'goldfish']
        '''
    }

    @Test
    void testBadRegexForExplicitJavaPatternDeclaration() {
        def err = shouldFail(shell, '''
        Pattern.compile(/?/) // dangling metacharacter
        ''')
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
        def err = shouldFail(shell, '''
        def newYears = '2020-12-31'
        def matcher = newYears =~ /(\\d{4})-(\\d{1,2})-(\\d{1,2}/
        ''')
        assert err.message.contains(/Bad regex: Unclosed group/)

        err = shouldFail(shell, '''
        def newYears = '2020-12-31'
        def matcher = newYears =~ /(\\d{4})-(\\d{1,2})-(\\d{1,2)/
        ''')
        assert err.message.contains(/Bad regex: Unclosed counted closure/)

        // field case(s)
        err = shouldFail(shell, '''
        class Foo {
            private static final REGEX = /(\\d{4})-(\\d{1,2})-(\\d{1,2}/
            static void main(String[] args) {
                def newYears = '2020-12-31'
                def m = newYears =~ REGEX
            }
        }
        ''')
        assert err.message ==~ /(?s).*Bad regex.*: Unclosed group.*/

        // local var case(s)
        err = shouldFail(shell, $/
        class Foo {
            static void main(String[] args) {
                def newYears = '2020-12-31'
                def REGEX = /(\d{4})-(\d{1,2})-(\d{1,2}/
                def m = newYears =~ REGEX
            }
        }
        /$)
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
        def err = shouldFail(shell, $/
        def newYears = '2020-12-31'
        assert newYears ==~ /\d{4}-\d{1,2}-\d{1,2/
        /$)
        assert err.message.contains(/Bad regex: Unclosed counted closure/)

        // field case(s)
        err = shouldFail(shell, '''
        class Foo {
            private static final REGEX = /2020-12-[0123][0123456789/
            static void main(String[] args) {
                def newYears = '2020-12-31'
                assert newYears ==~ REGEX
            }
        }
        ''')
        assert err.message ==~ /(?s).*Bad regex.*: Unclosed character class.*/

        // local var case(s)
        err = shouldFail(shell, '''
        def REGEX = /?/
        def newYears = '2020-12-31'
        assert newYears ==~ REGEX
        ''')
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
        def err = shouldFail(shell, '''
        def REGEX = /?/
        Pattern.matches(REGEX, 'foo')
        ''')
        assert err.message ==~ /(?s).*Bad regex.*: Dangling meta character '?'.*/
    }

    @Test
    void testGoodRegexJavaMatches() {
        assertScript shell, "assert Pattern.matches(/\\d{4}-\\d{1,2}-\\d{1,2}/, '2020-12-31')"
    }

    @Test
    void testBadRegexGroupCount() {
        shouldFailWithBadGroupCount'''
        def m = 'foobaz' =~ /(...)(...)/
        assert m.find()
        assert m.group(3)
        '''
        shouldFailWithBadGroupCount'''
        def p = ~'(...)(...)'
        Matcher m = p.matcher('barfoo')
        assert m.find()
        assert m.group(3)
        '''
        shouldFailWithBadGroupCount'''
        Pattern p = Pattern.compile('(...)(...)')
        Matcher m = p.matcher('foobar')
        assert m.find()
        assert m.group(3)
        '''
        shouldFailWithBadGroupCount'''
        def m = 'barbaz' =~ /(...)(...)/
        assert m[0][3]
        '''
    }

    private void shouldFailWithBadGroupCount(String script) {
        def err = shouldFail(shell, script)
        assert err.message.contains(/Invalid group count 3 for regex with 2 groups/)
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
}
