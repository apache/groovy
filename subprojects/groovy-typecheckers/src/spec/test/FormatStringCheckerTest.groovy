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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

class FormatStringCheckerTest {

    @Test
    void testIntro() {
        // tag::intro_example[]
        assert String.format('%4.2f %02X %B', Math.PI, 15, true) == '3.14 0F TRUE'
        // end::intro_example[]
    }

    @Test
    void testValidFormatStringExample() {
        assertScript('''
        import groovy.transform.TypeChecked

        // tag::valid_format_string_example[]
        @TypeChecked(extensions='groovy.typecheckers.FormatStringChecker')
        static main(args) {
            assert String.format('%x', 1023) == '3ff'
            assert String.format(Locale.US, '%x', 1024) == '400'
            assert sprintf('%s%s', 'foo', 'bar') == 'foobar'
            assert new Formatter().format('xy%s', 'zzy').toString() == 'xyzzy'
            def baos = new ByteArrayOutputStream()
            new PrintStream(baos).printf('%-4c|%6b', 'x' as char, true)
            assert baos.toString() == 'x   |  true'
        }
        // end::valid_format_string_example[]
        ''')
    }

    @Test
    void testValidFormatStringVariations() {
        assertScript('''
        import groovy.transform.TypeChecked

        // tag::valid_format_string_variations[]
        static final String FIELD_PATTERN = '%x'
        static final int FIELD_PARAM = 1023

        @TypeChecked(extensions='groovy.typecheckers.FormatStringChecker')
        static main(args) {
            assert sprintf('%x', 1024) == '400'  // <1>

            assert sprintf(FIELD_PATTERN, FIELD_PARAM) == '3ff'  // <2>

            var local_var_pattern = '%x'
            var local_var_param = 1022
            assert sprintf(local_var_pattern, local_var_param) == '3fe'  // <3>

            assert sprintf('%x%s', theParam(), 'a' + 'b') == '3fdab'  // <4>
        }

        static int theParam() { 1021 }
        // end::valid_format_string_variations[]
        ''')
    }

    @Test
    void testFormatMethodAnnotationExample() {
        assertScript('''
        import groovy.transform.TypeChecked
        import groovy.typecheckers.FormatMethod

        // tag::format_method_example[]
        @FormatMethod
        static String log(String formatString, Object... args) {
            sprintf(formatString, args)
        }

        @TypeChecked(extensions='groovy.typecheckers.FormatStringChecker')
        static main(args) {
            assert log('%x', 1023) == '3ff'
            assert log('%x', 1024) == '400'
        }
        // end::format_method_example[]
        ''')
    }

    @Test
    void testIllegalFormatConversion() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked

        @TypeChecked(extensions='groovy.typecheckers.FormatStringChecker')
        static main(args) {
            // tag::illegal_format_conversion[]
            sprintf '%c', true
            // end::illegal_format_conversion[]
        }
        ''')
        def expectedError = '''\
        # tag::illegal_format_conversion_message[]
        [Static type checking] - IllegalFormatConversion: c != java.lang.Boolean
        # end::illegal_format_conversion_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testIllegalFormatPrecision() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked

        @TypeChecked(extensions='groovy.typecheckers.FormatStringChecker')
        static main(args) {
            // tag::illegal_format_precision[]
            String.format('%1.7d', 3)
            // end::illegal_format_precision[]
        }
        ''')
        def expectedError = '''\
        # tag::illegal_format_precision_message[]
        [Static type checking] - IllegalFormatPrecision: 7
        # end::illegal_format_precision_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testUnknownFormatConversion() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked

        @TypeChecked(extensions='groovy.typecheckers.FormatStringChecker')
        static main(args) {
            // tag::unknown_format_conversion[]
            System.out.printf('%v', 7)
            // end::unknown_format_conversion[]
        }
        ''')
        def expectedError = '''\
        # tag::unknown_format_conversion_message[]
        [Static type checking] - UnknownFormatConversion: Conversion = 'v'
        # end::unknown_format_conversion_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

}
