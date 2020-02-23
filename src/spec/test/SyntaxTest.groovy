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
import gls.CompilableTestSupport
import org.codehaus.groovy.runtime.StringGroovyMethods

class SyntaxTest extends CompilableTestSupport {

    void testOctalLiteral() {
        // tag::octal_literal_example[]
        int xInt = 077
        assert xInt == 63

        short xShort = 011
        assert xShort == 9 as short
        
        byte xByte = 032
        assert xByte == 26 as byte

        long xLong = 0246
        assert xLong == 166l
        
        BigInteger xBigInteger = 01111
        assert xBigInteger == 585g
        
        int xNegativeInt = -077
        assert xNegativeInt == -63
        // end::octal_literal_example[]
    }

    void testHexadecimalLiteral() {
        // tag::hexadecimal_literal_example[]
        int xInt = 0x77
        assert xInt == 119

        short xShort = 0xaa
        assert xShort == 170 as short
        
        byte xByte = 0x3a
        assert xByte == 58 as byte

        long xLong = 0xffff
        assert xLong == 65535l
        
        BigInteger xBigInteger = 0xaaaa
        assert xBigInteger == 43690g
        
        Double xDouble = new Double('0x1.0p0')
        assert xDouble == 1.0d

        int xNegativeInt = -0x77
        assert xNegativeInt == -119
        // end::hexadecimal_literal_example[]
    }

    void testBinaryLiteral() {
        // tag::binary_literal_example[]
        int xInt = 0b10101111
        assert xInt == 175

        short xShort = 0b11001001
        assert xShort == 201 as short
        
        byte xByte = 0b11
        assert xByte == 3 as byte

        long xLong = 0b101101101101
        assert xLong == 2925l
        
        BigInteger xBigInteger = 0b111100100001
        assert xBigInteger == 3873g

        int xNegativeInt = -0b10101111
        assert xNegativeInt == -175
        // end::binary_literal_example[]
    }

    void testUnderscoreInNumber() {
        // tag::underscore_in_number_example[]
        long creditCardNumber = 1234_5678_9012_3456L
        long socialSecurityNumbers = 999_99_9999L
        double monetaryAmount = 12_345_132.12
        long hexBytes = 0xFF_EC_DE_5E
        long hexWords = 0xFFEC_DE5E
        long maxLong = 0x7fff_ffff_ffff_ffffL
        long alsoMaxLong = 9_223_372_036_854_775_807L
        long bytes = 0b11010010_01101001_10010100_10010010
        // end::underscore_in_number_example[]
    }

    void testNumberTypeSuffixes() {
        // tag::number_type_suffixes_example[]
        assert 42I == new Integer('42')
        assert 42i == new Integer('42') // lowercase i more readable
        assert 123L == new Long("123") // uppercase L more readable
        assert 2147483648 == new Long('2147483648') // Long type used, value too large for an Integer
        assert 456G == new BigInteger('456')
        assert 456g == new BigInteger('456')
        assert 123.45 == new BigDecimal('123.45') // default BigDecimal type used
        assert 1.200065D == new Double('1.200065')
        assert 1.234F == new Float('1.234')
        assert 1.23E23D == new Double('1.23E23')
        assert 0b1111L.class == Long // binary
        assert 0xFFi.class == Integer // hexadecimal
        assert 034G.class == BigInteger // octal
        // end::number_type_suffixes_example[]
    }

    void testVariableStoreBooleanValue() {
        shouldCompile '''
            @groovy.transform.Field boolean booleanField

            // tag::variable_store_boolean_value[]
            def myBooleanVariable = true
            boolean untypedBooleanVar = false
            booleanField = true
            // end::variable_store_boolean_value[]

            assert myBooleanVariable && !untypedBooleanVar && booleanField
        '''
    }

    void testValidIdentifiers() {
        // tag::valid_identifiers[]
        def name
        def item3
        def with_underscore
        def $dollarStart
        // end::valid_identifiers[]
    }

    void testInvalidIdentifiers() {
        shouldNotCompile '''
            // tag::invalid_identifiers[]
            def 3tier
            def a+b
            def a#b
            // end::invalid_identifiers[]
        '''
    }

    void testAllKeywordsAreValidIdentifiersFollowingADot() {
        shouldCompile '''
        def foo = [:]

        // tag::keywords_valid_id_after_dot[]
        foo.as
        foo.assert
        foo.break
        foo.case
        foo.catch
        // end::keywords_valid_id_after_dot[]
        foo.class
        foo.const
        foo.continue
        foo.def
        foo.default
        foo.do
        foo.else
        foo.enum
        foo.extends
        foo.false
        foo.finally
        foo.for
        foo.goto
        foo.if
        foo.implements
        foo.import
        foo.in
        foo.instanceof
        foo.interface
        foo.new
        foo.null
        foo.package
        foo.return
        foo.super
        foo.switch
        foo.this
        foo.throw
        foo.throws
        foo.true
        foo.try
        foo.while
        '''
    }

    void testShebangCommentLine() {
        def script = '''\
            // tag::shebang_comment_line[]
            #!/usr/bin/env groovy
            println "Hello from the shebang line"
            // end::shebang_comment_line[]
        '''

        shouldCompile StringGroovyMethods.stripIndent((CharSequence)script).split('\n')[1..2].join('\n')
    }

    void testSingleLineComment() {
        // tag::single_line_comment[]
        // a standalone single line comment
        println "hello" // a comment till the end of the line
        // end::single_line_comment[]
    }

    void testMultilineComment() {
        // tag::multiline_comment[]
        /* a standalone multiline comment
           spanning two lines */
        println "hello" /* a multiline comment starting
                           at the end of a statement */
        println 1 /* one */ + 2 /* two */
        // end::multiline_comment[]
    }

    void testGroovyDocComment() {
        shouldCompile '''
            // tag::groovydoc_comment[]
            /**
             * A Class description
             */
            class Person {
                /** the name of the person */
                String name

                /**
                 * Creates a greeting method for a certain person.
                 *
                 * @param otherPerson the person to greet
                 * @return a greeting message
                 */
                String greet(String otherPerson) {
                   "Hello ${otherPerson}"
                }
            }
            // end::groovydoc_comment[]
        '''
    }

    void testQuotedIdentifier() {
        // tag::quoted_id[]
        def map = [:]

        map."an identifier with a space and double quotes" = "ALLOWED"
        map.'with-dash-signs-and-single-quotes' = "ALLOWED"

        assert map."an identifier with a space and double quotes" == "ALLOWED"
        assert map.'with-dash-signs-and-single-quotes' == "ALLOWED"
        // end::quoted_id[]

        // tag::quoted_id_with_gstring[]
        def firstname = "Homer"
        map."Simpson-${firstname}" = "Homer Simpson"

        assert map.'Simpson-Homer' == "Homer Simpson"
        // end::quoted_id_with_gstring[]

        // tag::quoted_id_with_all_strings[]
        map.'single quote'
        map."double quote"
        map.'''triple single quote'''
        map."""triple double quote"""
        map./slashy string/
        map.$/dollar slashy string/$
        // end::quoted_id_with_all_strings[]
    }

    void testStrings() {
        // tag::string_0[]
        // end::string_0[]

        // tag::string_1[]
        'a single-quoted string'
        // end::string_1[]

        // tag::string_2[]
        'an escaped single quote: \' needs a backslash'
        // end::string_2[]

        // tag::string_3[]
        'an escaped escape character: \\ needs a double backslash'
        // end::string_3[]

        // tag::string_4[]
        'The Euro currency symbol: \u20AC'
        // end::string_4[]

        // tag::string_5[]
        "a double-quoted string"
        // end::string_5[]
    }

    void testStringConcatenationWithPlus() {
        // tag::string_plus[]
        assert 'ab' == 'a' + 'b'
        // end::string_plus[]
    }

    void testGString() {
        // tag::gstring_1[]
        def name = 'Guillaume' // a plain string
        def greeting = "Hello ${name}"

        assert greeting.toString() == 'Hello Guillaume'
        // end::gstring_1[]

        // tag::gstring_2[]
        def sum = "The sum of 2 and 3 equals ${2 + 3}"
        assert sum.toString() == 'The sum of 2 and 3 equals 5'
        // end::gstring_2[]

        // tag::gstring_3[]
        def person = [name: 'Guillaume', age: 36]
        assert "$person.name is $person.age years old" == 'Guillaume is 36 years old'
        // end::gstring_3[]
        // tag::gstring_3b[]
        String thing = 'treasure'
        // end::gstring_3b[]
        /*
        // tag::gstring_3b2[]
        assert 'The x-coordinate of the treasure is represented by treasure.x' ==
            "The x-coordinate of the $thing is represented by $thing.x"   // <= Not allowed: ambiguous!!
        // end::gstring_3b2[]
        */
        // tag::gstring_3b3[]
        assert 'The x-coordinate of the treasure is represented by treasure.x' ==
                "The x-coordinate of the $thing is represented by ${thing}.x"  // <= Curly braces required
        // end::gstring_3b3[]

        // tag::gstring_4[]
        def number = 3.14
        // end::gstring_4[]
        // tag::gstring_5[]
        shouldFail(MissingPropertyException) {
            println "$number.toString()"
        }
        // end::gstring_5[]

        // tag::gstring_6[]
        assert '$5' == "\$5"
        assert '${name}' == "\${name}"
        // end::gstring_6[]
    }

    void testCharacters() {
        // tag::char[]
        char c1 = 'A' // <1>
        assert c1 instanceof Character

        def c2 = 'B' as char // <2>
        assert c2 instanceof Character

        def c3 = (char)'C' // <3>
        assert c3 instanceof Character
        // end::char[]
    }

    void testInterpolatingClosuresInGstrings() {
        // tag::closure_in_gstring_1[]
        def sParameterLessClosure = "1 + 2 == ${-> 3}" // <1>
        assert sParameterLessClosure == '1 + 2 == 3'

        def sOneParamClosure = "1 + 2 == ${ w -> w << 3}" // <2>
        assert sOneParamClosure == '1 + 2 == 3'
        // end::closure_in_gstring_1[]

        // tag::closure_in_gstring_2[]
        def number = 1 // <1>
        def eagerGString = "value == ${number}"
        def lazyGString = "value == ${ -> number }"

        assert eagerGString == "value == 1" // <2>
        assert lazyGString ==  "value == 1" // <3>

        number = 2 // <4>
        assert eagerGString == "value == 1" // <5>
        assert lazyGString ==  "value == 2" // <6>
        // end::closure_in_gstring_2[]
    }

    void testGStringCoercerdToStringInMethodCallExpectingString() {
        assertScript '''
        // tag::java_gstring_interop_1[]
        String takeString(String message) {         // <4>
            assert message instanceof String        // <5>
            return message
        }

        def message = "The message is ${'hello'}"   // <1>
        assert message instanceof GString           // <2>

        def result = takeString(message)            // <3>
        assert result instanceof String
        assert result == 'The message is hello'
        // end::java_gstring_interop_1[]
        '''
    }

    void testStringGStringHashCode() {
        // tag::gstring_hashcode_1[]
        assert "one: ${1}".hashCode() != "one: 1".hashCode()
        // end::gstring_hashcode_1[]

        // tag::gstring_hashcode_2[]
        def key = "a"
        def m = ["${key}": "letter ${key}"]     // <1>

        assert m["a"] == null                   // <2>
        // end::gstring_hashcode_2[]
    }

    void testTripleSingleQuotedString() {
        // tag::triple_single_0[]
        '''a triple-single-quoted string'''
        // end::triple_single_0[]

        // tag::triple_single_1[]
        def aMultilineString = '''line one
        line two
        line three'''
        // end::triple_single_1[]

        // tag::triple_single_2[]
        def startingAndEndingWithANewline = '''
        line one
        line two
        line three
        '''
        // end::triple_single_2[]

        // tag::triple_single_3[]
        def strippedFirstNewline = '''\
        line one
        line two
        line three
        '''

        assert !strippedFirstNewline.startsWith('\n')
        // end::triple_single_3[]
    }

    void testTripleDoubleQuotedString() {
        // tag::triple_double_1[]
        def name = 'Groovy'
        def template = """
            Dear Mr ${name},

            You're the winner of the lottery!

            Yours sincerly,

            Dave
        """

        assert template.toString().contains('Groovy')
        // end::triple_double_1[]
    }

    void testSlashyString() {
        // tag::slashy_1[]
        def fooPattern = /.*foo.*/
        assert fooPattern == '.*foo.*'
        // end::slashy_1[]

        // tag::slashy_2[]
        def escapeSlash = /The character \/ is a forward slash/
        assert escapeSlash == 'The character / is a forward slash'
        // end::slashy_2[]

        // tag::slashy_3[]
        def multilineSlashy = /one
            two
            three/

        assert multilineSlashy.contains('\n')
        // end::slashy_3[]

        // tag::slashy_4[]
        def color = 'blue'
        def interpolatedSlashy = /a ${color} car/

        assert interpolatedSlashy == 'a blue car'
        // end::slashy_4[]

        shouldNotCompile '''
            // tag::slashy_5[]
            assert '' == //
            // end::slashy_5[]
        '''
    }

    void testDollarSlashyString() {
        // tag::dollar_slashy_1[]
        def name = "Guillaume"
        def date = "April, 1st"

        def dollarSlashy = $/
            Hello $name,
            today we're ${date}.

            $ dollar sign
            $$ escaped dollar sign
            \ backslash
            / forward slash
            $/ escaped forward slash
            $$$/ escaped opening dollar slashy
            $/$$ escaped closing dollar slashy
        /$

        assert [
            'Guillaume',
            'April, 1st',
            '$ dollar sign',
            '$ escaped dollar sign',
            '\\ backslash',
            '/ forward slash',
            '/ escaped forward slash',
            '$/ escaped opening dollar slashy',
            '/$ escaped closing dollar slashy'
        ].every { dollarSlashy.contains(it) }
        // end::dollar_slashy_1[]
    }

    void testIntegralNumberDeclarations() {
        // tag::int_decl[]
        // primitive types
        byte  b = 1
        char  c = 2
        short s = 3
        int   i = 4
        long  l = 5

        // infinite precision
        BigInteger bi =  6
        // end::int_decl[]

        assert b  instanceof Byte
        assert c  instanceof Character
        assert s  instanceof Short
        assert i  instanceof Integer
        assert l  instanceof Long
        assert bi instanceof BigInteger
    }

    void testDecimalNumberDeclarations() {
        // tag::float_decl[]
        // primitive types
        float  f = 1.234
        double d = 2.345

        // infinite precision
        BigDecimal bd =  3.456
        // end::float_decl[]

        assert f  instanceof Float
        assert d  instanceof Double
        assert bd instanceof BigDecimal

        // tag::float_exp[]
        assert 1e3  ==  1_000.0
        assert 2E4  == 20_000.0
        assert 3e+1 ==     30.0
        assert 4E-2 ==      0.04
        assert 5e-1 ==      0.5
        // end::float_exp[]
    }

    void testWideningIntegralValueTypes() {
        // positive values
        // tag::wide_int_positive[]
        def a = 1
        assert a instanceof Integer

        // Integer.MAX_VALUE
        def b = 2147483647
        assert b instanceof Integer

        // Integer.MAX_VALUE + 1
        def c = 2147483648
        assert c instanceof Long

        // Long.MAX_VALUE
        def d = 9223372036854775807
        assert d instanceof Long

        // Long.MAX_VALUE + 1
        def e = 9223372036854775808
        assert e instanceof BigInteger
        // end::wide_int_positive[]

        // negative values
        // tag::wide_int_negative[]
        def na = -1
        assert na instanceof Integer

        // Integer.MIN_VALUE
        def nb = -2147483648
        assert nb instanceof Integer

        // Integer.MIN_VALUE - 1
        def nc = -2147483649
        assert nc instanceof Long

        // Long.MIN_VALUE
        def nd = -9223372036854775808
        assert nd instanceof Long

        // Long.MIN_VALUE - 1
        def ne = -9223372036854775809
        assert ne instanceof BigInteger
        // end::wide_int_negative[]
    }

    void testNumberPower() {
        // tag::number_power[]
        // base and exponent are ints and the result can be represented by an Integer
        assert    2    **   3    instanceof Integer    //  8
        assert   10    **   9    instanceof Integer    //  1_000_000_000

        // the base is a long, so fit the result in a Long
        // (although it could have fit in an Integer)
        assert    5L   **   2    instanceof Long       //  25

        // the result can't be represented as an Integer or Long, so return a BigInteger
        assert  100    **  10    instanceof BigInteger //  10e20
        assert 1234    ** 123    instanceof BigInteger //  170515806212727042875...

        // the base is a BigDecimal and the exponent a negative int
        // but the result can be represented as an Integer
        assert    0.5  **  -2    instanceof Integer    //  4

        // the base is an int, and the exponent a negative float
        // but again, the result can be represented as an Integer
        assert    1    **  -0.3f instanceof Integer    //  1

        // the base is an int, and the exponent a negative int
        // but the result will be calculated as a Double
        // (both base and exponent are actually converted to doubles)
        assert   10    **  -1    instanceof Double     //  0.1

        // the base is a BigDecimal, and the exponent is an int, so return a BigDecimal
        assert    1.2  **  10    instanceof BigDecimal //  6.1917364224

        // the base is a float or double, and the exponent is an int
        // but the result can only be represented as a Double value
        assert    3.4f **   5    instanceof Double     //  454.35430372146965
        assert    5.6d **   2    instanceof Double     //  31.359999999999996

        // the exponent is a decimal value
        // and the result can only be represented as a Double value
        assert    7.8  **   1.9  instanceof Double     //  49.542708423868476
        assert    2    **   0.1f instanceof Double     //  1.0717734636432956
        // end::number_power[]
    }

    void testLists() {
        // tag::list_1[]
        def numbers = [1, 2, 3]         // <1>

        assert numbers instanceof List  // <2>
        assert numbers.size() == 3      // <3>
        // end::list_1[]

        // tag::list_2[]
        def heterogeneous = [1, "a", true]  // <1>
        // end::list_2[]

        assert heterogeneous instanceof List
        assert heterogeneous.size() == 3

        // tag::coercion_of_list[]
        def arrayList = [1, 2, 3]
        assert arrayList instanceof java.util.ArrayList

        def linkedList = [2, 3, 4] as LinkedList    // <1>
        assert linkedList instanceof java.util.LinkedList

        LinkedList otherLinked = [3, 4, 5]          // <2>
        assert otherLinked instanceof java.util.LinkedList
        // end::coercion_of_list[]

        // tag::subscript_and_leftshift[]
        def letters = ['a', 'b', 'c', 'd']

        assert letters[0] == 'a'     // <1>
        assert letters[1] == 'b'

        assert letters[-1] == 'd'    // <2>
        assert letters[-2] == 'c'

        letters[2] = 'C'             // <3>
        assert letters[2] == 'C'

        letters << 'e'               // <4>
        assert letters[ 4] == 'e'
        assert letters[-1] == 'e'

        assert letters[1, 3] == ['b', 'd']         // <5>
        assert letters[2..4] == ['C', 'd', 'e']    // <6>
        // end::subscript_and_leftshift[]

        // tag::multi_dim_list[]
        def multi = [[0, 1], [2, 3]]     // <1>
        assert multi[1][0] == 2          // <2>
        // end::multi_dim_list[]
    }

    void testArrays() {
        // tag::array_1[]
        String[] arrStr = ['Ananas', 'Banana', 'Kiwi']  // <1>

        assert arrStr instanceof String[]    // <2>
        assert !(arrStr instanceof List)

        def numArr = [1, 2, 3] as int[]      // <3>

        assert numArr instanceof int[]       // <4>
        assert numArr.size() == 3
        // end::array_1[]

        // tag::array_2[]
        def matrix3 = new Integer[3][3]         // <1>
        assert matrix3.size() == 3

        Integer[][] matrix2                     // <2>
        matrix2 = [[1, 2], [3, 4]]
        assert matrix2 instanceof Integer[][]
        // end::array_2[]

        // tag::array_3[]
        String[] names = ['Cédric', 'Guillaume', 'Jochen', 'Paul']
        assert names[0] == 'Cédric'     // <1>

        names[2] = 'Blackdrag'          // <2>
        assert names[2] == 'Blackdrag'
        // end::array_3[]
    }

    void testMaps() {
        // tag::map_def_access[]
        def colors = [red: '#FF0000', green: '#00FF00', blue: '#0000FF']   // <1>

        assert colors['red'] == '#FF0000'    // <2>
        assert colors.green  == '#00FF00'    // <3>

        colors['pink'] = '#FF00FF'           // <4>
        colors.yellow  = '#FFFF00'           // <5>

        assert colors.pink == '#FF00FF'
        assert colors['yellow'] == '#FFFF00'

        assert colors instanceof java.util.LinkedHashMap
        // end::map_def_access[]

        // tag::unknown_key[]
        assert colors.unknown == null
        // end::unknown_key[]

        // tag::number_key[]
        def numbers = [1: 'one', 2: 'two']

        assert numbers[1] == 'one'
        // end::number_key[]

        // tag::variable_key_1[]
        def key = 'name'
        def person = [key: 'Guillaume']      // <1>

        assert !person.containsKey('name')   // <2>
        assert person.containsKey('key')     // <3>
        // end::variable_key_1[]

        // tag::variable_key_2[]
        person = [(key): 'Guillaume']        // <1>

        assert person.containsKey('name')    // <2>
        assert !person.containsKey('key')    // <3>
        // end::variable_key_2[]


    }
}
