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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.BeforeClass
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assume.assumeTrue

final class FormatStringCheckerTest {

    private static GroovyShell shell

    @BeforeClass
    static void setUp() {
        shell = new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(groovy.transform.TypeChecked)
            customizer.annotationParameters = [extensions: 'groovy.typecheckers.FormatStringChecker']
            addCompilationCustomizers(customizer)
        })
    }

    @Test
    void testDuplicateFormatFlagsForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%003d', 3)
        '''
        assert err =~ /DuplicateFormatFlags: Flags = '0'/
        err = shouldFail shell, '''
            String.format('%++3d', 3)
        '''
        assert err =~ /DuplicateFormatFlags: Flags = '\+'/
    }

    @Test
    void testDuplicateFormatFlagsForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%--3d', 3 + 0)
        '''
        assert err =~ /DuplicateFormatFlags: Flags = '-'/
        err = shouldFail shell, '''
            String.format('%  3d', 3 + 0)
        '''
        assert err =~ /DuplicateFormatFlags: Flags = ' '/
    }

    @Test
    void testIllegalFormatFlagsForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%-03d', 3)
        '''
        assert err =~ /IllegalFormatFlags: Flags = '-0'/
        err = shouldFail shell, '''
            String.format('% +3d', 3)
        '''
        assert err =~ /IllegalFormatFlags: Flags = '\+ '/
        err = shouldFail shell, '''
            String.format('%+ 3d', 3)
        '''
        assert err =~ /IllegalFormatFlags: Flags = '\+ '/
    }

    @Test
    void testIllegalFormatFlagsForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%-03d', 3 + 0)
        '''
        assert err =~ /IllegalFormatFlags: Flags = '-0'/
        err = shouldFail shell, '''
            String.format('% +3d', 3 + 0)
        '''
        assert err =~ /IllegalFormatFlags: Flags = ' \+'/
        err = shouldFail shell, '''
            String.format('%+ 3d', 3 + 0)
        '''
        assert err =~ /IllegalFormatFlags: Flags = '\+ '/
    }

    @Test
    void testIllegalFormatPrecisionForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%1.7d', 3)
        '''
        assert err =~ /IllegalFormatPrecision: 7/
    }

    @Test
    void testIllegalFormatPrecisionForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%1.8tT', new Date())
        '''
        assert err =~ /IllegalFormatPrecision: 8/
        err = shouldFail shell, '''
            String.format('%1.7d', 3 + 0)
        '''
        assert err =~ /IllegalFormatPrecision: 7/
    }

    @Test
    void testIllegalFormatConversionForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%tT', 'foo')
        '''
        assert err =~ /IllegalFormatConversion: T != java\.lang\.String/
    }

    @Test
    void testUnknownFormatConversionForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%tq', new Date())
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'tq'/
    }

    @Test
    void testMissingFormatWidthForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%-c', 'c' as char)
        '''
        assert err =~ /MissingFormatWidth: %-c/
        err = shouldFail shell, '''
            String.format('%-tT', new Date())
        '''
        assert err =~ /MissingFormatWidth: %-tT/
    }

    @Test
    void testFormatFlagsConversionMismatchForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%+#tT', new Date())
        '''
        assert err =~ /FormatFlagsConversionMismatch: Conversion = T, Flags = '\+#'/
        err = shouldFail shell, '''
            String.format('%0,c', 'c' as char)
        '''
        assert err =~ /FormatFlagsConversionMismatch: Conversion = c, Flags = '0,'/
        err = shouldFail shell, '''
            String.format('%#b', true || true)
        '''
        assert err =~ /FormatFlagsConversionMismatch: Conversion = b, Flags = '#'/
    }

    @Test
    void testUnknownFormatForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%r', 7)
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'r'/
        err = shouldFail shell, '''
            String.format('%d %d %u', 7, 7, 7)
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'u'/
        err = shouldFail shell, '''
            System.out.printf('%v', 7)
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'v'/
        err = shouldFail shell, '''
            printf '%t', 'foo'
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 't'/
        err = shouldFail shell, '''
            printf '%w'
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'w'/
        err = shouldFail shell, '''
            sprintf '%z', 7, 8
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'z'/
    }

    @Test
    void testUnknownFormatForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%r', 7 + 0)
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'r'/
        err = shouldFail shell, '''
            String.format('%d %d %u', 7 + 0, 7, 7)
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'u'/
        err = shouldFail shell, '''
            System.out.printf('%v', 7 + 0)
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'v'/
        err = shouldFail shell, '''
            printf '%t', 'foo' + ''
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 't'/
        err = shouldFail shell, '''
            printf '%w'
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'w'/
        err = shouldFail shell, '''
            sprintf '%z', 7 + 0, 8
        '''
        assert err =~ /UnknownFormatConversion: Conversion = 'z'/
    }

    @Test
    void testIllegalConversionForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%d', 'some string')
        '''
        assert err =~ /IllegalFormatConversion: d != java\.lang\.String/
        err = shouldFail shell, '''
            System.out.printf('%o', 3.5)
        '''
        assert err =~ /IllegalFormatConversion: o != java\.math\.BigDecimal/
        err = shouldFail shell, '''
            printf '%s %x', 'foo', 'bar'
        '''
        assert err =~ /IllegalFormatConversion: x != java\.lang\.String/
        err = shouldFail shell, '''
            sprintf '%c', true
        '''
        assert err =~ /IllegalFormatConversion: c != java\.lang\.Boolean/
    }

    @Test
    void testIllegalConversionForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%d', 'some string' + '')
        '''
        assert err =~ /IllegalFormatConversion: d != java\.lang\.String/
        err = shouldFail shell, '''
            System.out.printf('%o', 3.5 + 0)
        '''
        assert err =~ /IllegalFormatConversion: o != java\.math\.BigDecimal/
        err = shouldFail shell, '''
            printf '%s %x', 'foo', 'bar' + ''
        '''
        assert err =~ /IllegalFormatConversion: x != java\.lang\.String/
        err = shouldFail shell, '''
            sprintf '%c', true || true
        '''
        assert err =~ /IllegalFormatConversion: c != java\.lang\.Boolean/
    }

    @Test
    void testMissingFormatArgumentForConstantArgs() {
        def err = shouldFail shell, '''
            String.format('%<s', 'some string')
        '''
        assert err =~ /MissingFormatArgument: Format specifier '%<s'/
        err = shouldFail shell, '''
            String.format('%s %s', 'some string')
        '''
        assert err =~ "MissingFormatArgument: Format specifier '%s'"
        err = shouldFail shell, '''
            String.format('%2$s', 'some string')
        '''
        assert err =~ /MissingFormatArgument: Format specifier '%\d[$]s'/
    }

    @Test
    void testMissingFormatArgumentForInferredArgs() {
        def err = shouldFail shell, '''
            String.format('%<s', 'some string' + '')
        '''
        assert err =~ /MissingFormatArgument: Format specifier '%<s'/
        err = shouldFail shell, '''
            String.format('%s %s', 'some string' + '')
        '''
        assert err =~ "MissingFormatArgument: Format specifier '%s'"
        err = shouldFail shell, '''
            String.format('%2$s', 'some string' + '')
        '''
        assert err =~ /MissingFormatArgument: Format specifier '%\d[$]s'/
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
}
