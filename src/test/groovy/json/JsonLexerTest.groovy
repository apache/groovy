package groovy.json

import static groovy.json.JsonTokenType.*

/**
 * @author Guillaume Laforge
 */
class JsonLexerTest extends GroovyTestCase {

    void testSkipWhitespace() {
        def content = "    true"
        def reader = new LineColumnReader(new StringReader(content))

        def lexer = new JsonLexer(reader)
        lexer.skipWhitespace()

        def charsLeftRead = new char[4]
        reader.read(charsLeftRead)

        assert new String(charsLeftRead) == "true"
    }

    void testNextToken() {
        def content = [" true ", " false ", " null ", " -123.456e78 ", " [ ", " ] ", " { ", " } ", " : ", " , "]

        assert content.collect {
            def lexer = new JsonLexer(new StringReader(it))
            lexer.nextToken().type
        } == [
                BOOL_TRUE, BOOL_FALSE, NULL, NUMBER,
                OPEN_BRACKET, CLOSE_BRACKET, OPEN_CURLY, CLOSE_CURLY, COLON, COMMA
        ]
    }

    void testSuiteOfTokens() {
        def content = ' [ true, null, false, { "a" : 1, "b": "hi"}, 12.34 ] '
        def lexer = new JsonLexer(new StringReader(content))

        def output = lexer.collect { it.toString() }

        assert output == [
                "[ (OPEN_BRACKET) [1:2-1:3]",
                "true (BOOL_TRUE) [1:4-1:8]",
                ", (COMMA) [1:8-1:9]",
                "null (NULL) [1:10-1:14]",
                ", (COMMA) [1:14-1:15]",
                "false (BOOL_FALSE) [1:16-1:21]",
                ", (COMMA) [1:21-1:22]",
                "{ (OPEN_CURLY) [1:23-1:24]",
                '"a" (STRING) [1:25-1:28]',
                ": (COLON) [1:29-1:30]",
                "1 (NUMBER) [1:31-1:32]",
                ", (COMMA) [1:32-1:33]",
                '"b" (STRING) [1:34-1:37]',
                ": (COLON) [1:37-1:38]",
                '"hi" (STRING) [1:39-1:43]',
                "} (CLOSE_CURLY) [1:43-1:44]",
                ", (COMMA) [1:44-1:45]",
                "12.34 (NUMBER) [1:46-1:51]",
                "] (CLOSE_BRACKET) [1:52-1:53]"
        ]
    }

    void testBeginningOfAValidToken() {
        def content = "  truaaa "
        def lexer = new JsonLexer(new StringReader(content))

        def msg = shouldFail(JsonException) {
            lexer.nextToken()
        }

        assert msg.contains("trua")
        assert msg.contains("constant 'true'")
        assert msg.contains("column: 7")
    }

    void testBeginningOfValidNumber() {
        def content = " 2134.432a"
        def lexer = new JsonLexer(new StringReader(content))

        assert lexer.nextToken().text == "2134.432"
        shouldFail(JsonException) {
            lexer.nextToken()
        }
    }

    void testIteratorRemoveUnimplemented() {
        def lexer = new JsonLexer(new StringReader(""))

        shouldFail(UnsupportedOperationException) {
            lexer.remove()
        }
    }
}
