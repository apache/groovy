package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.tools.shell.util.CurlyCountingGroovyLexer

/**
 * Defines method tokenList for other Unit tests and tests it
 */
class NavigablePropertiesCompleterTest extends GroovyTestCase {

    void testMap() {
        NavigablePropertiesCompleter completer = new NavigablePropertiesCompleter()
        completer.addCompletions(null, '', [] as Set)

        Set candidates = [] as Set
        completer.addCompletions(['aaa': 1, 'bbb': 2], '', candidates)
        assert ['aaa', 'bbb'] as Set == candidates

        candidates = [] as Set
        completer.addCompletions(['aaa': 1, 'bbb': 2], 'a', candidates)
        assert ['aaa'] as Set == candidates

        candidates = [] as Set
        completer.addCompletions(['aaa': 1, 'bbb': 2], 'a', candidates)
        assert ['aaa'] as Set == candidates
    }

    void testNodeList() {
        NavigablePropertiesCompleter completer = new NavigablePropertiesCompleter()
        completer.addCompletions(null, '', [] as Set)
        NodeBuilder someBuilder = new NodeBuilder()
        Node node = someBuilder.foo(){[bar(){[bam(7)]}, baz()]}

        Set candidates = [] as Set
        completer.addCompletions(node, 'ba', candidates)
        assert ['bar', 'baz'] as Set == candidates

    }
}