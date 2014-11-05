package groovy.text

import org.junit.Test
import org.junit.Before

class StreamingTemplateEngineTest {
  TemplateEngine engine
  Map binding
  private static final String SIXTY_FOUR_K_OF_A
  private static final int SIXTY_FOUR_K = 64 * 1024

  static {
    StringBuilder b = new StringBuilder()
    def sixtyFourAs = "a" * 64
    (1..1024).each {
      b.append(sixtyFourAs)
    }
    SIXTY_FOUR_K_OF_A = b.toString()
  }

  @Before
  void setUp() {
    engine = new StreamingTemplateEngine()
    binding = [alice: 'Alice', rabbit: 'Rabbit', queen: 'Queen', desk: 'writing desk']
  }

  private String template(String data, Map binding = null) {
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

  /**
   *  We expect the SimpleTemplateEngine to throw an exception with strings
   *  larger than 64k. That is one of the reasons this class was written in the first place. The
   *  exception message is something along the lines of:
   *
   *  <pre>
   *  groovy.lang.GroovyRuntimeException: Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): startup failed:
   *  GStringTemplateScript1.groovy: 2: String too long. The given string is 65536 Unicode code units long, but only a maximum of 65535 is allowed.
   *    @ line 2, column 79.
   *    new Binding(delegate); out << """aaaaaaa
   *                                  ^
   *  </pre>
   *
   */
  @Test(expected=groovy.lang.GroovyRuntimeException)
  void testStringOver64kNoBinding_SimpleTemplateEngine() {
    StringBuilder data = new StringBuilder()
    data.append(SIXTY_FOUR_K_OF_A)

    SimpleTemplateEngine engine = new SimpleTemplateEngine()

    //the below line throws an exception...
    Template template = engine.createTemplate(data.toString())
    Writable writable = template.make()
    StringWriter sw = new StringWriter()
    writable.writeTo(sw)

    String result = sw.toString()

    assert result.startsWith("aaaaaaaaaaaaa")
    assert result.endsWith("aaaaaaaaaaa")
    assert result.length() == SIXTY_FOUR_K
  }

  /**
   *  We expect the SimpleTemplateEngine to throw an exception with strings
   *  larger than 64k. That is one of the reasons this class was written in the first place. The
   *  exception message is something along the lines of:
   *
   *  <pre>
   *  groovy.lang.GroovyRuntimeException: Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): startup failed:
   *  SimpleTemplateScript1.groovy: 1: String too long. The given string is 65536 Unicode code units long, but only a maximum of 65535 is allowed.
   *    @ line 1, column 11.
   *    out.print("""
   *  </pre>
   *
   */
  @Test(expected=groovy.lang.GroovyRuntimeException)
  void testStringOver64kNoBinding_GStringTemplateEngine() {
    StringBuilder data = new StringBuilder()
    data.append(SIXTY_FOUR_K_OF_A)

    GStringTemplateEngine engine = new GStringTemplateEngine()

    //the below line throws an exception...
    Template template = engine.createTemplate(data.toString())
    Writable writable = template.make()
    StringWriter sw = new StringWriter()
    writable.writeTo(sw)

    String result = sw.toString()

    assert result.startsWith("aaaaaaaaaaaaa")
    assert result.endsWith("aaaaaaaaaaa")
    assert result.length() == SIXTY_FOUR_K
  }

  /**
   * And expect the StreamingTemplateEngine to work with arbitrary length strings
   */
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
    try {
      template(data, binding)
      assert false //we should trow an exception above
    } catch (Throwable e) {
      assert e.getMessage().contains("at line 3,")
    }
  }

  @Test
  void nonTerminatedGStringExpression() {
    String data = '''Hi
                     ${alice'''
    try {
      template(data, binding)
      assert false //we should trow an exception above
    } catch (Throwable e) {
      assert e.getMessage().contains("at line 2,")
    }
  }

  @Test
  void nonTerminatedLessThanExpression() {
    String data = 'Hi <%=alice'
    try {
      template(data, binding)
      assert false //we should trow an exception above
    } catch (Throwable e) {
      assert e.getMessage().contains("at line 1,")
    }
  }

  @Test
  void nonTerminatedLessThanCodeBlock() {
    String data = 'Hi <% out << alice'

    String result = template(data, binding)
    //weirdly this is actually ok with the current implementation...
    assert "Hi Alice" == result
  }

}
