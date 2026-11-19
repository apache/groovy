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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assumptions.assumeTrue

final class FormatStringCheckerTest {

    private static GroovyShell shell

    @BeforeAll
    static void setUp() {
        shell = new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(TypeChecked)
            customizer.annotationParameters = [extensions: 'groovy.typecheckers.FormatStringChecker']
            addCompilationCustomizers(customizer)
        })
    }

    @Test
    void testDuplicateFormatFlagsForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%003d', 3)
        '''
        assert err.message =~ /DuplicateFormatFlags: Flags = '0'/
        err = shouldFail shell, '''
            String.format('%++3d', 3)
        '''
        assert err.message =~ /DuplicateFormatFlags: Flags = '\+'/
    }

    @Test
    void testDuplicateFormatFlagsForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%--3d', 3 + 0)
        '''
        assert err.message =~ /DuplicateFormatFlags: Flags = '-'/
        err = shouldFail shell, '''
            String.format('%  3d', 3 + 0)
        '''
        assert err.message =~ /DuplicateFormatFlags: Flags = ' '/
    }

    @Test
    void testIllegalFormatFlagsForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%-03d', 3)
        '''
        assert err.message =~ /IllegalFormatFlags: Flags = '-0'/
        err = shouldFail shell, '''
            String.format('% +3d', 3)
        '''
        assert err.message =~ /IllegalFormatFlags: Flags = '\+ '/
        err = shouldFail shell, '''
            String.format('%+ 3d', 3)
        '''
        assert err.message =~ /IllegalFormatFlags: Flags = '\+ '/
    }

    @Test
    void testIllegalFormatFlagsForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%-03d', 3 + 0)
        '''
        assert err.message =~ /IllegalFormatFlags: Flags = '-0'/
        err = shouldFail shell, '''
            String.format('% +3d', 3 + 0)
        '''
        assert err.message =~ /IllegalFormatFlags: Flags = ' \+'/
        err = shouldFail shell, '''
            String.format('%+ 3d', 3 + 0)
        '''
        assert err.message =~ /IllegalFormatFlags: Flags = '\+ '/
    }

    @Test
    void testIllegalFormatPrecisionForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%1.7d', 3)
        '''
        assert err.message =~ /IllegalFormatPrecision: 7/
    }

    @Test
    void testIllegalFormatPrecisionForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%1.8tT', new Date())
        '''
        assert err.message =~ /IllegalFormatPrecision: 8/
        err = shouldFail shell, '''
            String.format('%1.7d', 3 + 0)
        '''
        assert err.message =~ /IllegalFormatPrecision: 7/
    }

    @Test
    void testIllegalFormatConversionForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%tT', 'foo')
        '''
        assert err.message =~ /IllegalFormatConversion: T != java\.lang\.String/
    }

    @Test
    void testUnknownFormatConversionForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%tq', new Date())
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'tq'/
    }

    @Test
    void testMissingFormatWidthForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%-c', 'c' as char)
        '''
        assert err.message =~ /MissingFormatWidth: %-c/
        err = shouldFail shell, '''
            String.format('%-tT', new Date())
        '''
        assert err.message =~ /MissingFormatWidth: %-tT/
    }

    @Test
    void testFormatFlagsConversionMismatchForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%+#tT', new Date())
        '''
        assert err.message =~ /FormatFlagsConversionMismatch: Conversion = T, Flags = '\+#'/
        err = shouldFail shell, '''
            String.format('%0,c', 'c' as char)
        '''
        assert err.message =~ /FormatFlagsConversionMismatch: Conversion = c, Flags = '0,'/
        err = shouldFail shell, '''
            String.format('%#b', true || true)
        '''
        assert err.message =~ /FormatFlagsConversionMismatch: Conversion = b, Flags = '#'/
    }

    @Test
    void testUnknownFormatForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%r', 7)
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'r'/
        err = shouldFail shell, '''
            String.format('%d %d %u', 7, 7, 7)
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'u'/
        err = shouldFail shell, '''
            System.out.printf('%v', 7)
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'v'/
        err = shouldFail shell, '''
            printf '%t', 'foo'
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 't'/
        err = shouldFail shell, '''
            printf '%w'
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'w'/
        err = shouldFail shell, '''
            sprintf '%z', 7, 8
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'z'/
    }

    @Test
    void testUnknownFormatForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%r', 7 + 0)
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'r'/
        err = shouldFail shell, '''
            String.format('%d %d %u', 7 + 0, 7, 7)
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'u'/
        err = shouldFail shell, '''
            System.out.printf('%v', 7 + 0)
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'v'/
        err = shouldFail shell, '''
            printf '%t', 'foo' + ''
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 't'/
        err = shouldFail shell, '''
            printf '%w'
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'w'/
        err = shouldFail shell, '''
            sprintf '%z', 7 + 0, 8
        '''
        assert err.message =~ /UnknownFormatConversion: Conversion = 'z'/
    }

    @Test
    void testIllegalConversionForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%d', 'some string')
        '''
        assert err.message =~ /IllegalFormatConversion: d != java\.lang\.String/
        err = shouldFail shell, '''
            System.out.printf('%o', 3.5)
        '''
        assert err.message =~ /IllegalFormatConversion: o != java\.math\.BigDecimal/
        err = shouldFail shell, '''
            printf '%s %x', 'foo', 'bar'
        '''
        assert err.message =~ /IllegalFormatConversion: x != java\.lang\.String/
        err = shouldFail shell, '''
            sprintf '%c', true
        '''
        assert err.message =~ /IllegalFormatConversion: c != java\.lang\.Boolean/
    }

    @Test
    void testIllegalConversionForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%d', 'some string' + '')
        '''
        assert err.message =~ /IllegalFormatConversion: d != java\.lang\.String/
        err = shouldFail shell, '''
            System.out.printf('%o', 3.5 + 0)
        '''
        assert err.message =~ /IllegalFormatConversion: o != java\.math\.BigDecimal/
        err = shouldFail shell, '''
            printf '%s %x', 'foo', 'bar' + ''
        '''
        assert err.message =~ /IllegalFormatConversion: x != java\.lang\.String/
        err = shouldFail shell, '''
            sprintf '%c', true || true
        '''
        assert err.message =~ /IllegalFormatConversion: c != java\.lang\.Boolean/
    }

    // GROOVY-12689
    @Test
    void testArgsOfTypesFromSameSource() {
        assertScript shell, '''
            class Foo { String toString() { 'foo' } }
            assert String.format('%s', new Foo()) == 'foo'
        '''
        assertScript '''
            import groovy.transform.TypeChecked

            class Foo { String toString() { 'foo' } }
            enum Color { RED, GREEN }
            class Stamp extends Date { Stamp() { super(0L) } }

            @TypeChecked(extensions = 'groovy.typecheckers.FormatStringChecker')
            class Main {
                static String show(Foo foo, Foo[] foos) {
                    String.format('%s %s %b %d', foo, Color.RED, foos, foos.length)
                }
                static String year() {
                    String.format('%tY', new Stamp())
                }
                String self() {
                    String.format('%s', this)
                }
                String toString() { 'main' }
            }
            assert Main.show(new Foo(), new Foo[0]) == 'foo RED true 0'
            assert Main.year() == String.format('%tY', new Date(0L))
            assert new Main().self() == 'main'
        '''
    }

    // GROOVY-12689
    @Test
    void testIllegalConversionForArgsOfTypesFromSameSource() {
        def err = shouldFail '''
            import groovy.transform.TypeChecked

            class Foo {}
            enum Color { RED, GREEN }

            @TypeChecked(extensions = 'groovy.typecheckers.FormatStringChecker')
            class Main {
                static String number(Foo foo) {
                    String.format('%d', foo)
                }
                static String letter() {
                    String.format('%c', Color.RED)
                }
                static String time(Foo foo) {
                    String.format('%tY', foo)
                }
            }
        '''
        assert err.message =~ /IllegalFormatConversion: d != Foo/
        assert err.message =~ /IllegalFormatConversion: c != Color/
        assert err.message =~ /IllegalFormatConversion: Y != Foo/
        assert !err.message.contains('BUG!')
    }

    @Test
    void testMissingFormatArgumentForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%<s', 'some string')
        '''
        assert err.message =~ /MissingFormatArgument: Format specifier '%<s'/
        err = shouldFail shell, '''
            String.format('%s %s', 'some string')
        '''
        assert err.message =~ "MissingFormatArgument: Format specifier '%s'"
        err = shouldFail shell, '''
            String.format('%2$s', 'some string')
        '''
        assert err.message =~ /MissingFormatArgument: Format specifier '%\d[$]s'/
    }

    @Test
    void testMissingFormatArgumentForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%<s', 'some string' + '')
        '''
        assert err.message =~ /MissingFormatArgument: Format specifier '%<s'/
        err = shouldFail shell, '''
            String.format('%s %s', 'some string' + '')
        '''
        assert err.message =~ "MissingFormatArgument: Format specifier '%s'"
        err = shouldFail shell, '''
            String.format('%2$s', 'some string' + '')
        '''
        assert err.message =~ /MissingFormatArgument: Format specifier '%\d[$]s'/
    }

    // GROOVY-12692
    @Test
    void testPercentAndLineSeparatorTakeNoArgumentForInferredArgs() {
        assertScript shell, '''
            String s = 'some string' + ''
            String n = System.lineSeparator()
            assert String.format('100%% %s', s) == '100% some string'
            assert String.format('%s %%', s) == 'some string %'
            assert String.format('%s%n', s) == "some string$n"
            assert String.format('%n%s', s) == "${n}some string"
            assert String.format('%s %% %s', s, s) == 'some string % some string'
            assert String.format('%s %% %<s', s) == 'some string % some string'
            assert String.format('%s%n%<s', s) == "some string${n}some string"
            assert String.format('%-3%|%s', s) == '%  |some string'
        '''
    }

    // GROOVY-12692
    @Test
    void testFormatErrorsAfterPercentAndLineSeparatorForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('100%% %d', 'some string' + '')
        '''
        assert err.message =~ /IllegalFormatConversion: d != java\.lang\.String/
        err = shouldFail shell, '''
            String.format('%s %% %s', 'some string' + '')
        '''
        assert err.message =~ "MissingFormatArgument: Format specifier '%s'"
        err = shouldFail shell, '''
            String.format('%s%n%s', 'some string' + '')
        '''
        assert err.message =~ "MissingFormatArgument: Format specifier '%s'"
        err = shouldFail shell, '''
            String.format('%% %<s', 'some string' + '')
        '''
        assert err.message =~ /MissingFormatArgument: Format specifier '%<s'/
        err = shouldFail shell, '''
            String.format('%--3% %s', 'some string' + '')
        '''
        assert err.message =~ /DuplicateFormatFlags: Flags = '-'/
    }

    @Test
    void testValidFormatStringsConstantArgs() {
        assertScript shell, '''
        static int x = 254

        static main(args) {
            assert String.format('%x', 1023) == '3ff'
            assert String.format(Locale.US, '%x', 1023) == '3ff'
            assert sprintf('%x', 1022) == '3fe'
            assert sprintf('%x', x) == 'fe'
            var z = 16
            assert sprintf('%x', z) == '10'
            assert sprintf('%3d', 3) == '  3'
            assert sprintf('%-3d', 3) == '3  '
            assert sprintf('%03d', 3) == '003'
            assert sprintf('%x', z) == '10'
            assert sprintf('%f', 3.5) == '3.500000'
            assert sprintf('%3s', 'abcde') == 'abcde'
            assert sprintf('%6s', 'abcde') == ' abcde'
            assert sprintf('%-6s', 'abcde') == 'abcde '
            assert String.format('%2$s %1$s', 'bar', 'foo') == 'foo bar'
            assert String.format('%1$s %1$s', 'bar', 'foo') == 'bar bar'
            assert String.format('%s %<s', 'bar', 'foo') == 'bar bar'
            assert String.format('%2$s %<s', 'bar', 'foo') == 'foo foo'
            assert String.format('%2$s %2$s', 'bar', 'foo') == 'foo foo'
        }
        '''
    }

    @Test
    void testValidFormattedCall() {
        assumeTrue(isAtLeastJdk('15.0'))
        assertScript shell, '''
        assert '%x'.formatted(16) == '10'
        '''
    }

    // GROOVY-12688
    @Test
    void testEnumAndRecordDeclarations() {
        assertScript shell, '''
            enum Size { S, M }
            record Point(int x, int y) {}
            assert Size.valueOf('M').ordinal() == 1
            assert new Point(1, 2).y() == 2
        '''
        assertScript '''
            import groovy.transform.TypeChecked

            @TypeChecked(extensions = 'groovy.typecheckers.FormatStringChecker')
            class Shapes {
                enum Size { S, M }
                record Point(int x, int y) {}
                static String describe() { String.format('%s %d', Size.M.name(), new Point(1, 2).x()) }
            }
            assert Shapes.describe() == 'M 1'
        '''
    }

    // GROOVY-12688
    @Test
    void testFormatErrorsReportedWithEnumAndRecordDeclarations() {
        def err = shouldFail '''
            import groovy.transform.TypeChecked

            @TypeChecked(extensions = 'groovy.typecheckers.FormatStringChecker')
            class Shapes {
                enum Size {
                    S, M
                    String code() { sprintf('%x', name()) }
                }
                record Point(int x, int y) {
                    String flag() { String.format('%c', x > y) }
                }
                static String describe() { String.format('%d', 'some string') }
            }
        '''
        assert err.message =~ /IllegalFormatConversion: d != java\.lang\.String/
        assert err.message =~ /IllegalFormatConversion: x != java\.lang\.String/
        assert err.message =~ /IllegalFormatConversion: c != java\.lang\.Boolean/
        assert !err.message.contains('General error')
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
                PLUS { String show() { String.format('%d', 'some string') } }
                abstract String show()
            }
        '''
        assert err.message =~ /IllegalFormatConversion: d != java\.lang\.String/
        assert !err.message.contains('General error')
    }

    // GROOVY-12696
    @Test
    void testExplicitArgumentIndexForInferredArgs() {
        assertScript shell, '''
            String s = 'some string' + ''
            Integer i = 7 + 0
            assert String.format('%1$s', s) == 'some string'
            assert String.format('%1$s %1$s', s) == 'some string some string'
            assert String.format('%2$s', s, i) == '7'
            assert String.format('%2$d %1$s', s, i) == '7 some string'
            assert String.format('%1$s %2$d', s, i) == 'some string 7'
            assert String.format('%1$d', i, s) == '7'
            assert String.format('%2$s %s %<s %1$s', s, i) == '7 some string some string some string'
            assert String.format('%2$d %<d %s %s', s, i) == '7 7 some string 7'
            assert sprintf('%2$d %1$s', s, i) == '7 some string'
        '''
    }

    // GROOVY-12696
    @Test
    void testExplicitArgumentIndexErrorsForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%1$d', 'some string' + '', 7)
        '''
        assert err.message =~ /IllegalFormatConversion: d != java\.lang\.String/
        err = shouldFail shell, '''
            String.format('%2$d', 7 + 0, 'some string')
        '''
        assert err.message =~ /IllegalFormatConversion: d != java\.lang\.String/
        err = shouldFail shell, '''
            String.format('%1$s %<d', 'some string' + '', 7)
        '''
        assert err.message =~ /IllegalFormatConversion: d != java\.lang\.String/
        err = shouldFail shell, '''
            String.format('%3$s', 'some string' + '', 7)
        '''
        assert err.message =~ /MissingFormatArgument: Format specifier '%3[$]s'/
        err = shouldFail shell, '''
            sprintf('%2$d %1$s', 'some string' + '')
        '''
        assert err.message =~ /MissingFormatArgument: Format specifier '%2[$]d'/
    }

    @Test
    void testIllegalArgumentIndexConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%0$s', 'some string')
        '''
        assert err.message =~ /IllegalFormatArgumentIndex: Illegal format argument index = 0/
        err = shouldFail shell, '''
            String.format('%99999999999$s', 'some string')
        '''
        assert err.message =~ /IllegalFormatArgumentIndex: Format argument index: \(not representable as int\)/
    }

    @Test
    void testIllegalArgumentIndexForInferredArgs() {
        ['%0$s', '%0$d', '%s %0$<s', '%0$%', '%0$n'].each { fmt ->
            def err = shouldFail shell, """
                String.format('$fmt', 'some string' + '', 7)
            """
            assert err.message =~ /IllegalFormatArgumentIndex: Illegal format argument index = 0/
            assert !err.message.contains('IllegalFormatConversion')
        }
        ['%99999999999$s', '%2147483648$s', '%s %99999999999$<s', '%99999999999$%'].each { fmt ->
            def err = shouldFail shell, """
                String.format('$fmt', 'some string' + '', 7)
            """
            assert err.message =~ /IllegalFormatArgumentIndex: Format argument index: \(not representable as int\)/
            assert !err.message.contains('General error')
        }
        assertScript shell, '''
            String s = 'some string' + ''
            assert String.format('%s %1$<s %1$s%%%n', s) == 'some string some string some string%' + System.lineSeparator()
        '''
    }

    // GROOVY-12709: fields whose initializers refer to each other have no constant value
    @Test
    void testFieldInitializerCycle() {
        assertScript shell, '''
            class Cycle {
                static final String F = G
                static final String G = F
                static String format() { String.format(F, 1) }
            }
            assert Cycle.F == null
        '''
        assertScript shell, '''
            class Cycle {
                static String F = G
                static String G = F
                static String format() { String.format(F, 1) }
            }
            assert Cycle.F == null
        '''
        // a chain of fields that ends in a constant is still followed
        def err = shouldFail shell, '''
            class Chain {
                static final String F = G
                static final String G = '%d'
                static String format() { String.format(F, 'x') }
            }
        '''
        assert err.message =~ /IllegalFormatConversion @ line 4, column \d+: d != java\.lang\.String/
    }
}
