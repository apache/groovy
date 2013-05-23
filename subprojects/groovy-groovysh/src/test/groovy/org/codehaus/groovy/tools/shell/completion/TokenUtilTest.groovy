package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.tools.shell.util.CurlyCountingGroovyLexer

/**
 * Defines method tokenList for other Unit tests and tests it
 */
class TokenUtilTest extends GroovyTestCase {

    /**
     * return token list without EOF
     */
    static List<GroovySourceToken> tokenList(String src) {
        CurlyCountingGroovyLexer lexer = CurlyCountingGroovyLexer.createGroovyLexer(src)
        def result = lexer.toList()
        if (result && result.size() > 1) {
           return result[0..-2]
        }
        return []
    }

    void testTokenList() {
        assertEquals([], tokenList(""))
        assertEquals('foo', tokenList("foo")[0].getText())
        assertEquals(['foo'], tokenList("foo").collect { it.getText()})
        assertEquals(['foo', '{', 'bar'], tokenList("foo{bar").collect { it.getText()})
        assertEquals(['1', '..', '2'], tokenList("1..2").collect { it.getText()})
    }

    static tokensString(List<GroovySourceToken> tokens) {
        if (tokens == null) {
            return null
        }
        return tokens.collect {it.getText()}.join()
    }

}
