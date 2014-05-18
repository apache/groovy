package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.GroovySourceToken
import org.codehaus.groovy.tools.shell.util.CurlyCountingGroovyLexer

/**
 * Defines method tokenList for other Unit tests and tests it
 */
class NavigablePropertiesCompleterTest extends GroovyTestCase {

    void testSet() {
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

    void testMap() {
        NavigablePropertiesCompleter completer = new NavigablePropertiesCompleter()
        completer.addCompletions(null, '', [] as Set)

        Map map = [
                'id': 'FX-17',
                name: 'Turnip',
                99: 123,
                (new Object()) : 'some non string object',
                [] : 'some non string object',
                'a b' : 'space',
                'a.b' : 'dot',
                'a\'b' : 'quote',
                'a\\b' : 'escape',
                'a\nb' : 'new line',
                'a\tb' : 'tab',
                'G$\\"tring' : 'string',
                '!@#$%^&*()_+={}[]~`<>,./?:;|_' : 'operators',
                '_ !@#$%^&*()_+={}[]~`<>,./?:;|' : 'operators',
                '☃' : 'snowman'
        ]

        Set candidates = [] as Set
        Set expected = ['id', 'name', '\'a b\'', '\'a.b\'', '\'a\\\'b\'', '\'a\\\\b\'', '\'a\\nb\'', '\'a\\tb\'',
                        '\'G$\\\\"tring\'', '\'_ !@#$%^&*()_+={}[]~`<>,./?:;|\''] as Set
        completer.addCompletions(map, '', candidates)
        assert expected == candidates
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