import gls.CompilableTestSupport

class SyntaxTest extends CompilableTestSupport {

    void testNumberTypeSuffixes() {
        // tag::number_type_suffixes_example[]
        assert 42I == new Integer("42")
        assert 123L == new Long("123")
        assert 2147483648 == new Long("2147483648") //Long type used, value too large for an Integer
        assert 456G == new java.math.BigInteger("456")
        assert 123.45 == new java.math.BigDecimal("123.45") //default BigDecimal type used
        assert 1.200065D == new Double("1.200065")
        assert 1.234F == new Float("1.234")
        assert 1.23E23D == new Double("1.23E23")
        // end::number_type_suffixes_example[]
    }

    void testBooleanVariableStoreNull() {
        // tag::boolean_variable_store_null[]
        boolean myFlag = null
        // end::boolean_variable_store_null[]
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