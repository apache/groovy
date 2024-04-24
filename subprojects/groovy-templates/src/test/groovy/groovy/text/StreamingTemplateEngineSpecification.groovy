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

import spock.lang.*

import static StreamingTemplateEngineSpecification.EngineType.*

/**
 * Author: Matias Bjarland
 */
class StreamingTemplateEngineSpecification extends Specification {

  enum EngineType {
    STREAMING('StreamingTemplateEngine'),
    SIMPLE('SimpleTemplateEngine'),
    GSTRING('GStringTemplateEngine')
    String displayString

    EngineType(displayString) {
      this.displayString = displayString
    }

    String toString() {
      displayString
    }
  }

  @Shared Map defaultBinding = [alice: 'Alice', rabbit: 'Rabbit', queen: 'Queen', desk: 'writing desk']

  private String template(EngineType type, String data, Map binding=null) {
    TemplateEngine engine
    switch (type) {
      case STREAMING:
        engine = new StreamingTemplateEngine()
        break
      case SIMPLE:
        engine = new SimpleTemplateEngine()
        break
      case GSTRING:
        engine = new GStringTemplateEngine()
        break
    }
    Template template = engine.createTemplate(data)

    Writable writable = (binding ? template.make(binding) : template.make())
    StringWriter sw = new StringWriter()
    writable.writeTo(sw)

    sw
  }

  //TODO: Handle dollarExpressionSlashAtStart case better below
  @Unroll
  def "#testName - #engineType should evaluate '#data' to '#expectedResult' using binding '#binding'"() {
    expect:
      template(engineType, data, binding) == expectedResult

    where:
      data                     | expectedResult      | engineType     | binding        | testName
      ''                       | ''                  | STREAMING      | null           | 'emptyStringNoBinding'
      ''                       | ''                  | STREAMING      | defaultBinding | 'emptyStringWithBinding'
      'bob'                    | 'bob'               | STREAMING      | null           | 'noExpressionsNoBinding'
      'bob'                    | 'bob'               | STREAMING      | defaultBinding | 'noExpressionsWithBinding'

      '\\Hello World'          | '\\Hello World'     | STREAMING      | null           | 'noExpressionsNoBindingEscapingAtStart'
      '\\Hello World'          | '\\Hello World'     | STREAMING      | defaultBinding | 'noExpressionsWithBindingEscapingAtStart'
      '\\\\Hello World'        | '\\\\Hello World'   | STREAMING      | null           | 'noExpressionsNoBindingDoubleEscapingAtStart'
      '\\\\Hello World'        | '\\\\Hello World'   | STREAMING      | defaultBinding | 'noExpressionsWithBindingDoubleEscapingAtStart'
      '\\\\\\Hello World'      | '\\\\\\Hello World' | STREAMING      | null           | 'noExpressionsNoBindingTripleEscapingAtStart'
      '\\\\\\Hello World'      | '\\\\\\Hello World' | STREAMING      | defaultBinding | 'noExpressionsWithBindingTripleEscapingAtStart'

      'Hello World\\'          | 'Hello World\\'     | STREAMING      | null           | 'noExpressionsNoBindingEscapingAtEnd'
      'Hello World\\'          | 'Hello World\\'     | STREAMING      | defaultBinding | 'noExpressionsWithBindingEscapingAtEnd'
      'Hello World\\\\'        | 'Hello World\\\\'   | STREAMING      | null           | 'noExpressionsNoBindingDoubleEscapingAtEnd'
      'Hello World\\\\'        | 'Hello World\\\\'   | STREAMING      | defaultBinding | 'noExpressionsWithBindingDoubleEscapingAtEnd'
      'Hello World\\\\\\'      | 'Hello World\\\\\\' | STREAMING      | null           | 'noExpressionsNoBindingTripleEscapingAtEnd'
      'Hello World\\\\\\'      | 'Hello World\\\\\\' | STREAMING      | defaultBinding | 'noExpressionsWithBindingTripleEscapingAtEnd'

      'Hello $alice'           | 'Hello Alice'       | STREAMING      | defaultBinding | 'dollarExpressionAtEnd'
      '$alice Hello'           | 'Alice Hello'       | STREAMING      | defaultBinding | 'dollarExpressionAtBeginning'
      '$alice'                 | 'Alice'             | STREAMING      | defaultBinding | 'dollarExpressionByItself'
      'a $alice b'             | 'a Alice b'         | STREAMING      | defaultBinding | 'dollarExpressionInBetweenStrings'

      '$rabbit$alice'          | 'RabbitAlice'       | STREAMING      | defaultBinding | 'dollarExpressionTwoAdjacentButDifferent'
      '$alice$alice'           | 'AliceAlice'        | STREAMING      | defaultBinding | 'dollarExpressionTwoAdjacentAndIdentical'
      '$rabbit $alice'         | 'Rabbit Alice'      | STREAMING      | defaultBinding | 'dollarExpressionTwoAdjacentButDifferentWithSpace'
      '$alice $alice'          | 'Alice Alice'       | STREAMING      | defaultBinding | 'dollarExpressionTwoAdjacentAndIdenticalWithSpace'
      '$'                      | '$'                 | STREAMING      | defaultBinding | 'literalDollarSignByItself'
      '\\$'                    | '\\$'               | STREAMING      | defaultBinding | 'literalDollarSignEscapingAtStart'
      '$\\'                    | '$\\'               | STREAMING      | defaultBinding | 'literalDollarSignEscapingAtEnd'
      '\\$alice'               | '\\$alice'          | STREAMING      | defaultBinding | 'dollarExpressionSlashAtStart'
      '\$alice'                | 'Alice'             | STREAMING      | defaultBinding | 'dollarExpressionEscapingAtStart'

      '${rabbit}${alice}'      | 'RabbitAlice'       | STREAMING      | defaultBinding | 'curlyExpressionTwoAdjacentButDifferent'
      '${alice}${alice}'       | 'AliceAlice'        | STREAMING      | defaultBinding | 'curlyExpressionTwoAdjacentAndIdentical'
      '${rabbit} ${alice}'     | 'Rabbit Alice'      | STREAMING      | defaultBinding | 'curlyExpressionTwoAdjacentButDifferentWithSpace'
      '${alice} ${alice}'      | 'Alice Alice'       | STREAMING      | defaultBinding | 'curlyExpressionTwoAdjacentAndIdenticalWithSpace'
      '${}'                    | 'null'              | STREAMING      | defaultBinding | 'curlyExpressionEmptyEvaluatesToNull'

      "<% out.print alice %>"    | 'Alice'           | STREAMING      | defaultBinding | 'scriptletOutPrintBinding'
      "a<% out.print alice %>b"  | 'aAliceb'         | STREAMING      | defaultBinding | 'scriptletOutPrintBindingBetween'
      "<% out.print 'bob' %>"    | 'bob'             | STREAMING      | defaultBinding | 'scriptletOutPrintConstant'
      "a<% out.print 'bob' %>b"  | 'abobb'           | STREAMING      | defaultBinding | 'scriptletOutPrintConstantBetween'

      "<%= alice %>"             | 'Alice'           | STREAMING      | defaultBinding | 'outExprPrintBinding'
      "a<%= alice %>b"           | 'aAliceb'         | STREAMING      | defaultBinding | 'outExprPrintBindingBetween'
      "<%= 'bob' %>"             | 'bob'             | STREAMING      | defaultBinding | 'outExprPrintConstant'
      "a<%= 'bob' %>b"           | 'abobb'           | STREAMING      | defaultBinding | 'outExprPrintConstantBetween'
  }

  /**
   * Validate fix of handling of \r\n line endings as reported by Wilfried Middleton 2014.02.12
   * https://github.com/mbjarland/groovy-streaming-template-engine/issues/1
   * Note: This was before the template engine got merged into groovy-core
   */
  def "should handle \\r\\n line feeds correctly"() {
    setup:
      String basic = '<%\r\n' +
                     'def var1 = "cookie"\r\n' +
                     'out << "This is var1: ${var1}"\r\n' +
                     '%>\r\n' +
                     'Style 1:\r\n' +
                     '<%= "Again, this is var1:${var1}" %>\r\n' +
                     '\r\n' +
                     'Style 2:\r\n' +
                     '${"var1:" + var1}'

    when:
      String gStringTemplate = new GStringTemplateEngine().createTemplate(basic).make().toString()
      String streamingTemplate = new StreamingTemplateEngine().createTemplate(basic).make().toString()

    then:
      gStringTemplate == streamingTemplate
  }

  /**
   * Validate fix of handling of if statements as reported by Wilfried Middleton 2014.02.12
   * https://github.com/mbjarland/groovy-streaming-template-engine/issues/2
   * Note: This was before the template engine got merged into groovy-core
   */
  def "should handle simple embedded if statements"() {
    setup:
      String templateText = 'before "<% if (false) { %>should not be included<% } else { %>should be included<% } %>" after'

    when:
      Template template = new StreamingTemplateEngine().createTemplate(templateText)
      String result  = template.make().toString()

    then:
      result == 'before "should be included" after'
  }

  /**
   * Validate fix of handling of if statements as reported by Wilfried Middleton 2014.02.12
   * https://github.com/mbjarland/groovy-streaming-template-engine/issues/2
   * Note: This was before the template engine got merged into groovy-core
   */
  def "should handle complex embedded if statements same as GStringTemplateEngine"() {
    setup:
      String templateText = 'first line text\n' +
                            '<%\n' +
                            'def var1 = "test"\n' +
                            'if (false) {%>\n' +
                            '  non executed text\n' +
                            '  <%\n' +
                            '  out << "more non-executed text"\n' +
                            '} else {%>\n' +
                            '  else stuff\n' +
                            '<% \n' +
                            '} %>\n' +
                            '\n' +
                            'last line text\n'

    when:
      Template template1 = new StreamingTemplateEngine().createTemplate(templateText)
      Template template2 = new GStringTemplateEngine().createTemplate(templateText)

      String streamingResult  = template1.make().toString()
      String gStringResult    = template2.make().toString()

    then:
      streamingResult == gStringResult
  }

  def "should execute the javadoc example with correct output"() {
    setup:
      def binding = [firstname : "Grace",
                     lastname  : "Hopper",
                     accepted  : false,
                     title     : 'Groovy for COBOL programmers']
      def text = '''\
        |Dear <% out.print firstname %> ${lastname},
        |
        |We <% if (accepted) out.print 'are pleased' else out.print 'regret' %> to inform you that your paper entitled
        |'$title' was ${ accepted ? 'accepted' : 'rejected' }.
        |
        |The conference committee.
        '''.stripMargin()

      def expected = '''\
        |Dear Grace Hopper,
        |
        |We regret to inform you that your paper entitled
        |'Groovy for COBOL programmers' was rejected.
        |
        |The conference committee.
        '''.stripMargin()

    when:
      String result = template(STREAMING, text, binding)

    then:
     result == expected
  }

  def "should execute the javadoc example same as GStringTemplateEngine"() {
    setup:
      def binding = [firstname : "Grace",
                   lastname  : "Hopper",
                   accepted  : false,
                   title     : 'Groovy for COBOL programmers']
    def text = '''\
        |Dear <% out.print firstname %> ${lastname},
        |
        |We <% if (accepted) out.print 'are pleased' else out.print 'regret' %> to inform you that your paper entitled
        |'$title' was ${ accepted ? 'accepted' : 'rejected' }.
        |
        |The conference committee.
        '''.stripMargin()

    when:
      String streaming = template(STREAMING, text, binding)
      String gString   = template(GSTRING, text, binding)

    then:
      streaming == gString
  }
/*
  def "should throw exception with correct line number on template execution error in mid template"() {
    setup:
      def binding = [firstname : "Grace",
                     lastname  : "Hopper",
                     accepted  : false,
                     title     : 'Groovy for COBOL programmers']
    def text = '''\
        |Dear <% out.print firstname %> ${lastname},
        |
        |We <% if (accepted) out.print 'are pleased' else out.print 'regret' %> to inform you that your paper entitled
        |'$txitle' was ${ accepted ? 'accepted' : 'rejected' }.
        |
        |The conference committee.
        '''.stripMargin()

    when:
      template(STREAMING, text, binding)

    then:
      def e = thrown(TemplateExecutionException)
      e.lineNumber == 4
  }

  def "should throw exception with correct line number on template execution error at start of template"() {
    setup:
      def binding = [firstname : "Grace",
                     lastname  : "Hopper",
                     accepted  : false,
                     title     : 'Groovy for COBOL programmers']
      def text = '''\
          |$txitle Dear <% out.print firstname %> ${lastname},
          |
          |We <% if (accepted) out.print 'are pleased' else out.print 'regret' %> to inform you that your paper entitled
          |'$title' was ${ accepted ? 'accepted' : 'rejected' }.
          |
          |The conference committee.
          '''.stripMargin()

    when:
      template(STREAMING, text, binding)

    then:
      def e = thrown(TemplateExecutionException)
      e.lineNumber == 1
  }

  def "should throw exception with correct line number on template execution error at end of template"() {
    setup:
    def binding = [firstname : "Grace",
                   lastname  : "Hopper",
                   accepted  : false,
                   title     : 'Groovy for COBOL programmers']
    def text = '''\
        |Dear <% out.print firstname %> ${lastname},
        |
        |We <% if (accepted) out.print 'are pleased' else out.print 'regret' %> to inform you that your paper entitled
        |'$title' was ${ accepted ? 'accepted' : 'rejected' }.
        |
        |The conference committee.
        |$txitle'''.stripMargin()

    when:
    template(STREAMING, text, binding)

    then:
    def e = thrown(TemplateExecutionException)
    e.lineNumber == 7
  }


  def "should throw exception with correct line number on template parse error in mid template"() {
    setup:
      def binding = [firstname : "Grace",
                     lastname  : "Hopper",
                     accepted  : false,
                     title     : 'Groovy for COBOL programmers']
    def text = '''\
        |Dear <% out.print firstname %> ${lastname},
        |
        |We <% if (accepted) out.print 'are pleased' else out.print 'regret' %> to inform you that your paper entitled
        |<% -- %> was ${ accepted ? 'accepted' : 'rejected' }.
        |
        |The conference committee.
        '''.stripMargin()

    when:
      template(STREAMING, text, binding)

    then:
      def e = thrown(TemplateParseException)
      e.lineNumber == 4
  }

  def "should throw exception with correct line number on template parse error at start of template"() {
    setup:
      def binding = [firstname : "Grace",
                     lastname  : "Hopper",
                     accepted  : false,
                     title     : 'Groovy for COBOL programmers']
      def text = '''\
          ||<% -- %> Dear <% out.print firstname %> ${lastname},
          |
          |We <% if (accepted) out.print 'are pleased' else out.print 'regret' %> to inform you that your paper entitled
          |'$title' was ${ accepted ? 'accepted' : 'rejected' }.
          |
          |The conference committee.
          '''.stripMargin()

    when:
      template(STREAMING, text, binding)

    then:
      def e = thrown(TemplateParseException)
      e.lineNumber == 1
  }

  def "should throw exception with correct line number on template parse error at end of template"() {
    setup:
      def binding = [firstname : "Grace",
                     lastname  : "Hopper",
                     accepted  : false,
                     title     : 'Groovy for COBOL programmers']
      def text = '''\
          |Dear <% out.print firstname %> ${lastname},
          |
          |We <% if (accepted) out.print 'are pleased' else out.print 'regret' %> to inform you that your paper entitled
          |'$title' was ${ accepted ? 'accepted' : 'rejected' }.
          |
          |The conference committee.
          ||<% -- %>'''.stripMargin()

    when:
      template(STREAMING, text, binding)

    then:
      def e = thrown(TemplateParseException)
      e.lineNumber == 7
  }
*/
  @Unroll
  def "should evaluate adjacent expressions '#expression' to '#expected'"() {
    expect:
      //noinspection GroovyAssignabilityCheck
      template(STREAMING, expression, defaultBinding) == expected

    where:
      //Create all permutations of the different kinds of expressions and validate that they
      //evaluate to expected results. We have encountered a few too many edge cases with failing
      //adjacent evaluations...this should cover most of them
      [expression, expected] << [
        ['constant'               , 'constant'],
        ['${alice}'               , 'Alice'],
        ['$rabbit '               , 'Rabbit '],
        ['<%= queen %>'           , 'Queen'],
        ['<% out << desk %>'      , 'writing desk'],
      ].permutations().collect { p ->
        p.inject(['','']) { List acc, val ->
          acc[0] += val[0]
          acc[1] += val[1]
          acc
        }
      }
  }
}
