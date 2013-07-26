package org.codehaus.groovy.tools.shell.completion

import static org.codehaus.groovy.tools.shell.completion.TokenUtilTest.tokenList

class KeywordCompletorTest extends GroovyTestCase {

    void testKeywordModifier() {
        KeywordSyntaxCompletor completor = new KeywordSyntaxCompletor()
        def candidates = []
        String buffer = "pub"
        assertEquals(true, completor.complete(tokenList(buffer), candidates))
        assertEquals(['public '], candidates)
    }

    void testKeywordModifierSecond() {
        KeywordSyntaxCompletor completor = new KeywordSyntaxCompletor()
        def candidates = []
        String buffer = "public sta"
        assertEquals(true, completor.complete(tokenList(buffer), candidates))
        assertEquals(['static '], candidates)
        candidates = []
        buffer = "public swi" // don't suggest switch keyword here
        assertEquals(true, completor.complete(tokenList(buffer), candidates))
        assertEquals(["switch ("], candidates)
    }

    void testKeywordModifierThird() {
        KeywordSyntaxCompletor completor = new KeywordSyntaxCompletor()
        def candidates = []
        String buffer = "public static inter"
        assertEquals(true, completor.complete(tokenList(buffer), candidates))
        assertEquals(['interface '], candidates)
    }

    void testKeywordModifierFor() {
        KeywordSyntaxCompletor completor = new KeywordSyntaxCompletor()
        def candidates = []
        String buffer = "fo"
        assertEquals(true, completor.complete(tokenList(buffer), candidates))
        assertEquals(['for ('], candidates)
    }
}
