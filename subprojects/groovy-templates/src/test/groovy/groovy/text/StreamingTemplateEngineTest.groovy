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
package groovy.text

import org.junit.Test

import static org.junit.Assert.assertThrows
import static groovy.test.GroovyAssert.assertScript

final class StreamingTemplateEngineTest {

    private static final String SIXTY_FOUR_K_OF_A
    private static final int SIXTY_FOUR_K = 64 * 1024

    static {
        StringBuilder b = new StringBuilder()
        def sixtyFourAs = 'a' * 64
        (1..1024).each {
            b.append(sixtyFourAs)
        }
        SIXTY_FOUR_K_OF_A = b.toString()
    }

    private TemplateEngine engine = new StreamingTemplateEngine()
    private Map<String, Object> binding = [alice: 'Alice', rabbit: 'Rabbit', queen: 'Queen', desk: 'writing desk']

    private String template(String data, Map<String, Object> binding = null) {
        Template template = engine.createTemplate(data)

        Writable writable = (binding ? template.make(binding) : template.make())
        StringWriter sw = new StringWriter()
        writable.writeTo(sw)

        return sw.toString()
    }

    @Test
    void testEmptyStringNoBinding() {
        String data = ''
        String result = template(data)
        assert data == result
    }

    @Test
    void testEmptyStringWithBinding() {
        String data = ''
        String result = template(data, binding)
        assert data == result
    }

    @Test
    void noExpressionsNoBinding() {
        String data = 'Hello World!'
        String result = template(data)
        assert data == result
    }

    @Test
    void noExpressionsEscapingAtEnd() {
        String data = 'Hello World\\'
        String result = template(data)
        assert data == result
    }

    @Test
    void noExpressionsDoubleEscapingAtEnd() {
        String data = 'Hello World\\\\'
        String result = template(data)
        assert data == result
    }

    @Test
    void noExpressionsTripleEscapingAtEnd() {
        String data = 'Hello World\\\\\\'
        String result = template(data)
        assert data == result
    }

    @Test
    void noExpressionsEscapingAtStart() {
        String data = '\\Hello World'
        String result = template(data)
        assert data == result
    }

    @Test
    void noExpressionsDoubleEscapingAtStart() {
        String data = '\\\\Hello World'
        String result = template(data)
        assert data == result
    }

    @Test
    void noExpressionsTripleEscapingAtStart() {
        String data = '\\\\\\Hello World'
        String result = template(data)
        assert data == result
    }

    @Test
    void incompleteGStringExpressionEscapedAtStart() {
        String data = '\\$Hello World'
        String result = template(data)
        assert data == result
    }

    @Test
    void incompleteGStringExpressionEscapedAtEnd() {
        String data = 'Hello World\\$'
        String result = template(data)
        assert data == result
    }

    @Test
    void incompleteTwoCharGStringExpressionEscapedAtStart() {
        String data = '\\${Hello World'
        String result = template(data)
        assert '${Hello World' == result
    }

    @Test
    void incompleteTwoCharGStringExpressionEscapedAtEnd() {
        String data = 'Hello World\\${'
        String result = template(data)
        assert 'Hello World${' == result
    }

    @Test
    void escapedSlashesInFrontOfGStringExpressionAtStart() {
        String data = '\\\\${alice}'
        String result = template(data, binding)
        assert '\\Alice' == result
    }

    @Test
    void escapedSlashesInFrontOfGStringExpressionAtEnd() {
        String data = '${alice}\\\\'
        String result = template(data, binding)
        assert 'Alice\\\\' == result
    }

    @Test
    void incompleteLessThanExpressionEscapedAtStart() {
        String data = '\\<Hello World'
        String result = template(data)
        assert data == result
    }

    @Test
    void incompleteLessThanExpressionEscapedAtEnd() {
        String data = 'Hello World\\<'
        String result = template(data)
        assert data == result
    }

    @Test
    void incompleteTwoCharLessThanExpressionEscapedAtStart() {
        String data = '\\<%Hello World'
        String result = template(data)
        assert '<%Hello World' == result
    }

    @Test
    void incompleteTwoCharLessThanExpressionEscapedAtEnd() {
        String data = 'Hello World\\<%'
        String result = template(data)
        assert 'Hello World<%' == result
    }

    @Test
    void escapedSlashesInFrontOfLessThanExpressionAtStart() {
        String data = '\\\\<%= alice %>'
        String result = template(data, binding)
        assert '\\Alice' == result
    }

    @Test
    void escapedSlashesInFrontOfLessThanExpressionAtEnd() {
        String data = '<%= alice %>\\\\'
        String result = template(data, binding)
        assert 'Alice\\\\' == result
    }

    @Test
    void testStringOver64kNoBinding() {
        StringBuilder data = new StringBuilder()
        data.append(SIXTY_FOUR_K_OF_A)

        String result = template(data.toString())

        assert result.startsWith("aaaaaaaaaaaaa")
        assert result.endsWith("aaaaaaaaaaa")
        assert result.length() == SIXTY_FOUR_K
    }

    @Test
    void testStringOver64kWithStartingGString() {
        StringBuilder data = new StringBuilder()
        String prefix = '${alice}, why is a raven like a ${desk}?'
        data.append(prefix)
        data.append(SIXTY_FOUR_K_OF_A)

        String result = template(data.toString(), binding)

        String expectedStart = 'Alice, why is a raven like a writing desk?'
        assert result.startsWith(expectedStart)
        assert result.endsWith("aaaaaaaaaaaaaaa")
        assert result.length() == expectedStart.length() + SIXTY_FOUR_K
    }

    @Test
    void testStringOver64kWithEndingGString() {
        StringBuilder data = new StringBuilder()
        data.append(SIXTY_FOUR_K_OF_A)
        String postfix = '${alice}, why is a raven like a ${desk}'
        data.append(postfix)

        String result = template(data.toString(), binding)

        assert result.startsWith("aaaaaaaaaaaaa")
        String expectedEnding = 'Alice, why is a raven like a writing desk'
        assert result.endsWith(expectedEnding)
        assert result.length() == SIXTY_FOUR_K + expectedEnding.length()
    }

    @Test
    void testStringOver64kWithMiddleGString() {
        StringBuilder data = new StringBuilder()
        data.append(SIXTY_FOUR_K_OF_A)
        String middle = '${alice}, why is a raven like a ${desk}?'
        data.append(middle)
        data.append(SIXTY_FOUR_K_OF_A)

        String result = template(data.toString(), binding)
        String expectedMiddle = 'Alice, why is a raven like a writing desk?'

        assert result.indexOf(expectedMiddle) == SIXTY_FOUR_K
        assert result.startsWith("aaaaaaaaaaaaaaaaa")
        assert result.endsWith("aaaaaaaaaaaaaaa")
        assert result.length() == SIXTY_FOUR_K * 2 + expectedMiddle.length()
    }

    @Test
    void testStringOver64kWithStartingExpression() {
        StringBuilder data = new StringBuilder()
        String prefix = '<%= alice %>, why is a raven like a <%= desk %>?'
        data.append(prefix)
        data.append(SIXTY_FOUR_K_OF_A)

        String result = template(data.toString(), binding)

        String expectedStart = 'Alice, why is a raven like a writing desk?'
        assert result.startsWith(expectedStart)
        assert result.endsWith("aaaaaaaaaaaaaaa")
        assert result.length() == expectedStart.length() + SIXTY_FOUR_K
    }

    @Test
    void testStringOver64kWithEndingExpression() {
        StringBuilder data = new StringBuilder()
        data.append(SIXTY_FOUR_K_OF_A)
        String postfix = '<%= alice %>, why is a raven like a <%= desk %>'
        data.append(postfix)

        String result = template(data.toString(), binding)

        assert result.startsWith("aaaaaaaaaaaaa")
        String expectedEnding = 'Alice, why is a raven like a writing desk'
        assert result.endsWith(expectedEnding)
        assert result.length() == SIXTY_FOUR_K + expectedEnding.length()
    }

    @Test
    void testStringOver64kWithMiddleExpression() {
        StringBuilder data = new StringBuilder()
        data.append(SIXTY_FOUR_K_OF_A)
        String middle = '<%= alice %>, why is a raven like a <%= desk %>?'
        data.append(middle)
        data.append(SIXTY_FOUR_K_OF_A)

        String result = template(data.toString(), binding)
        String expectedMiddle = 'Alice, why is a raven like a writing desk?'

        assert result.indexOf(expectedMiddle) == SIXTY_FOUR_K
        assert result.startsWith("aaaaaaaaaaaaaaaaa")
        assert result.endsWith("aaaaaaaaaaaaaaa")
        assert result.length() == SIXTY_FOUR_K * 2 + expectedMiddle.length()
    }

    @Test
    void testStringOver64kWithStartingSection() {
        StringBuilder data = new StringBuilder()
        String prefix = '<% out << alice %>, why is a raven like a <% out << desk %>?'
        data.append(prefix)
        data.append(SIXTY_FOUR_K_OF_A)

        String result = template(data.toString(), binding)

        String expectedStart = 'Alice, why is a raven like a writing desk?'
        assert result.startsWith(expectedStart)
        assert result.endsWith("aaaaaaaaaaaaaaa")
        assert result.length() == expectedStart.length() + SIXTY_FOUR_K
    }

    @Test
    void testStringOver64kWithEndingSection() {
        StringBuilder data = new StringBuilder()
        data.append(SIXTY_FOUR_K_OF_A)
        String postfix = '<% out << alice %>, why is a raven like a <% out << desk %>'
        data.append(postfix)

        String result = template(data.toString(), binding)

        assert result.startsWith("aaaaaaaaaaaaa")
        String expectedEnding = 'Alice, why is a raven like a writing desk'
        assert result.endsWith(expectedEnding)
        assert result.length() == SIXTY_FOUR_K + expectedEnding.length()
    }

    @Test
    void testStringOver64kWithMiddleSection() {
        StringBuilder data = new StringBuilder()
        data.append(SIXTY_FOUR_K_OF_A)
        String middle = '<% out << alice %>, why is a raven like a <% out << desk %>?'
        data.append(middle)
        data.append(SIXTY_FOUR_K_OF_A)

        String result = template(data.toString(), binding)
        String expectedMiddle = 'Alice, why is a raven like a writing desk?'

        assert result.indexOf(expectedMiddle) == SIXTY_FOUR_K
        assert result.startsWith("aaaaaaaaaaaaaaaaa")
        assert result.endsWith("aaaaaaaaaaaaaaa")
        assert result.length() == SIXTY_FOUR_K * 2 + expectedMiddle.length()
    }

    @Test
    void testEscapingGString() {
        String data = 'This should be \\${left alone}!'
        String result = template(data, binding)
        assert 'This should be ${left alone}!' == result
    }

    @Test
    void testEscapingNonGString() {
        String data = 'This should be \\$[left alone]!'
        String result = template(data, binding)
        assert 'This should be \\$[left alone]!' == result
    }

    @Test
    void testEscapingDollarSign() {
        String data = 'This should be \\$ left alone'
        String result = template(data, binding)
        assert 'This should be \\$ left alone' == result
    }

    @Test
    void testEscapingAtEndOfString() {
        String data = 'This should be \\'
        String result = template(data, binding)
        assert 'This should be \\' == result
    }

    @Test
    void testEscapingGStringExtraSlashInFront() {
        String data = 'This should be \\\\${alice}!'
        String result = template(data, binding)
        assert 'This should be \\Alice!' == result
    }

    @Test
    void mixedGStringExpressionSequenceNoStringSections() {
        String data = '${alice}<% out << rabbit %><%= queen %>'
        String result = template(data, binding)
        assert 'AliceRabbitQueen' == result
    }

    @Test
    void mixedGStringExpressionSequenceWithStringSections() {
        String data = 'Hi ${alice}, have you seen the <% out << rabbit %> and the <%= queen %>?'
        String result = template(data, binding)
        assert 'Hi Alice, have you seen the Rabbit and the Queen?' == result
    }

    @Test
    void multiLineCodeSectionWithEmbeddedBindings() {
        String data = '''<%
            out << "Hi ${alice}, "
            out << "have you seen the ${rabbit} "
            out << "and the ${queen}"
        %>?'''
        String result = template(data, binding)
        assert 'Hi Alice, have you seen the Rabbit and the Queen?' == result
    }

    @Test
    void multiLineCodeSectionWithErrorsAndBindings() {
        String data = '''<%
            out << "Hi ${alice}, "
            out << "have you seen the ${ :rabbit} "
            out << "and the ${queen}"
        %>?'''
        def e = assertThrows(TemplateParseException) {
            template(data, binding)
        }
        assert e.message.contains("at line 3,")
    }

    @Test
    void nonTerminatedGStringExpression() {
        String data = '''Hi
            ${alice'''
        def e = assertThrows(TemplateParseException) {
            template(data, binding)
        }
        assert e.message.contains("at line 2,")
    }

    @Test
    void nonTerminatedLessThanExpression() {
        String data = 'Hi <%=alice'
        def e = assertThrows(TemplateParseException) {
            template(data, binding)
        }
        assert e.message.contains("at line 1,")
    }

    @Test
    void nonTerminatedLessThanCodeBlock() {
        String data = 'Hi <% out << alice'

        String result = template(data, binding)
        // weirdly this is actually ok with the current implementation...
        assert "Hi Alice" == result
    }

    @Test
    void reuseClassLoader1() {
        assertScript '''
            final reuseOption = 'groovy.StreamingTemplateEngine.reuseClassLoader'
            System.setProperty(reuseOption, 'true')
            try {
                // reload class to initialize static field from the beginning
                def steClass = groovy.text.StreamingTemplateEngineTest.reloadClass('groovy.text.StreamingTemplateEngine')

                GroovyClassLoader gcl = new GroovyClassLoader()
                def engine = steClass.newInstance(gcl)
                assert 'Hello, Daniel' == engine.createTemplate('Hello, ${name}').make([name: 'Daniel']).toString()
                assert gcl.loadedClasses.length > 0
                def cloned = gcl.loadedClasses.clone()
                assert 'Hello, Paul' == engine.createTemplate('Hello, ${name}').make([name: 'Paul']).toString()
                assert cloned == gcl.loadedClasses
            } finally {
                System.clearProperty(reuseOption)
            }
        '''
    }

    @Test
    void reuseClassLoader2() {
        assertScript '''
            final reuseOption = 'groovy.StreamingTemplateEngine.reuseClassLoader'
            System.setProperty(reuseOption, 'true')
            try {
                // reload class to initialize static field from the beginning
                def steClass = groovy.text.StreamingTemplateEngineTest.reloadClass('groovy.text.StreamingTemplateEngine')

                GroovyClassLoader gcl = new GroovyClassLoader()
                def engine = steClass.newInstance(gcl)
                assert 'Hello, Daniel' == engine.createTemplate('Hello, ${name}').make([name: 'Daniel']).toString()
                assert gcl.loadedClasses.length > 0
                def cloned = gcl.loadedClasses.clone()
                engine = steClass.newInstance(gcl)
                assert 'Hello, Paul' == engine.createTemplate('Hello, ${name}').make([name: 'Paul']).toString()
                assert cloned == gcl.loadedClasses
            } finally {
                System.clearProperty(reuseOption)
            }
        '''
    }

    static Class reloadClass(String className) {
        def loader = new GroovyClassLoader() {
            private final Map<String, Class> loadedClasses = new java.util.concurrent.ConcurrentHashMap<>()

            @Override
            Class loadClass(String name) {
                if (name ==~ ('^' + className + '([$].+)?$')) {
                    return loadedClasses.computeIfAbsent(name, n -> {
                        def clazz = defineClass(n, GroovyClassLoader.class.getResourceAsStream('/' + n.replace('.', '/') + '.class').bytes)
                        return clazz
                    })
                }
                return super.loadClass(name)
            }
        }
        def loaded = loader.loadClass(className)
        return loaded
    }
}
