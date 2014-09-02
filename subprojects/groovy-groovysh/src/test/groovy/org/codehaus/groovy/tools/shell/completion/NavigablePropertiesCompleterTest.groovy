package org.codehaus.groovy.tools.shell.completion


/**
 * Defines method tokenList for other Unit tests and tests it
 */
class NavigablePropertiesCompleterTest extends GroovyTestCase {

    void testPatternNoControlChars() {
        assert !'java\bCfoo'.matches(NavigablePropertiesCompleter.NO_CONTROL_CHARS_PATTERN)
        assert !'java\u001BCfoo'.matches(NavigablePropertiesCompleter.NO_CONTROL_CHARS_PATTERN)
        assert 'ja12_<!$%&_?§'.matches(NavigablePropertiesCompleter.NO_CONTROL_CHARS_PATTERN)
    }

    void testPatternInvalidIdentifierChar() {
        assert 'java@foo'.find(NavigablePropertiesCompleter.INVALID_CHAR_FOR_IDENTIFIER_PATTERN)
        assert 'java&bar'.find(NavigablePropertiesCompleter.INVALID_CHAR_FOR_IDENTIFIER_PATTERN)
        assert 'java~bar'.find(NavigablePropertiesCompleter.INVALID_CHAR_FOR_IDENTIFIER_PATTERN)
        assert !'javaBar$foo_b2'.find(NavigablePropertiesCompleter.INVALID_CHAR_FOR_IDENTIFIER_PATTERN)
    }

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
                'a\rb' : 'control',
                'a\'b' : 'quote',
                'a\\b' : 'escape',
                'a\nb' : 'new line',
                'a\tb' : 'tab',
                'G$\\"tring' : 'string',
                '_ !@#$%^&*()_+={}[]~`<>,./?:;|' : 'operators',
                'snowman ☃' : 'Olaf',
                'Japan ぁ' : '77',
                'ぁJapanstart' : '77',
                'a☃$4ä_' : 'no hypehns',
                '$123' : 'digits',
                '123$' : 'digits',
                '\u0002foo' : 'bar'
        ]

        Set candidates = [] as Set
        Set expected = ['id', 'name', '\'a b\'', '\'a.b\'', '\'a\\\'b\'', '\'a\\\\b\'', '\'G$\\\\"tring\'',
                        '\'_ !@#$%^&*()_+={}[]~`<>,./?:;|\'', '\'snowman ☃\'', '\'Japan ぁ\'', 'ぁJapanstart', 'a☃$4ä_', '$123', '\'123$\'' ] as Set
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
