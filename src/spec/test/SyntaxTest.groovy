import gls.CompilableTestSupport

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
        assert xNegativeInt == -179
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
        assert 42i == new Integer('42') //lowercase i more readable
        assert 123L == new Long("123") //uppercase L more readable
        assert 2147483648 == new Long('2147483648') //Long type used, value too large for an Integer
        assert 456G == new BigInteger('456')
        assert 456g == new BigInteger('456')
        assert 123.45 == new BigDecimal('123.45') //default BigDecimal type used
        assert 1.200065D == new Double('1.200065')
        assert 1.234F == new Float('1.234')
        assert 1.23E23D == new Double('1.23E23')
        assert 0b1111L.class == Long // binary
        assert 0xFFi.class == Integer // hexadecimal
        assert 034G.class == BigInteger // ocatal
        // end::number_type_suffixes_example[]
    }

    void testVariableStoreBooleanValue() {
        shouldCompile '''
            def myBooleanVariable
            // tag::variable_store_boolean_value[]
            myBooleanVariable = true
            // end::variable_store_boolean_value[]
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

        shouldCompile script.stripIndent().split('\n')[1..2].join('\n')
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
                 * @return ag reeting message
                 */
                String greet(String otherPerson) {
                   "Hello ${otherPerson}"
                }
            }
            // end::groovydoc_comment[]
        '''
    }
}
